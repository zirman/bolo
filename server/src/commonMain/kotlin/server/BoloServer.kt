@file:OptIn(ExperimentalUnsignedTypes::class)

package server

import common.bmap.Bmap
import common.bmap.BmapCode
import common.bmap.TerrainTile
import common.bmap.toByteArray
import common.bmap.toExtra
import common.bmap.writeBmap
import common.bmap.writeBmapCode
import common.bmap.writeDamage
import common.PILL_ARMOR_MAX
import common.isMined
import common.isRoadBuildable
import common.isWallBuildable
import common.frame.FrameClient
import common.frame.FrameServer
import common.frame.Owner
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.send
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.max
import kotlin.math.min

class BoloServer(
    private val bmap: Bmap,
    private val bmapCode: BmapCode,
) {
    private val nextOwnerId = AtomicInteger()
    private val clients: MutableMap<Owner, DefaultWebSocketServerSession> = ConcurrentHashMap()

    suspend fun handleWebSocket(session: DefaultWebSocketServerSession) {
        if (clients.size >= 16) {
            throw IllegalStateException("clients full")
        }

        val owner = Owner(nextOwnerId.getAndIncrement())
        clients[owner] = session

        try {
            mutableListOf<UByte>()
                .writeBmap(bmap)
                .writeDamage(bmap)
                .writeBmapCode(bmapCode)
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
                if (session.handleFrame(owner, frame)) break
            }
        } catch (error: Throwable) {
            currentCoroutineContext().ensureActive()
            error.printStackTrace()
            throw error
        } finally {
            session.cleanup(owner)
        }
    }

    private suspend fun DefaultWebSocketServerSession.handleBinary(owner: Owner, frame: Frame.Binary) {
        when (val frameClient = frame.toFrameClient()) {
            is FrameClient.Signal -> {
                handleSignal(owner, frameClient)
            }

            is FrameClient.TerrainBuild -> {
                handleTerrainBuild(frameClient)
            }

            is FrameClient.TerrainDamage -> {
                handleTerrainDamage(frameClient)
            }

            is FrameClient.TerrainMine -> {
                handleTerrainMine(frameClient)
            }

            is FrameClient.BaseDamage -> {
                handleBaseDamage(frameClient)
            }

            is FrameClient.PillDamage -> {
                handlePillDamage(frameClient)
            }

            is FrameClient.PillRepair -> {
                handlePillRepair(frameClient)
            }

            is FrameClient.PillPlacement -> {
                handlePillPlacement(owner, frameClient)
            }

            is FrameClient.Position -> {
                handlePosition(owner, frameClient)
            }
        }
    }

    private suspend fun handleSignal(owner: Owner, frameClient: FrameClient.Signal) {
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

    private suspend fun DefaultWebSocketServerSession.handleTerrainBuild(frameClient: FrameClient.TerrainBuild) {
        val terrainTile = bmap[frameClient.col, frameClient.row]

        if (terrainTile.isMined()) {
            bmap.damage(frameClient.col, frameClient.row)

            val serverFrame = FrameServer
                .TerrainDamage(
                    col = frameClient.col,
                    row = frameClient.row,
                )
                .toByteArray()

            clients.forEach { (_, client) -> client.send(serverFrame) }
            send(FrameServer.TerrainBuildMined.toByteArray())
        } else {
            val isSuccessful = when (frameClient.terrain) {
                TerrainTile.Grass3 -> bmap[frameClient.col, frameClient.row] == TerrainTile.Tree
                TerrainTile.Road -> terrainTile.isRoadBuildable()
                TerrainTile.Wall -> terrainTile.isWallBuildable()
                TerrainTile.Boat -> terrainTile == TerrainTile.River

                else -> false
            }

            if (isSuccessful) {
                bmap[frameClient.col, frameClient.row] = frameClient.terrain
                bmapCode.inc(frameClient.col, frameClient.row)

                val serverFrame = FrameServer
                    .TerrainBuild(
                        terrain = frameClient.terrain,
                        col = frameClient.col,
                        row = frameClient.row,
                    )
                    .toByteArray()

                clients
                    .filter { (_, client) -> client != this }
                    .forEach { (_, client) -> client.send(serverFrame) }
            }

            send(
                if (isSuccessful) FrameServer.TerrainBuildSuccess.toByteArray()
                else FrameServer.TerrainBuildFailed.toByteArray()
            )
        }
    }

    private suspend fun DefaultWebSocketServerSession.handleFrame(owner: Owner, frame: Frame): Boolean {
        when (frame) {
            is Frame.Binary -> {
                handleBinary(owner, frame)
            }

            is Frame.Ping,
            is Frame.Pong,
            -> {
            }

            is Frame.Text -> {
                throw IllegalStateException("Unexpected text frame")
            }

            is Frame.Close -> {
                return true
            }
        }

        return false
    }

    private suspend fun DefaultWebSocketServerSession.handleTerrainDamage(frameClient: FrameClient.TerrainDamage) {
        bmap.damage(frameClient.col, frameClient.row)
        val codeMatches = bmapCode[frameClient.col, frameClient.row] == frameClient.code

        val serverFrame = FrameServer
            .TerrainDamage(
                col = frameClient.col,
                row = frameClient.row,
            )
            .toByteArray()

        run {
            if (codeMatches) {
                clients.filter { (_, client) -> client != this }
            } else {
                clients
            }
        }.forEach { (_, client) -> client.send(serverFrame) }
    }

    private suspend fun DefaultWebSocketServerSession.handleTerrainMine(frameClient: FrameClient.TerrainMine) {
        bmap.mine(frameClient.col, frameClient.row)

        val serverFrame = FrameServer
            .TerrainMine(
                col = frameClient.col,
                row = frameClient.row,
            )
            .toByteArray()

        clients.forEach { (_, client) ->
            if (client != this) {
                client.send(serverFrame)
            }
        }
    }

    private suspend fun DefaultWebSocketServerSession.handleBaseDamage(frameClient: FrameClient.BaseDamage) {
        val base = bmap.bases[frameClient.index]
        base.armor = max(0, base.armor - 8)

        val serverFrame = FrameServer
            .BaseDamage(index = frameClient.index)
            .toByteArray()

        clients
            .let { clients ->
                if (base.code == frameClient.code) {
                    clients.filter { (_, client) -> client != this }
                } else {
                    clients
                }
            }
            .forEach { (_, client) -> client.send(serverFrame) }
    }

    private suspend fun handlePillDamage(frameClient: FrameClient.PillDamage) {
        val pill = bmap.pills[frameClient.index]

        if (pill.isPlaced &&
            pill.col == frameClient.col &&
            pill.row == frameClient.row
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
    }

    private suspend fun DefaultWebSocketServerSession.handlePillRepair(frameClient: FrameClient.PillRepair) {
        val pill = bmap.pills[frameClient.index]

        if (pill.isPlaced &&
            pill.col == frameClient.col &&
            pill.row == frameClient.row &&
            pill.owner == frameClient.owner
        ) {
            val additionalArmor = frameClient.material * 4
            val oldArmor = pill.armor
            val newArmor = min(PILL_ARMOR_MAX, oldArmor + additionalArmor)
            pill.armor = newArmor
            pill.code++

            FrameServer
                .PillRepairSuccess(material = (additionalArmor - (newArmor - oldArmor)) / 4)
                .toByteArray()
                .run { send(this) }

            val serverFrame = FrameServer
                .PillRepair(
                    index = frameClient.index,
                    armor = pill.armor,
                )
                .toByteArray()

            clients.forEach { (_, client) -> client.send(serverFrame) }
        }
    }

    private suspend fun DefaultWebSocketServerSession.handlePillPlacement(
        owner: Owner,
        frameClient: FrameClient.PillPlacement,
    ) {
        val pill = bmap.pills[frameClient.index]

        if (pill.isPlaced.not() &&
            pill.owner == owner.int
        ) {
            pill.isPlaced = true
            pill.armor = min(PILL_ARMOR_MAX, frameClient.material * 4)
            pill.col = frameClient.col
            pill.row = frameClient.row
            pill.code++

            FrameServer.PillPlacementSuccess
                .toByteArray()
                .run { send(this) }

            val serverFrame = FrameServer
                .PillPlacement(
                    index = frameClient.index,
                    armor = pill.armor,
                    col = pill.col,
                    row = pill.row,
                )
                .toByteArray()

            clients.forEach { (_, client) -> client.send(serverFrame) }
        }
    }

    private suspend fun handlePosition(owner: Owner, frameClient: FrameClient.Position) {
        // update held pill positions
        bmap.pills
            .filter { it.owner == owner.int && it.isPlaced.not() }
            .forEach { pill ->
                pill.col = frameClient.col
                pill.row = frameClient.row
            }

        // check for pill takes
        bmap.pills.forEachIndexed { index, pill ->
            if (pill.isPlaced &&
                pill.col == frameClient.col &&
                pill.row == frameClient.row &&
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
            if (base.col == frameClient.col &&
                base.row == frameClient.row &&
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

        // check for mines
        if (bmap[frameClient.col, frameClient.row].isMined()) {
            bmap.damage(frameClient.col, frameClient.row)

            val serverFrame = FrameServer
                .TerrainDamage(
                    col = frameClient.col,
                    row = frameClient.row,
                )
                .toByteArray()

            clients.forEach { (_, client) -> client.send(serverFrame) }
        }
    }

    private suspend fun DefaultWebSocketServerSession.cleanup(owner: Owner) {
        close()
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
                        col = pill.col,
                        row = pill.row,
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

        FrameServer.Signal
            .Disconnect(from = owner)
            .toByteArray()
            .run {
                clients.forEach { (_, client) ->
                    client.send(this)
                }
            }
    }
}
