package client

import adapters.HTMLCanvasElementAdapter
import adapters.RTCPeerConnectionAdapter
import assert.never
import bmap.BORDER
import bmap.Bmap
import bmap.BmapCode
import bmap.Entity
import bmap.TerrainTile
import bmap.WORLD_HEIGHT
import bmap.WORLD_WIDTH
import frame.FrameClient
import frame.FrameServer
import frame.Owner
import frame.toFrame
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.protobuf.ProtoBuf
import math.M4
import math.V2
import math.V2_ORIGIN
import math.add
import math.dirToVec
import math.orthographicProj2d
import math.scale
import math.x
import math.y
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import kotlin.math.max
import kotlin.random.Random

class GameImpl(
    private val scope: CoroutineScope,
    override val sendChannel: SendChannel<Frame>,
    private val receiveChannel: ReceiveChannel<Frame>,
    private val control: Control,
    private val canvas: HTMLCanvasElementAdapter,
    private val tileProgram: Deferred<TileProgram>,
    private val spriteProgram: Deferred<SpriteProgram>,
    private val tileArray: ImageTileArray,
    override val owner: Owner,
    override val bmap: Bmap,
    private val bmapCode: BmapCode,
) : Game, KoinComponent {
    override val random = Random.Default
    override var center: V2 = V2_ORIGIN
    private val frameServerFlow = MutableSharedFlow<FrameServer>()
    private val frameRegulator: MutableSet<Float> = mutableSetOf()
    private val buildQueue: MutableList<BuildOp> = mutableListOf()
    override val zoomLevel: Float = 2f

    override val tank: Tank? get() = gameProcesses.filterIsInstance<Tank>().firstOrNull()
    private val builder: Builder? get() = gameProcesses.filterIsInstance<Builder>().firstOrNull()
    private val shells: List<Shell> get() = gameProcesses.filterIsInstance<Shell>()

    private val gameProcesses: MutableList<GameProcess> = mutableListOf(LogicGameProcess(get()) {
        // creates tank on first frame
        tickChannel.receive().apply {
            set(get<Tank> { parametersOf(true) })
        }
    })

    init {
        launchReceiverFlow(scope)
        launchServerFlow(scope)
        launchGameLoop(scope)
    }

//    private suspend fun treeHarvest(x: Int, y: Int) {
//        buildQueue.add(BuildOp.Terrain(Terrain.Grass3, x, y))
//
//        ProtoBuf
//            .encodeToByteArray(
//                FrameClient.serializer(),
//                FrameClient.TerrainBuild(
//                    terrain = Terrain.Grass3,
//                    x = x,
//                    y = y,
//                ),
//            )
//            .let { Frame.Binary(fin = true, it) }
//            .let { sendChannel.send(it) }
//    }

    override suspend fun buildTerrain(x: Int, y: Int, t: TerrainTile, result: (Boolean) -> Unit) {
        buildQueue.add(BuildOp.Terrain(t, x, y, result))

        FrameClient
            .TerrainBuild(
                terrain = t,
                x = x,
                y = y,
            )
            .toFrame()
            .let { sendChannel.send(it) }
    }

    override suspend fun mineTerrain(x: Int, y: Int) {
        bmap.mine(x, y)
        tileArray.update(x, y)

        FrameClient
            .TerrainMine(
                x = x,
                y = y,
            )
            .toFrame()
            .let { sendChannel.send(it) }
    }

//    private suspend fun pillRepair(index: Int, material: Int) {
//        val pill = bmap.pills[index]
//        buildQueue.add(BuildOp.PillRepair(index, material))
//
//        ProtoBuf
//            .encodeToByteArray(
//                FrameClient.serializer(),
//                FrameClient.PillRepair(
//                    index,
//                    owner = pill.owner,
//                    x = pill.x,
//                    y = pill.y,
//                    material = material,
//                ),
//            )
//            .let { Frame.Binary(fin = true, it) }
//            .let { sendChannel.send(it) }
//    }

//    private suspend fun pillPlacement(index: Int, x: Int, y: Int, material: Int) {
//        buildQueue.add(BuildOp.PillPlacement)
//
//        ProtoBuf
//            .encodeToByteArray(
//                FrameClient.serializer(),
//                FrameClient.PillPlacement(
//                    index,
//                    x = x,
//                    y = y,
//                    material = material,
//                ),
//            )
//            .let { Frame.Binary(fin = true, it) }
//            .let { sendChannel.send(it) }
//    }

    private fun Tick.render(tileProgram: TileProgram, spriteProgram: SpriteProgram) {
        val windowWidth: Float = (canvas.width.toFloat() / TILE_PIXEL_WIDTH.toFloat()) / zoomLevel
        val windowHeight: Float = (canvas.height.toFloat() / TILE_PIXEL_HEIGHT.toFloat()) / zoomLevel

        val clipMatrix: M4 =
            orthographicProj2d(
                left = center.x - (windowWidth / 2f),
                right = center.x + (windowWidth / 2f),
                bottom = center.y - (windowHeight / 2f),
                top = center.y + (windowHeight / 2f),
            )

        tileProgram(clipMatrix, tileArray)
        val sprites = mutableListOf<SpriteInstance>()

        builder?.run {
            SpriteInstance(
                x = position.x,
                y = position.y,
                sprite = when (((2 * frameCount) / ticksPerSec.toInt()).mod(2)) {
                    0 -> Sprite.Lgm0
                    1 -> Sprite.Lgm1
                    else -> never()
                },
            ).run { sprites.add(this) }
        }

        for ((_, peer) in peers) {
            val tank = peer.tank

            if (tank != null) {
                SpriteInstance(
                    x = tank.positionX,
                    y = tank.positionY,
                    sprite = run { if (tank.onBoat) Sprite.TankEnemyBoat0 else Sprite.TankEnemy0 }
                        .withBearing(tank.bearing),
                ).let { sprites.add(it) }
            }

            for (shell in peer.shells) {
                SpriteInstance(
                    x = shell.positionX,
                    y = shell.positionY,
                    sprite = Sprite.Shell0.withBearing(shell.bearing),
                ).let { sprites.add(it) }
            }

            val builder = peer.builder

            if (builder != null) {
                SpriteInstance(
                    x = builder.positionX,
                    y = builder.positionY,
                    sprite = Sprite.Lgm0,
                ).let { sprites.add(it) }
            }
        }

        tank?.run {
            SpriteInstance(
                x = position.x,
                y = position.y,
                sprite = (if (onBoat) Sprite.TankBoat0 else Sprite.Tank0).withBearing(bearing),
            ).let { sprites.add(it) }

            val reticulePosition = position.add(dirToVec(bearing).scale(sightRange))

            SpriteInstance(
                x = reticulePosition.x,
                y = reticulePosition.y,
                sprite = Sprite.Reticule,
            ).let { sprites.add(it) }
        }

        for (shell in shells) {
            SpriteInstance(
                x = shell.position.x,
                y = shell.position.y,
                sprite = Sprite.Shell0.withBearing(shell.bearing),
            ).let { sprites.add(it) }
        }

        spriteProgram(clipMatrix, sprites)
    }

    override suspend fun terrainDamage(x: Int, y: Int) {
        bmap.damage(x, y)
        tileArray.update(x, y)

        FrameClient
            .TerrainDamage(
                code = bmapCode[x, y],
                x = x,
                y = y,
            )
            .toFrame()
            .run { sendChannel.send(this) }
    }

    override suspend fun baseDamage(index: Int) {
        val base = bmap.bases[index]
        base.armor = max(0, base.armor - 8)

        FrameClient
            .BaseDamage(
                index = index,
                code = base.code,
            )
            .toFrame()
            .run { sendChannel.send(this) }
    }

    override suspend fun pillDamage(index: Int) {
        val pill = bmap.pills[index]
        pill.armor = max(0, pill.armor - 1)
        tileArray.update(pill.x, pill.y)

        FrameClient
            .PillDamage(
                index = index,
                code = pill.code,
                x = pill.x,
                y = pill.y,
            )
            .toFrame()
            .run { sendChannel.send(this) }
    }

    override operator fun get(x: Int, y: Int): Entity {
        for (index in bmap.pills.indices) {
            val pill = bmap.pills[index]
            if (pill.x == x &&
                pill.y == y &&
                pill.isPlaced
            ) {
                return Entity.Pill(pill)
            }
        }

        for (index in bmap.bases.indices) {
            val base = bmap.bases[index]
            if (base.x == x &&
                base.y == y
            ) {
                return Entity.Base(bmap.bases[index])
            }
        }

        return Entity.Terrain(bmap[x, y])
    }

    private fun launchGameLoop(scope: CoroutineScope): Job = scope.launch(CoroutineName("launchGameLoop")) {
        for (frameCount in 0..Int.MAX_VALUE) {
            gameLoop(frameCount)
        }
    }

    private suspend fun gameLoop(frameCount: Int) {
        val time = awaitAnimationFrame()
        frameRegulator.removeAll { time - it > 1000.0 }
        frameRegulator.add(time)
        val ticksPerSec = max(1, frameRegulator.size).toFloat()

        val tick = Tick(
            frameCount = frameCount,
            control = control.getControlState(),
            ticksPerSec = ticksPerSec,
            delta = 1f / ticksPerSec,
            gameProcessesIterator = gameProcesses.listIterator(),
        )

        tick.stepGameProcesses()
        tick.handleMouseEvents()
        tick.sendPeerUpdate()
        tick.render(tileProgram.await(), spriteProgram.await())
    }

    private suspend fun Tick.stepGameProcesses() {
        for (gameProcess in this) {
            gameProcess.step(this)
        }
    }

    private fun Tick.sendPeerUpdate() {
        if (peers.isEmpty()) return
        // only send to one peer per tick
        val dataChannel = peers.values.toList()[frameCount.mod(peers.size)].dataChannel

        if (dataChannel.readyState == "open") {
            dataChannel.send(
                PeerUpdate(
                    tank = tank?.let { tank ->
                        PeerTank(
                            positionX = tank.position.x,
                            positionY = tank.position.y,
                            bearing = tank.bearing,
                            onBoat = tank.onBoat,
                        )
                    },
                    shells = shells.map {
                        PeerShell(
                            positionX = it.position.x,
                            positionY = it.position.y,
                            bearing = it.bearing,
                        )
                    },
                    builder = builder?.let { builder ->
                        PeerBuilder(
                            positionX = builder.position.x,
                            positionY = builder.position.y,
                        )
                    },
                ).toHexString(),
            )
        }
    }

    private fun Tick.handleMouseEvents() {
        val devicePixelRatio = getDevicePixelRatio()

        when (val mouse = control.mouseEvent) {
            // updates viewport
            is MouseEvent.Drag -> {
                center.x -= ((mouse.dx.toFloat() / 16f) / zoomLevel) * devicePixelRatio
                center.y += ((mouse.dy.toFloat() / 16f) / zoomLevel) * devicePixelRatio
            }
            // builder actions
            is MouseEvent.Up -> {
                fun Int.toRow(): Int {
                    return (WORLD_WIDTH.toFloat() - (((canvas.clientHeight.toFloat() / 2f) - this) * (devicePixelRatio / (zoomLevel * 16f))) - center.y).toInt()
                }

                fun Int.toCol(): Int {
                    return (((toFloat() - (canvas.clientWidth.toFloat() / 2f)) * (devicePixelRatio / (zoomLevel * 16f))) + center.x).toInt()
                }

                val row = mouse.y.toRow()
                val col = mouse.x.toCol()
                val downRow = mouse.downY.toRow()
                val downCol = mouse.downX.toCol()

                // make sure mouse down and up are in the same square
                if (downCol == col && downRow == row) {
                    tank?.run {
                        if (col in BORDER..<(WORLD_WIDTH - BORDER) &&
                            row in BORDER..<(WORLD_HEIGHT - BORDER)
                        ) {
                            when (control.builderMode) {
                                BuilderMode.Tree -> when (bmap[col, row]) {
                                    TerrainTile.Tree -> BuilderMission.HarvestTree(col, row)
                                    else -> null
                                }

                                BuilderMode.Road -> when (bmap[col, row]) {
                                    TerrainTile.Grass0,
                                    TerrainTile.Grass1,
                                    TerrainTile.Grass2,
                                    TerrainTile.Grass3,
                                    TerrainTile.Swamp0,
                                    TerrainTile.Swamp1,
                                    TerrainTile.Swamp2,
                                    TerrainTile.Swamp3,
                                    TerrainTile.Road,
                                    TerrainTile.Crater,
                                    TerrainTile.Rubble0,
                                    TerrainTile.Rubble1,
                                    TerrainTile.Rubble2,
                                    TerrainTile.Rubble3,
                                    -> BuilderMission.BuildRoad(col, row)

                                    TerrainTile.Tree -> BuilderMission.HarvestTree(col, row)
                                    else -> null
                                }

                                BuilderMode.Wall -> when (bmap[col, row]) {
                                    TerrainTile.Grass0,
                                    TerrainTile.Grass1,
                                    TerrainTile.Grass2,
                                    TerrainTile.Grass3,
                                    TerrainTile.Swamp0,
                                    TerrainTile.Swamp1,
                                    TerrainTile.Swamp2,
                                    TerrainTile.Swamp3,
                                    TerrainTile.Road,
                                    TerrainTile.Crater,
                                    TerrainTile.Rubble0,
                                    TerrainTile.Rubble1,
                                    TerrainTile.Rubble2,
                                    TerrainTile.Rubble3,
                                    TerrainTile.WallDamaged0,
                                    TerrainTile.WallDamaged1,
                                    TerrainTile.WallDamaged2,
                                    TerrainTile.WallDamaged3,
                                    -> BuilderMission.BuildWall(col, row)

                                    TerrainTile.Tree -> BuilderMission.HarvestTree(col, row)
                                    TerrainTile.River -> BuilderMission.BuildBoat(col, row)
                                    else -> null
                                }

                                BuilderMode.Pill -> {
                                    null
//                                var index =
//                                    bmap.pills.indexOfFirst { it.isPlaced && it.x == sqrX && it.y == sqrY }
//
//                                if (index >= 0) {
//                                    val pill = bmap.pills[index]
//
//                                    if (tankMaterial > 0) {
//                                        val material = min(tankMaterial, (pill.armor + 1) / 4)
//                                        tankMaterial -= material
////                                        pillRepair(index, material = material)
//                                    }
//                                } else {
//                                    index = bmap.pills.indexOfFirst { it.isPlaced.not() && it.owner == owner }
//
//                                    if (index >= 0 && tankMaterial >= pillPerMaterial) {
//                                        tankMaterial =
//                                            (tankMaterial - pillPerMaterial).clampRange(0, tankMaterialMax)
////                                        pillPlacement(index, x = sqrX, y = sqrY, material = pillPerMaterial)
//                                    }
//                                }
                                }

                                BuilderMode.Mine -> when (bmap[col, row]) {
                                    TerrainTile.Tree,
                                    TerrainTile.Grass0,
                                    TerrainTile.Grass1,
                                    TerrainTile.Grass2,
                                    TerrainTile.Grass3,
                                    TerrainTile.Swamp0,
                                    TerrainTile.Swamp1,
                                    TerrainTile.Swamp2,
                                    TerrainTile.Swamp3,
                                    TerrainTile.Road,
                                    TerrainTile.Crater,
                                    TerrainTile.Rubble0,
                                    TerrainTile.Rubble1,
                                    TerrainTile.Rubble2,
                                    TerrainTile.Rubble3,
                                    -> BuilderMission.PlaceMine(col, row)

                                    else -> null
                                }
                            }?.run {
                                if (hasBuilder) {
                                    if (builder != null) throw IllegalStateException("only one builder should exist at a time")
                                    add(get<Builder> { parametersOf(position, this) })
                                    hasBuilder = false
                                } else {
                                    nextBuilderMission = this
                                }
                            }
                        }
                    }
                }
            }

            null -> {}
        }
    }

    private val peers: MutableMap<Owner, Peer> = mutableMapOf()

    private fun launchReceiverFlow(scope: CoroutineScope): Job {
        // emit decoded server frames to game
        return scope.launch(CoroutineName("launchReceiverFlow")) {
            receiveChannel
                .consumeAsFlow()
                .transform { frame ->
                    when (frame) {
                        is Frame.Binary -> emit(frame)
                        is Frame.Text -> throw IllegalStateException("unexpected text frame")
                        is Frame.Close -> throw IllegalStateException("connection closed by server")
                        is Frame.Ping -> Unit
                        is Frame.Pong -> Unit
                        else -> never()
                    }
                }
                .map { ProtoBuf.decodeFromByteArray(FrameServer.serializer(), it.readBytes()) }
                .map { frameServerFlow.emit(it) }
                .collect()
        }
    }

    private fun launchServerFlow(scope: CoroutineScope): Job {
        return scope.launch(CoroutineName("launchServerFlow")) {
            frameServerFlow
                .map { frameServer ->
                    when (frameServer) {
                        is FrameServer.Signal -> {
                            val peerConnection = getPeer(frameServer.from).peerConnection

                            when (frameServer) {
                                is FrameServer.Signal.NewPeer -> {
                                    // RTCPeerConnection created above
                                }

                                is FrameServer.Signal.Offer -> {
                                    peerConnection.setRemoteDescription(frameServer.sessionDescription)
                                    val localDescription = peerConnection.createAnswer()
                                    peerConnection.setLocalDescription(localDescription)

                                    FrameClient.Signal
                                        .Answer(
                                            owner = frameServer.from,
                                            sessionDescription = peerConnection.localDescription!!,
                                        )
                                        .toFrame()
                                        .run { sendChannel.send(this) }
                                }

                                is FrameServer.Signal.Answer -> {
                                    peerConnection.setRemoteDescription(frameServer.sessionDescription)
                                }

                                is FrameServer.Signal.IceCandidate -> {
                                    peerConnection.addIceCandidate(frameServer.iceCandidate)
                                }

                                is FrameServer.Signal.Disconnect -> {
                                    peers.remove(frameServer.from)
                                }
                            }
                        }

                        is FrameServer.TerrainBuild -> {
                            // build from other players
                            bmap[frameServer.x, frameServer.y] = frameServer.terrain
                            bmapCode.inc(frameServer.x, frameServer.y)
                            tileArray.update(frameServer.x, frameServer.y)
                        }

                        is FrameServer.TerrainBuildSuccess -> {
                            val buildOp = buildQueue.removeAt(0) as BuildOp.Terrain
                            bmap[buildOp.x, buildOp.y] = buildOp.terrain
                            bmapCode.inc(buildOp.x, buildOp.y)
                            tileArray.update(buildOp.x, buildOp.y)
                            buildOp.result(true)
                        }

                        is FrameServer.TerrainBuildFailed -> {
                            val buildOp = buildQueue.removeAt(0) as BuildOp.Terrain
                            buildOp.result(false)
                        }

                        is FrameServer.TerrainDamage -> {
                            // damage from other players
                            bmap.damage(frameServer.x, frameServer.y)
                            tileArray.update(frameServer.x, frameServer.y)
                        }

                        is FrameServer.TerrainMine -> {
                            // mines from other players
                            bmap.mine(frameServer.x, frameServer.y)
                            tileArray.update(frameServer.x, frameServer.y)
                        }

                        is FrameServer.BaseTake -> {
                            val base = bmap.bases[frameServer.index]
                            base.code++
                            base.owner = frameServer.owner
                            base.armor = frameServer.armor
                            base.shells = frameServer.shells
                            base.mines = frameServer.mines
                            tileArray.update(base.x, base.y)
                        }

                        is FrameServer.BaseDamage -> {
                            val base = bmap.bases[frameServer.index]
                            base.armor = max(0, base.armor - 8)
                        }

                        is FrameServer.PillDamage -> {
                            val pill = bmap.pills[frameServer.index]
                            pill.armor = max(0, pill.armor - 1)
                            tileArray.update(pill.x, pill.y)
                        }

                        is FrameServer.PillRepair -> {
                            val pill = bmap.pills[frameServer.index]
                            pill.armor = frameServer.armor
                            pill.code++
                            tileArray.update(pill.x, pill.y)
                        }

                        is FrameServer.PillRepairSuccess -> {
                            // buildQueue.removeAt(0) as BuildOp.PillRepair
                            // tankMaterial = (tankMaterial + frameServer.material).clampRange(0, tankMaterialMax)
                        }

                        is FrameServer.PillRepairFailed -> {
                            // val pillRepair = buildQueue.removeAt(0) as BuildOp.PillRepair
                            // tankMaterial = (tankMaterial + pillRepair.material).clampRange(0, tankMaterialMax)
                        }

                        is FrameServer.PillTake -> {
                            val pill = bmap.pills[frameServer.index]
                            pill.owner = frameServer.owner
                            pill.isPlaced = false
                            tileArray.update(pill.x, pill.y)
                        }

                        is FrameServer.PillDrop -> {
                            val pill = bmap.pills[frameServer.index]
                            pill.owner = frameServer.owner
                            pill.x = frameServer.x
                            pill.y = frameServer.y
                            pill.isPlaced = true
                            tileArray.update(pill.x, pill.y)
                        }

                        is FrameServer.PillPlacement -> {
                            val pill = bmap.pills[frameServer.index]
                            pill.armor = frameServer.armor
                            pill.x = frameServer.x
                            pill.y = frameServer.y
                            pill.isPlaced = true
                            pill.code++
                            tileArray.update(pill.x, pill.y)
                        }

                        is FrameServer.PillPlacementSuccess -> {
                            // buildQueue.removeAt(0) is BuildOp.PillPlacement
                            // Unit
                        }

                        is FrameServer.PillPlacementFailed -> {
                            // buildQueue.removeAt(0) is BuildOp.PillPlacement
                            // tankMaterial = (tankMaterial + pillPerMaterial).clampRange(0, tankMaterialMax)
                        }
                    }
                }
                .collect()
        }
    }

    private fun getPeer(from: Owner): Peer {
        return peers.getOrPut(from) {
            val peerConnection: RTCPeerConnectionAdapter = get()

            peerConnection.setOnnegotiationneeded { event ->
                println("PeerConnection.onnegotiationneeded: $from $event")

                scope.launch(CoroutineName("setOnnegotiationneeded")) {
                    val offer = peerConnection.createOffer()
                    peerConnection.setLocalDescription(offer)

                    FrameClient.Signal.Offer(
                        owner = from,
                        sessionDescription = peerConnection.localDescription!!,
                    )
                        .toFrame()
                        .run { sendChannel.send(this) }
                }
            }

            peerConnection.setOnconnectionstatechange { connectionState ->
                println("PeerConnection.onconnectionstatechange: $from $connectionState")
            }

            peerConnection.setOndatachannel { dataChannel ->
                println("peerConnection.ondatachannel: $from")

                dataChannel.setOnopen {
                    println("channel.onopen: $from")
                }

                dataChannel.setOnmessage { message ->
                    peerEventUpdate(
                        from = from,
                        peerUpdate = message.toPeerUpdate(),
                    )
                }

                dataChannel.setOnclose {
                    println("channel.onclose: $from")
                }

                dataChannel.setOnerror {
                    println("channel.onerror: $from")
                }
            }

            peerConnection.setOnicecandidate { candidate ->
                println("PeerConnection.onicecandidate: $from $candidate")
                if (candidate == null) return@setOnicecandidate

                scope.launch(CoroutineName("setOnicecandidate")) {
                    FrameClient.Signal
                        .IceCandidate(
                            owner = from,
                            iceCandidate = candidate,
                        )
                        .toFrame()
                        .run { sendChannel.send(this) }
                }
            }

            val dataChannel = peerConnection.createDataChannel(
                label = "Data Channel: $from",
                options = buildJsonObject {
                    put("negotiated", JsonPrimitive(false))
                    put("ordered", JsonPrimitive(false))
                    put("maxRetransmits", JsonPrimitive(0))
                },
            )

            dataChannel.setOnopen { event ->
                println("DataChannel.onopen: $from $event")
            }

            dataChannel.setOnmessage { event ->
                println("DataChannel.onmessage: $from $event")

                peerEventUpdate(
                    from = from,
                    peerUpdate = Json.decodeFromString(event),
                )
            }

            dataChannel.setOnclose { event ->
                println("DataChannel.onclose: $from $event")
            }

            dataChannel.setOnerror { event ->
                println("DataChannel.onerror: $from $event")
            }

            Peer(
                peerConnection = peerConnection,
                dataChannel = dataChannel,
            )
        }
    }

    private fun peerEventUpdate(from: Owner, peerUpdate: PeerUpdate) {
        val peer = peers[from] ?: throw IllegalStateException("peers[${from}] is null")
        peer.tank = peerUpdate.tank
        peer.shells = peerUpdate.shells
        peer.builder = peerUpdate.builder
    }
}
