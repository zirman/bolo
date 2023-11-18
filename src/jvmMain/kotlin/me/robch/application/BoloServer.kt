@file:OptIn(ExperimentalUnsignedTypes::class)

package me.robch.application

import bmap.Bmap
import bmap.BmapCode
import bmap.Terrain
import bmap.toByteArray
import bmap.toExtra
import bmap.writeBmap
import bmap.writeBmapCode
import bmap.writeDamage
import frame.FrameClient
import frame.FrameServer
import frame.Owner
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.send
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import util.isBuildable
import util.pillArmorMax
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.mutableListOf
import kotlin.collections.plus
import kotlin.collections.set
import kotlin.collections.toByteArray
import kotlin.collections.toUByteArray
import kotlin.math.max
import kotlin.math.min

class BoloServer : KoinComponent {
    val bmap: Bmap by inject()
    val bmapCode: BmapCode by inject()
    val nextOwnerId = AtomicInteger()
    val clients: MutableMap<Owner, DefaultWebSocketServerSession> = ConcurrentHashMap()

    suspend fun handleWebSocket(session: DefaultWebSocketServerSession) {
        if (clients.size >= 16) {
            throw Exception("clients full")
        }
        val owner = Owner(nextOwnerId.getAndIncrement())
        clients[owner] = session
        try {
            val buffer: MutableList<UByte> = mutableListOf()
            writeBmap(bmap, buffer)
            writeDamage(bmap, buffer)
            writeBmapCode(bmapCode, buffer)

            buffer
                .toUByteArray()
                .toByteArray()
                .plus(bmap.toExtra(owner.int).toByteArray())
                .run { session.send(this) }

            clients.forEach { (callee) ->
                if (callee != owner) {
                    FrameServer.Signal
                        .NewPeer(callee)
                        .toByteArray()
                        .run { session.send(this) }
                }
            }

            for (frame in session.incoming) {
                when (frame) {
                    is Frame.Binary -> {
                        when (val frameClient = frame.toFrameClient()) {
                            is FrameClient.Signal -> {
                                clients[frameClient.owner]?.run {
                                    when (frameClient) {
                                        is FrameClient.Signal.Offer -> FrameServer.Signal.Offer(
                                            from = owner,
                                            sessionDescription = frameClient.sessionDescription
                                        )

                                        is FrameClient.Signal.Answer -> FrameServer.Signal.Answer(
                                            from = owner,
                                            sessionDescription = frameClient.sessionDescription
                                        )

                                        is FrameClient.Signal.IceCandidate -> FrameServer.Signal.IceCandidate(
                                            from = owner,
                                            iceCandidate = frameClient.iceCandidate
                                        )
                                    }
                                        .toByteArray()
                                        .run { send(this) }
                                }
                            }

                            is FrameClient.TerrainBuild -> {
                                val isSuccessful =
                                    when (frameClient.terrain) {
                                        Terrain.Grass3 -> bmap[frameClient.x, frameClient.y] == Terrain.Tree
                                        Terrain.Boat -> bmap[frameClient.x, frameClient.y] == Terrain.River
                                        Terrain.Wall,
                                        Terrain.Road,
                                        -> isBuildable(bmap[frameClient.x, frameClient.y])

                                        else -> false
                                    }

                                run {
                                    if (isSuccessful) FrameServer.TerrainBuildSuccess
                                    else FrameServer.TerrainBuildFailed
                                }
                                    .toByteArray()
                                    .run { session.send(this) }

                                if (isSuccessful) {
                                    bmap[frameClient.x, frameClient.y] = frameClient.terrain
                                    bmapCode.inc(frameClient.x, frameClient.y)

                                    val serverFrame = FrameServer
                                        .TerrainBuild(
                                            terrain = frameClient.terrain,
                                            x = frameClient.x,
                                            y = frameClient.y,
                                        )
                                        .toByteArray()

                                    clients
                                        .filter { (_, client) -> client != session }
                                        .forEach { (_, client) -> client.send(serverFrame) }
                                }

                                Unit
                            }

                            is FrameClient.TerrainDamage -> {
                                bmap.damage(frameClient.x, frameClient.y)
                                val codeMatches = bmapCode[frameClient.x, frameClient.y] == frameClient.code

                                val serverFrame = FrameServer
                                    .TerrainDamage(
                                        x = frameClient.x,
                                        y = frameClient.y,
                                    )
                                    .toByteArray()

                                clients
                                    .let { clients ->
                                        if (codeMatches) {
                                            clients.filter { (_, client) -> client != session }
                                        } else {
                                            clients
                                        }
                                    }
                                    .forEach { (_, client) -> client.send(serverFrame) }
                            }

                            is FrameClient.BaseDamage -> {
                                val base = bmap.bases[frameClient.index]
                                base.armor = max(0, base.armor - 8)

                                val serverFrame = FrameServer
                                    .BaseDamage(index = frameClient.index)
                                    .toByteArray()

                                clients
                                    .let { clients ->
                                        if (base.code == frameClient.code) {
                                            clients.filter { (_, client) -> client != session }
                                        } else {
                                            clients
                                        }
                                    }
                                    .forEach { (_, client) -> client.send(serverFrame) }
                            }

                            is FrameClient.PillDamage -> {
                                val pill = bmap.pills[frameClient.index]

                                if (pill.isPlaced &&
                                    pill.x == frameClient.x &&
                                    pill.y == frameClient.y
                                ) {
                                    pill.armor = max(0, pill.armor - 1)

                                    val serverFrame = FrameServer
                                        .PillDamage(index = frameClient.index)
                                        .toByteArray()

                                    clients.forEach { (_, client) ->
                                        if (pill.code != frameClient.code) {
                                            client.send(serverFrame)
                                        }
                                    }
                                }

                                Unit
                            }

                            is FrameClient.PillRepair -> {
                                val pill = bmap.pills[frameClient.index]

                                if (pill.isPlaced &&
                                    pill.x == frameClient.x &&
                                    pill.y == frameClient.y &&
                                    pill.owner == frameClient.owner
                                ) {
                                    val additionalArmor = frameClient.material * 4
                                    val oldArmor = pill.armor
                                    val newArmor = min(pillArmorMax, oldArmor + additionalArmor)
                                    pill.armor = newArmor
                                    pill.code++

                                    FrameServer
                                        .PillRepairSuccess(material = (additionalArmor - (newArmor - oldArmor)) / 4)
                                        .toByteArray()
                                        .run { session.send(this) }

                                    val serverFrame = FrameServer
                                        .PillRepair(
                                            index = frameClient.index,
                                            armor = pill.armor,
                                        )
                                        .toByteArray()

                                    clients.forEach { (_, client) -> client.send(serverFrame) }
                                }

                                Unit
                            }

                            is FrameClient.PillPlacement -> {
                                val pill = bmap.pills[frameClient.index]

                                if (pill.isPlaced.not() &&
                                    pill.owner == owner.int
                                ) {
                                    pill.isPlaced = true
                                    pill.armor = min(pillArmorMax, frameClient.material * 4)
                                    pill.x = frameClient.x
                                    pill.y = frameClient.y
                                    pill.code++

                                    FrameServer.PillPlacementSuccess
                                        .toByteArray()
                                        .run { session.send(this) }

                                    val serverFrame = FrameServer
                                        .PillPlacement(
                                            index = frameClient.index,
                                            armor = pill.armor,
                                            x = pill.x,
                                            y = pill.y,
                                        )
                                        .toByteArray()

                                    clients.forEach { (_, client) -> client.send(serverFrame) }
                                }

                                Unit
                            }

                            is FrameClient.Position -> {
                                // update held pill positions
                                bmap.pills
                                    .filter { it.owner == owner.int && it.isPlaced.not() }
                                    .forEach { pill ->
                                        pill.x = frameClient.x
                                        pill.y = frameClient.y
                                    }

                                // check for pill takes
                                bmap.pills.forEachIndexed { index, pill ->
                                    if (pill.isPlaced &&
                                        pill.x == frameClient.x &&
                                        pill.y == frameClient.y &&
                                        pill.armor == 0
                                    ) {
                                        pill.owner = owner.int
                                        pill.isPlaced = false

                                        val serverFrame = FrameServer
                                            .PillTake(
                                                index = index,
                                                owner = owner.int,
                                            )
                                            .toByteArray()

                                        clients.forEach { (_, client) -> client.send(serverFrame) }
                                    }
                                }

                                // check for base takes
                                bmap.bases.forEachIndexed { index, base ->
                                    if (base.x == frameClient.x &&
                                        base.y == frameClient.y &&
                                        (base.owner == 0xff || base.armor == 0)
                                    ) {
                                        if (base.owner != 0xff) {
                                            base.armor = 0
                                            base.shells = 0
                                            base.mines = 0
                                        }

                                        base.owner = owner.int
                                        base.code++

                                        val serverFrame = FrameServer
                                            .BaseTake(
                                                index = index,
                                                owner = base.owner,
                                                armor = base.armor,
                                                shells = base.shells,
                                                mines = base.mines,
                                            )
                                            .toByteArray()

                                        clients.forEach { (_, client) -> client.send(serverFrame) }
                                    }
                                }
                            }
                        }
                    }

                    is Frame.Ping,
                    is Frame.Pong,
                    -> {
                    }

                    is Frame.Close -> {
                        break
                    }

                    is Frame.Text -> {
                        throw Exception("Unexpected text frame")
                    }
                }
            }
        } catch (error: Throwable) {
            currentCoroutineContext().ensureActive()
            error.printStackTrace()
            throw error
        } finally {
            session.close()
            clients.remove(owner)

            // drop pills
            bmap.pills.forEachIndexed { index, pill ->
                if (pill.owner == owner.int) {
                    pill.owner = 0xff

                    if (pill.isPlaced.not()) {
                        // TODO check placement
                        pill.isPlaced = true
                    }

                    val frameServer = FrameServer
                        .PillDrop(
                            index = index,
                            owner = pill.owner,
                            x = pill.x,
                            y = pill.y,
                        )
                        .toByteArray()

                    clients.forEach { (_, client) -> client.send(frameServer) }
                }
            }

            // neutralize bases
            bmap.bases.forEachIndexed { index, base ->
                if (base.owner == owner.int) {
                    base.owner = 0xff

                    val frameServer = FrameServer
                        .BaseTake(
                            index = index,
                            owner = base.owner,
                            armor = base.armor,
                            shells = base.shells,
                            mines = base.mines,
                        )
                        .toByteArray()

                    clients.forEach { (_, client) -> client.send(frameServer) }
                }
            }
        }
    }
}
