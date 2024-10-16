@file:OptIn(ExperimentalUnsignedTypes::class)

package server

import common.MATERIAL_PER_PILL_ARMOR
import common.MATERIAL_PER_TREE
import common.PILL_ARMOR_MAX
import common.PILL_DAMAGE_PER_SHOT
import common.bmap.Bmap
import common.bmap.BmapCode
import common.bmap.TerrainTile
import common.frame.FrameClient
import common.frame.FrameServer
import common.frame.Owner
import common.isMined
import common.isRoadBuildable
import common.isWallBuildable
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.send
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import server.bmap.toByteArray
import server.bmap.toExtra
import server.bmap.writeBmap
import server.bmap.writeBmapCode
import server.bmap.writeDamage
import server.frame.toByteArray
import server.frame.toFrameClient
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class BoloServer(
    private val bmap: Bmap,
    private val bmapCode: BmapCode,
) {
    private var nextOwnerId = 0
    private val clients: MutableMap<Owner, DefaultWebSocketServerSession> = mutableMapOf()
    private val lockContext = Dispatchers.Default.limitedParallelism(1)

    suspend fun handleWebSocket(session: DefaultWebSocketServerSession) {
        var owner: Owner? = null
        try {
            lock {
                if (clients.size >= 16) {
                    throw IllegalStateException("clients full")
                }
                owner = Owner(nextOwnerId++)
                clients[owner!!] = session
                mutableListOf<UByte>()
                    .writeBmap(bmap)
                    .writeDamage(bmap)
                    .writeBmapCode(bmapCode)
                    .toUByteArray()
                    .toByteArray()
                    .plus(bmap.toExtra(owner!!.int).toByteArray())
                    .sendTo(session)
                clients.forEach { (callee) ->
                    if (callee != owner) {
                        FrameServer.Signal
                            .NewPeer(callee)
                            .toByteArray()
                            .sendTo(session)
                    }
                }
            }
            for (frame in session.incoming) {
                if (lock { session.handleFrame(owner!!, frame) }) {
                    break
                }
            }
        } catch (error: Throwable) {
            currentCoroutineContext().ensureActive()
            error.printStackTrace()
            throw error
        } finally {
            withContext(lockContext + NonCancellable) {
                owner?.cleanup(session)
            }
        }
    }

    private suspend inline fun <T> lock(crossinline block: () -> T): T = withContext(lockContext) {
        block()
    }

    private fun DefaultWebSocketServerSession.handleFrame(owner: Owner, frame: Frame): Boolean {
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

    private fun DefaultWebSocketServerSession.handleBinary(owner: Owner, frame: Frame.Binary) {
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

    private fun handleSignal(owner: Owner, frameClient: FrameClient.Signal) {
        clients[frameClient.owner]?.run {
            when (frameClient) {
                is FrameClient.Signal.Offer -> FrameServer.Signal.Offer(
                    from = owner,
                    sessionDescription = frameClient.sessionDescription,
                )

                is FrameClient.Signal.Answer -> FrameServer.Signal.Answer(
                    from = owner,
                    sessionDescription = frameClient.sessionDescription,
                )

                is FrameClient.Signal.IceCandidate -> FrameServer.Signal.IceCandidate(
                    from = owner,
                    iceCandidate = frameClient.iceCandidate,
                )
            }
                .toByteArray()
                .sendTo(this)
        }
    }

    private fun DefaultWebSocketServerSession.handleTerrainBuild(frameClient: FrameClient.TerrainBuild) {
        val terrainTile = bmap[frameClient.col, frameClient.row]
        if (terrainTile.isMined()) {
            bmap.damage(frameClient.col, frameClient.row)
            val serverFrame = FrameServer
                .TerrainDamage(
                    col = frameClient.col,
                    row = frameClient.row,
                )
                .toByteArray()

            clients.forEach { (_, client) ->
                serverFrame.sendTo(client)
            }
            FrameServer.TerrainBuildMined.toByteArray().sendTo(this)
        } else {
            val isSuccessful = when (frameClient.terrain) {
                TerrainTile.Grass3 -> if (bmap[frameClient.col, frameClient.row] == TerrainTile.Tree) MATERIAL_PER_TREE else null
                TerrainTile.Road -> if (terrainTile.isRoadBuildable()) 0 else null
                TerrainTile.Wall -> if (terrainTile.isWallBuildable()) 0 else null
                TerrainTile.Boat -> if (terrainTile == TerrainTile.River) 0 else null
                else -> null
            }
            if (isSuccessful != null) {
                bmap[frameClient.col, frameClient.row] = frameClient.terrain
                bmapCode.inc(frameClient.col, frameClient.row)
                val serverFrame = FrameServer
                    .TerrainBuild(
                        terrain = frameClient.terrain,
                        col = frameClient.col,
                        row = frameClient.row,
                    )
                    .toByteArray()
                clients.forEach { (_, client) ->
                    if (client != this@handleTerrainBuild) {
                        serverFrame.sendTo(client)
                    }
                }
            }
            if (isSuccessful != null) {
                FrameServer.TerrainBuildSuccess(isSuccessful)
            } else {
                FrameServer.TerrainBuildFailed
            }.toByteArray().sendTo(this)
        }
    }

    private fun DefaultWebSocketServerSession.handleTerrainDamage(frameClient: FrameClient.TerrainDamage) {
        bmap.damage(frameClient.col, frameClient.row)
        val codeMatches = bmapCode[frameClient.col, frameClient.row] == frameClient.code
        val serverFrame = FrameServer
            .TerrainDamage(
                col = frameClient.col,
                row = frameClient.row,
            )
            .toByteArray()
        clients.forEach { (_, client) ->
            if (client != this@handleTerrainDamage || codeMatches.not()) {
                serverFrame.sendTo(client)
            }
        }
    }

    private fun DefaultWebSocketServerSession.handleTerrainMine(frameClient: FrameClient.TerrainMine) {
        val placeMineResult = if (bmap[frameClient.col, frameClient.row].isMined()) {
            FrameServer.MinePlaceMined
        } else if (bmap.mine(frameClient.col, frameClient.row)) {
            bmapCode.inc(frameClient.col, frameClient.row)
            val serverFrame = FrameServer
                .TerrainMine(col = frameClient.col, row = frameClient.row)
                .toByteArray()
            clients.forEach { (_, client) ->
                if (client != this@handleTerrainMine) {
                    serverFrame.sendTo(client)
                }
            }
            FrameServer.MinePlaceSuccess
        } else {
            FrameServer.MinePlaceFailed
        }
        placeMineResult.toByteArray().sendTo(this)
    }

    private fun DefaultWebSocketServerSession.handleBaseDamage(frameClient: FrameClient.BaseDamage) {
        val base = bmap.bases[frameClient.index]
        base.armor = (base.armor - 8).coerceAtLeast(0)

        val serverFrame = FrameServer
            .BaseDamage(index = frameClient.index)
            .toByteArray()

        clients.forEach { (_, client) ->
            if (client != this@handleBaseDamage || base.code != frameClient.code) {
                serverFrame.sendTo(client)
            }
        }
    }

    private fun DefaultWebSocketServerSession.handlePillDamage(frameClient: FrameClient.PillDamage) {
        val pill = bmap.pills[frameClient.index]

        if (pill.isPlaced &&
            pill.col == frameClient.col &&
            pill.row == frameClient.row
        ) {
            pill.armor = (pill.armor - PILL_DAMAGE_PER_SHOT).coerceAtLeast(0)
            val serverFrame = FrameServer
                .PillDamage(index = frameClient.index)
                .toByteArray()
            clients.forEach { (_, client) ->
                if (client != this@handlePillDamage || pill.code != frameClient.code) {
                    serverFrame.sendTo(client)
                }
            }
        }
    }

    private fun DefaultWebSocketServerSession.handlePillRepair(frameClient: FrameClient.PillRepair) {
        val pill = bmap.pills[frameClient.index]
        if (pill.isPlaced &&
            pill.col == frameClient.col &&
            pill.row == frameClient.row
        ) {
            val oldArmor = pill.armor
            val newArmor = (oldArmor + frameClient.material * MATERIAL_PER_PILL_ARMOR).coerceAtMost(PILL_ARMOR_MAX)
            pill.armor = newArmor
            pill.code++
            FrameServer
                .PillRepairSuccess(material = (newArmor - oldArmor + MATERIAL_PER_PILL_ARMOR - 1) / MATERIAL_PER_PILL_ARMOR)
                .toByteArray()
                .sendTo(this)
            val serverFrame = FrameServer
                .PillRepair(
                    index = frameClient.index,
                    armor = pill.armor,
                )
                .toByteArray()
            clients.forEach { (_, client) ->
                if (client != this@handlePillRepair) {
                    serverFrame.sendTo(client)
                }
            }
        }
    }

    private fun DefaultWebSocketServerSession.handlePillPlacement(
        owner: Owner,
        frameClient: FrameClient.PillPlacement,
    ) {
        val pill = bmap.pills[frameClient.index]
        if (pill.isPlaced.not() &&
            pill.owner == owner.int
        ) {
            pill.isPlaced = true
            pill.armor = (frameClient.material * MATERIAL_PER_PILL_ARMOR).coerceAtMost(PILL_ARMOR_MAX)
            pill.col = frameClient.col
            pill.row = frameClient.row
            pill.code++
            FrameServer.PillPlacementSuccess
                .toByteArray()
                .sendTo(this)
            val serverFrame = FrameServer
                .PillPlacement(
                    index = frameClient.index,
                    armor = pill.armor,
                    col = pill.col,
                    row = pill.row,
                )
                .toByteArray()
            clients.forEach { (_, client) ->
                if (client != this@handlePillPlacement) {
                    serverFrame.sendTo(client)
                }
            }
        } else {
            FrameServer.PillPlacementFailed
                .toByteArray()
                .sendTo(this)
        }
    }

    private fun handlePosition(owner: Owner, frameClient: FrameClient.Position) {
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

                clients.forEach { (_, client) ->
                    serverFrame.sendTo(client)
                }
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

                clients.forEach { (_, client) ->
                    serverFrame.sendTo(client)
                }
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

            clients.forEach { (_, client) ->
                serverFrame.sendTo(client)
            }
        }
    }

    private fun Owner.cleanup(session: DefaultWebSocketServerSession) {
        clients.remove(this)

        // drop pills
        bmap.pills.forEachIndexed { index, pill ->
            if (pill.owner == int) {
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

                clients.forEach { (_, client) ->
                    frameServer.sendTo(client)
                }
            }
        }

        // neutralize bases
        bmap.bases.forEachIndexed { index, base ->
            if (base.owner == int) {
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

                clients.forEach { (_, client) ->
                    frameServer.sendTo(client)
                }
            }
        }

        val disconnectSignal = FrameServer.Signal
            .Disconnect(from = this)
            .toByteArray()

        clients.forEach { (_, client) ->
            disconnectSignal.sendTo(client)
        }

        session.launch {
            session.close()
        }
    }

    private fun ByteArray.sendTo(session: DefaultWebSocketServerSession) {
        session.launch {
            session.send(this@sendTo)
        }
    }
}
