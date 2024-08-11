package client

import client.adapters.HTMLCanvasElementAdapter
import client.adapters.RTCPeerConnectionAdapter
import common.bmap.BORDER_WIDTH
import common.bmap.Bmap
import common.bmap.BmapCode
import common.bmap.Entity
import common.bmap.TerrainTile
import common.bmap.WORLD_HEIGHT
import common.bmap.WORLD_WIDTH
import common.TILE_PIXEL_HEIGHT
import common.TILE_PIXEL_WIDTH
import common.frame.FrameClient
import common.frame.FrameServer
import common.frame.Owner
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
import client.math.M4
import client.math.V2
import client.math.dirToVec
import common.frame.frameServerSerializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import kotlin.math.max
import kotlin.math.min

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
    override var center: V2 = V2.ORIGIN
    private val frameServerFlow = MutableSharedFlow<FrameServer>()
    private val frameRegulator: MutableSet<Float> = mutableSetOf()
    private val buildQueue: MutableList<(BuildResult) -> Unit> = mutableListOf()
    override val zoomLevel: Float = 2f

    override val tank: Tank? get() = gameProcesses.filterIsInstance<Tank>().firstOrNull()
    private val builder: Builder? get() = gameProcesses.filterIsInstance<Builder>().firstOrNull()
    private val shells: List<Shell> get() = gameProcesses.filterIsInstance<Shell>()
    private val parachutes: List<Parachute> get() = gameProcesses.filterIsInstance<Parachute>()

    private val gameProcesses: MutableList<GameProcess> = mutableListOf(LogicGameProcess {
        // creates tank on first frame
        next().apply {
            set(get<Tank> { parametersOf(true) })
        }
    })

    init {
        launchReceiverFlow(scope)
        launchServerFlow(scope)
        launchGameLoop(scope)
    }

    override suspend fun ConsumerScope<Tick>.buildTerrain(
        col: Int,
        row: Int,
        terrainTile: TerrainTile,
    ): Triple<Tick, Float, BuildResult> {
        var buildResult: BuildResult? = null
        buildQueue.add { result ->
            buildResult = result
        }
        FrameClient
            .TerrainBuild(terrain = terrainTile, col = col, row = row)
            .toFrame()
            .let { sendChannel.trySend(it).getOrThrow() }
        var timeDelta = 0f
        while (true) {
            val tick = next()
            timeDelta += tick.delta
            buildResult?.run {
                if (this == BuildResult.Success) {
                    bmap[col, row] = terrainTile
                    bmapCode.inc(col, row)
                    tileArray.update(col, row)
                }
                return Triple(tick, timeDelta, this)
            }
        }
    }

    override suspend fun ConsumerScope<Tick>.mineTerrain(col: Int, row: Int): Triple<Tick, Float, BuildResult> {
        var buildResult: BuildResult? = null
        buildQueue.add { result ->
            buildResult = result
        }
        FrameClient
            .TerrainMine(col = col, row = row)
            .toFrame()
            .let { sendChannel.trySend(it).getOrThrow() }
        var timeDelta = 0f
        while (true) {
            val tick = next()
            timeDelta += tick.delta
            buildResult?.run {
                if (this == BuildResult.Success) {
                    check(bmap.mine(col, row))
                    bmapCode.inc(col, row)
                    tileArray.update(col, row)
                }
                return Triple(tick, timeDelta, this)
            }
        }
    }

    override suspend fun ConsumerScope<Tick>.placePill(
        col: Int,
        row: Int,
        pill: Int
    ): Triple<Tick, Float, BuildResult> {
        var buildResult: BuildResult? = null
        buildQueue.add { result ->
            buildResult = result
        }
        FrameClient
            .TerrainMine(col = col, row = row)
            .toFrame()
            .let { sendChannel.trySend(it).getOrThrow() }
        var timeDelta = 0f
        while (true) {
            val tick = next()
            timeDelta += tick.delta
            buildResult?.run {
                if (this == BuildResult.Success) {
                    check(bmap.mine(col, row))
                    tileArray.update(col, row)
                }
                return Triple(tick, timeDelta, this)
            }
        }
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
            M4.orthographicProj2d(
                left = center.x - (windowWidth / 2f),
                right = center.x + (windowWidth / 2f),
                bottom = center.y - (windowHeight / 2f),
                top = center.y + (windowHeight / 2f),
            )

        tileProgram(clipMatrix, tileArray)

        val sprites = buildList {
            builder?.run {
                SpriteInstance(
                    x = position.x,
                    y = position.y,
                    sprite = when (((2 * frameCount) / ticksPerSec.toInt()).mod(2)) {
                        0 -> Sprite.Lgm0
                        1 -> Sprite.Lgm1
                        else -> throw IllegalStateException("this should never happen")
                    },
                ).run { add(this) }
            }

            for ((_, peer) in peers) {
                val tank = peer.tank

                if (tank != null) {
                    SpriteInstance(
                        x = tank.x,
                        y = tank.y,
                        sprite = run { if (tank.onBoat) Sprite.TankEnemyBoat0 else Sprite.TankEnemy0 }
                            .withBearing(tank.bearing),
                    ).let { add(it) }
                }

                for (shell in peer.shells) {
                    SpriteInstance(
                        x = shell.x,
                        y = shell.y,
                        sprite = Sprite.Shell0.withBearing(shell.bearing),
                    ).let { add(it) }
                }

                val builder = peer.builder

                if (builder != null) {
                    SpriteInstance(
                        x = builder.x,
                        y = builder.y,
                        sprite = Sprite.Lgm0,
                    ).let { add(it) }
                }
            }

            tank?.run {
                SpriteInstance(
                    x = position.x,
                    y = position.y,
                    sprite = (if (onBoat) Sprite.TankBoat0 else Sprite.Tank0).withBearing(bearing),
                ).let { add(it) }

                val reticulePosition = position.add(dirToVec(bearing).scale(sightRange))

                SpriteInstance(
                    x = reticulePosition.x,
                    y = reticulePosition.y,
                    sprite = Sprite.Reticule,
                ).let { add(it) }
            }

            for (shell in shells) {
                SpriteInstance(
                    x = shell.position.x,
                    y = shell.position.y,
                    sprite = Sprite.Shell0.withBearing(shell.bearing),
                ).let { add(it) }
            }

            for (parachute in parachutes) {
                SpriteInstance(
                    x = parachute.position.x,
                    y = parachute.position.y,
                    sprite = Sprite.Parachute,
                ).let { add(it) }
            }
        }

        spriteProgram(clipMatrix, sprites)
    }

    override fun terrainDamage(col: Int, row: Int) {
        bmap.damage(col, row)
        tileArray.update(col, row)
        killBuilderInTile(col, row)

        FrameClient
            .TerrainDamage(
                code = bmapCode[col, row],
                col = col,
                row = row,
            )
            .toFrame()
            .run { sendChannel.trySend(this).getOrThrow() }
    }

    private fun killBuilderInTile(col: Int, row: Int) {
        val listIterator = gameProcesses.listIterator()
        for (process in listIterator) {
            if (process is Builder &&
                process.position.x.toInt() == col &&
                process.position.y.toInt() == row &&
                process.consumer.done.not()
            ) {
                process.consumer.finish(BuilderImpl.BuilderKilled(listIterator))
            }
        }
    }

    override fun baseDamage(index: Int) {
        val base = bmap.bases[index]
        base.armor = max(0, base.armor - 8)

        FrameClient
            .BaseDamage(
                index = index,
                code = base.code,
            )
            .toFrame()
            .run { sendChannel.trySend(this).getOrThrow() }
    }

    override fun pillDamage(index: Int) {
        val pill = bmap.pills[index]
        pill.armor = max(0, pill.armor - 1)
        tileArray.update(pill.col, pill.row)

        FrameClient
            .PillDamage(
                index = index,
                code = pill.code,
                col = pill.col,
                row = pill.row,
            )
            .toFrame()
            .run { sendChannel.trySend(this).getOrThrow() }
    }

    override operator fun get(col: Int, row: Int): Entity {
        for (index in bmap.pills.indices) {
            val pill = bmap.pills[index]
            if (pill.col == col &&
                pill.row == row &&
                pill.isPlaced
            ) {
                return Entity.Pill(pill)
            }
        }

        for (index in bmap.bases.indices) {
            val base = bmap.bases[index]
            if (base.col == col &&
                base.row == row
            ) {
                return Entity.Base(bmap.bases[index])
            }
        }

        return Entity.Terrain(bmap[col, row])
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

    private fun Tick.stepGameProcesses() {
        for (gameProcess in this) {
            if (gameProcess.consumer.done.not()) {
                gameProcess.consumer.yield(this)
            }
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
                            x = tank.position.x,
                            y = tank.position.y,
                            bearing = tank.bearing,
                            onBoat = tank.onBoat,
                        )
                    },
                    shells = shells.map {
                        PeerShell(
                            x = it.position.x,
                            y = it.position.y,
                            bearing = it.bearing,
                        )
                    },
                    builder = builder?.let { builder ->
                        PeerBuilder(
                            x = builder.position.x,
                            y = builder.position.y,
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

                val row = mouse.row.toRow()
                val col = mouse.col.toCol()
                val downRow = mouse.downRow.toRow()
                val downCol = mouse.downCol.toCol()

                // make sure mouse down and up are in the same square
                val tank = tank
                if (tank != null &&
                    downCol == col &&
                    downRow == row &&
                    col in BORDER_WIDTH..<(WORLD_WIDTH - BORDER_WIDTH) &&
                    row in BORDER_WIDTH..<(WORLD_HEIGHT - BORDER_WIDTH)
                ) {
                    if (tank.hasBuilder) {
                        control.builderMode.tryBuilderAction(tank, col, row)?.run {
                            if (builder != null) throw IllegalStateException("only one builder should exist at a time")
                            add(this)
                        }
                    } else {
                        tank.setNextBuilderMission(
                            NextBuilderMission(
                                builderMode = control.builderMode,
                                col = col,
                                row = row,
                            )
                        )
                    }
                }
            }

            null -> {}
        }
    }

    override fun BuilderMode.tryBuilderAction(tank: Tank, col: Int, row: Int): Builder? = when (this) {
        BuilderMode.Tree -> when (bmap[col, row]) {
            TerrainTile.Tree -> tryCreatingBuilder(
                tank = tank,
                builderMission = BuilderMission.HarvestTree(col, row),
            )

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
            TerrainTile.Crater,
            TerrainTile.Rubble0,
            TerrainTile.Rubble1,
            TerrainTile.Rubble2,
            TerrainTile.Rubble3,
                -> tryCreatingBuilder(
                tank = tank,
                builderMission = BuilderMission.BuildRoad(col = col, row = row),
            )

            TerrainTile.Tree -> tryCreatingBuilder(
                tank = tank,
                builderMission = BuilderMission.HarvestTree(col = col, row = row),
            )

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
                -> tryCreatingBuilder(
                tank = tank,
                builderMission = BuilderMission.BuildWall(
                    col = col,
                    row = row,
                    material = BuilderImpl.WALL_MATERIAL,
                ),
            )

            TerrainTile.WallDamaged1 -> tryCreatingBuilder(
                tank = tank,
                builderMission = BuilderMission.BuildWall(
                    col = col,
                    row = row,
                    material = BuilderImpl.WALL_MATERIAL,
                ),
            )

            TerrainTile.WallDamaged2 -> tryCreatingBuilder(
                tank = tank,
                builderMission = BuilderMission.BuildWall(
                    col = col,
                    row = row,
                    material = BuilderImpl.WALL_MATERIAL,
                ),
            )

            TerrainTile.WallDamaged3 -> tryCreatingBuilder(
                tank = tank,
                builderMission = BuilderMission.BuildWall(
                    col = col,
                    row = row,
                    material = BuilderImpl.WALL_MATERIAL,
                ),
            )

            TerrainTile.Tree -> tryCreatingBuilder(
                tank = tank,
                builderMission = BuilderMission.HarvestTree(col = col, row = row),
            )

            TerrainTile.River -> tryCreatingBuilder(
                tank = tank,
                builderMission = BuilderMission.BuildBoat(col = col, row = row),
            )

            else -> null
        }

        BuilderMode.Pill -> null
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
                -> tryCreatingBuilder(
                tank = tank,
                builderMission = BuilderMission.PlaceMine(col = col, row = row),
            )

            else -> null
        }
    }

    private fun tryCreatingBuilder(
        tank: Tank,
        builderMission: BuilderMission,
    ): Builder? {
        return if (tank.material >= builderMission.material && tank.mines >= builderMission.mines) {
            val mat = min(tank.material, builderMission.material)
            tank.material -= mat
            setMaterialStatusBar(tank.material.toFloat() / TankImpl.TANK_MATERIAL_MAX)
            tank.mines -= builderMission.mines
            setMinesStatusBar(tank.mines.toFloat() / TankImpl.TANK_MINES_MAX)
            tank.hasBuilder = false
            get<Builder> { parametersOf(tank.position, builderMission, mat, builderMission.mines, null) }
        } else {
            // TODO: print message
            null
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
                        else -> throw IllegalStateException("should never happen")
                    }
                }
                .map { ProtoBuf.decodeFromByteArray(frameServerSerializer, it.readBytes()) }
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
                            bmap[frameServer.col, frameServer.row] = frameServer.terrain
                            bmapCode.inc(frameServer.col, frameServer.row)
                            tileArray.update(frameServer.col, frameServer.row)
                        }

                        is FrameServer.TerrainBuildSuccess -> {
                            buildQueue.removeAt(0)(BuildResult.Success)
                        }

                        is FrameServer.TerrainBuildFailed -> {
                            buildQueue.removeAt(0)(BuildResult.Failed)
                        }

                        is FrameServer.TerrainBuildMined -> {
                            buildQueue.removeAt(0)(BuildResult.Mined)
                        }

                        is FrameServer.MinePlaceSuccess -> {
                            buildQueue.removeAt(0)(BuildResult.Success)
                        }

                        is FrameServer.MinePlaceFailed -> {
                            buildQueue.removeAt(0)(BuildResult.Failed)
                        }

                        is FrameServer.MinePlaceMined -> {
                            buildQueue.removeAt(0)(BuildResult.Mined)
                        }

                        is FrameServer.TerrainDamage -> {
                            // damage from other players
                            bmap.damage(frameServer.col, frameServer.row)
                            tileArray.update(frameServer.col, frameServer.row)
                        }

                        is FrameServer.TerrainMine -> {
                            // mines from other players
                            bmap.mine(frameServer.col, frameServer.row)
                            tileArray.update(frameServer.col, frameServer.row)
                        }

                        is FrameServer.BaseTake -> {
                            val base = bmap.bases[frameServer.index]
                            base.code++
                            base.owner = frameServer.owner
                            base.armor = frameServer.armor
                            base.shells = frameServer.shells
                            base.mines = frameServer.mines
                            tileArray.update(base.col, base.row)
                        }

                        is FrameServer.BaseDamage -> {
                            val base = bmap.bases[frameServer.index]
                            base.armor = max(0, base.armor - 8)
                        }

                        is FrameServer.PillDamage -> {
                            val pill = bmap.pills[frameServer.index]
                            pill.armor = max(0, pill.armor - 1)
                            tileArray.update(pill.col, pill.row)
                        }

                        is FrameServer.PillRepair -> {
                            val pill = bmap.pills[frameServer.index]
                            pill.armor = frameServer.armor
                            pill.code++
                            tileArray.update(pill.col, pill.row)
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
                            tileArray.update(pill.col, pill.row)
                        }

                        is FrameServer.PillDrop -> {
                            val pill = bmap.pills[frameServer.index]
                            pill.owner = frameServer.owner
                            pill.col = frameServer.col
                            pill.row = frameServer.row
                            pill.isPlaced = true
                            tileArray.update(pill.col, pill.row)
                        }

                        is FrameServer.PillPlacement -> {
                            val pill = bmap.pills[frameServer.index]
                            pill.armor = frameServer.armor
                            pill.col = frameServer.col
                            pill.row = frameServer.row
                            pill.isPlaced = true
                            pill.code++
                            tileArray.update(pill.col, pill.row)
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
