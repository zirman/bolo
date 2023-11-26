@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package client

import bmap.Bmap
import bmap.BmapCode
import bmap.Entity
import bmap.TerrainTile
import bmap.border
import bmap.isSolid
import bmap.worldHeight
import bmap.worldWidth
import frame.FrameClient
import frame.FrameServer
import frame.Owner
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAnimationFrame
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import math.M4
import math.V2
import math.add
import math.orthographicProj2d
import math.scale
import math.v2Origin
import math.x
import math.y
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_TEST
import org.khronos.webgl.WebGLRenderingContext.Companion.ONE_MINUS_SRC_ALPHA
import org.khronos.webgl.WebGLRenderingContext.Companion.SRC_ALPHA
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf
import org.w3c.dom.HTMLCanvasElement
import util.dirToVec
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.math.max
import kotlin.random.Random

data class Tick(
    val control: ControlState,
    val ticksPerSec: Float,
    val delta: Float,
)

sealed interface BuildOp {
    data class Terrain(
        val terrain: TerrainTile,
        val x: Int,
        val y: Int,
        val result: (Boolean) -> Unit,
    ) : BuildOp

    data object PillPlacement : BuildOp

    data class PillRepair(
        val index: Int,
        val material: Int,
    ) : BuildOp
}

interface Game {
    val bmap: Bmap
    val random: Random
    val owner: Owner
    val sendChannel: SendChannel<Frame>
    var center: V2
    fun launchTank()
    fun launchShell(bearing: Float, onBoat: Boolean, startPosition: V2, sightRange: Float)
    fun launchBuilder(startPosition: V2, targetX: Int, targetY: Int, buildOp: BuilderMission)
    suspend fun terrainDamage(x: Int, y: Int)
    suspend fun buildTerrain(x: Int, y: Int, t: TerrainTile, result: (Boolean) -> Unit)
    suspend fun mineTerrain(x: Int, y: Int)
    suspend fun baseDamage(index: Int)
    suspend fun pillDamage(index: Int)
    var tank: Tank?
    val builder: Builder?
    var isBuilderInTank: Boolean
    operator fun get(x: Int, y: Int): Entity
}

class GameImpl(
    private val scope: CoroutineScope,
    private val gl: WebGLRenderingContext,
    private val canvas: HTMLCanvasElement,
    private val tileProgram: Deferred<TileProgram>,
    private val spriteProgram: Deferred<SpriteProgram>,
    override val sendChannel: SendChannel<Frame>,
    override val owner: Owner,
    override val bmap: Bmap,
    private val receiveChannel: ReceiveChannel<Frame>,
    private val bmapCode: BmapCode,
) : Game, KoinComponent {
    override val random = Random(Date.now().toInt())
    override var center: V2 = v2Origin
    override var isBuilderInTank: Boolean = true

    private val frameServerFlow = MutableSharedFlow<FrameServer>()

    private val frameRegulator: MutableSet<Double> = mutableSetOf()

    private val tileArray: ImageTileArray = ImageTileArray(bmap, owner)
    private val buildQueue: MutableList<BuildOp> = mutableListOf()

    private val zoomLevel: Float = 2f
    override var tank: Tank? = null
    private val shells = mutableListOf<Shell>()
    private val builders = mutableListOf<Builder>()

    init {
        launchReceiverFlow(scope)
        launchServerFlow(scope)
        launchGameLoop(scope)
        // allow Game to finish instantiating before injecting game into Tank
        scope.launch { launchTank() }
    }

    override val builder get() = builders.firstOrNull()

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

    private fun render(frameCount: Int, tileProgram: TileProgram, spriteProgram: SpriteProgram) {
        gl.blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
        gl.disable(DEPTH_TEST)

        val windowWidth: Float = (canvas.width.toFloat() / tilePixelWidth.toFloat()) / zoomLevel
        val windowHeight: Float = (canvas.height.toFloat() / tilePixelHeight.toFloat()) / zoomLevel

        val clipMatrix: M4 =
            orthographicProj2d(
                left = center.x - (windowWidth / 2f),
                right = center.x + (windowWidth / 2f),
                bottom = center.y - (windowHeight / 2f),
                top = center.y + (windowHeight / 2f),
            )

        try {
            tileProgram(clipMatrix, tileArray)

            val sprites = mutableListOf<SpriteInstance>()

            for (builder in builders) {
                SpriteInstance(
                    x = builder.position.x,
                    y = builder.position.y,
                    sprite = Sprite.Lgm0,
                ).let { sprites.add(it) }
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

            // only send to one peer per tick
            if (peers.isNotEmpty()) {
                val dataChannel = peers.values.toList()[frameCount % peers.size].dataChannel

                if (dataChannel.asDynamic().readyState == "open") {
                    dataChannel.asDynamic().send(
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
        } catch (error: Throwable) {
            error.printStackTrace()
            throw error
        }
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

    private fun launchGameLoop(scope: CoroutineScope): Job = scope.launch {
        try {
            for (frameCount in 0..Int.MAX_VALUE) {
                renderFrame(frameCount)
            }
        } catch (error: Throwable) {
            currentCoroutineContext().ensureActive()
            error.printStackTrace()
            throw error
        }
    }

    private suspend fun renderFrame(frameCount: Int) {
        val time = window.awaitAnimationFrame()
        frameRegulator.removeAll { time - it > 1000.0 }
        frameRegulator.add(time)
        val ticksPerSec = max(1, frameRegulator.size).toFloat()

        val tick = Tick(
            control = Control.getControlState(),
            ticksPerSec = ticksPerSec,
            delta = 1f / ticksPerSec,
        )

        handleMouseEvents(tick)

        tank?.run {
            if (job.isCompleted) {
                tank = null
            } else {
                resumeWith(tick)
            }
        }

        shells.removeAll { it.job.isCompleted }

        for (shell in shells) {
            shell.resumeWith(tick)
        }

        builders.removeAll { it.job.isCompleted }

        for (builder in builders) {
            builder.resumeWith(tick)
        }

        render(frameCount, tileProgram.await(), spriteProgram.await())
    }

    private fun handleMouseEvents(tick: Tick) {
        val devicePixelRatio = window.devicePixelRatio

        when (val mouse = tick.control.mouse) {
            // updates viewport
            is Mouse.Drag -> {
                center.x -= ((mouse.x.toFloat() / 16f) / zoomLevel) * devicePixelRatio.toFloat()
                center.y += ((mouse.y.toFloat() / 16f) / zoomLevel) * devicePixelRatio.toFloat()
            }
            // builder actions
            is Mouse.Up -> {
                val sqrX: Int =
                    (((mouse.x.toFloat() - (canvas.clientWidth.toFloat() / 2f)) * (devicePixelRatio.toFloat() / (zoomLevel * 16f))) + center.x).toInt()

                val sqrY: Int =
                    (worldWidth.toFloat() - (((canvas.clientHeight.toFloat() / 2f) - mouse.y) * (devicePixelRatio.toFloat() / (zoomLevel * 16f))) - center.y).toInt()

                if (isBuilderInTank &&
                    sqrX in border..<(worldWidth - border) &&
                    sqrY in border..<(worldHeight - border)
                ) {
                    when (tick.control.builderMode) {
                        is BuilderMode.Tree -> {
                            if (bmap[sqrX, sqrY] == TerrainTile.Tree) {
                                tank?.let { tank ->
                                    launchBuilder(tank.position, sqrX, sqrY, BuilderMission.HarvestTree)
                                    isBuilderInTank = false
                                }
                            }
                        }

                        is BuilderMode.Road -> {
                            // TODO: proper check
                            if (bmap[sqrX, sqrY] == TerrainTile.Grass3) {
                                tank?.let { tank ->
                                    launchBuilder(tank.position, sqrX, sqrY, BuilderMission.BuildRoad)
                                    isBuilderInTank = false
                                }
                            }
                        }

                        is BuilderMode.Wall -> {
                            if (bmap[sqrX, sqrY] == TerrainTile.Grass3) {
                                tank?.let { tank ->
                                    launchBuilder(tank.position, sqrX, sqrY, BuilderMission.BuildWall)
                                    isBuilderInTank = false
                                }
                            }
                        }

                        is BuilderMode.Pill -> {
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

                        is BuilderMode.Mine -> {
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
        return receiveChannel
            .consumeAsFlow()
            .transform { frame ->
                when (frame) {
                    is Frame.Binary -> emit(frame)
                    is Frame.Text -> throw IllegalStateException("unexpected text frame")
                    is Frame.Close -> throw IllegalStateException("connection closed by server")
                    is Frame.Ping -> Unit
                    is Frame.Pong -> Unit
                }
            }
            .map { ProtoBuf.decodeFromByteArray(FrameServer.serializer(), it.readBytes()) }
            .map { frameServerFlow.emit(it) }
            .launchIn(scope)
    }

    private fun launchServerFlow(scope: CoroutineScope): Job {
        return frameServerFlow
            .map { frameServer ->
                when (frameServer) {
                    is FrameServer.Signal -> {
                        val peerConnection = getPeer(frameServer.from).peerConnection

                        when (frameServer) {
                            is FrameServer.Signal.NewPeer -> {
                                // RTCPeerConnection created above
                            }

                            is FrameServer.Signal.Offer -> {
                                peerConnection
                                    .asDynamic()
                                    .setRemoteDescription(JSON.parse(frameServer.sessionDescription))
                                    .unsafeCast<Promise<Any?>>().await()

                                peerConnection
                                    .asDynamic()
                                    .createAnswer()
                                    .unsafeCast<Promise<Any?>>().await()
                                    .let { peerConnection.asDynamic().setLocalDescription(it) }
                                    .unsafeCast<Promise<Any?>>().await()

                                run {
                                    peerConnection.asDynamic().localDescription.unsafeCast<Any?>()
                                        ?: throw IllegalStateException("localDescription == null")
                                }
                                    .let { answer ->
                                        FrameClient.Signal.Answer(
                                            owner = frameServer.from,
                                            sessionDescription = JSON.stringify(answer),
                                        )
                                    }
                                    .toFrame()
                                    .run { sendChannel.send(this) }
                            }

                            is FrameServer.Signal.Answer -> {
                                peerConnection
                                    .asDynamic()
                                    .setRemoteDescription(JSON.parse(frameServer.sessionDescription))
                                    .unsafeCast<Promise<Any?>>().await()
                            }

                            is FrameServer.Signal.IceCandidate -> {
                                peerConnection
                                    .asDynamic()
                                    .addIceCandidate(JSON.parse(frameServer.iceCandidate))
                                    .unsafeCast<Promise<Any?>>().await()
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
            .launchIn(scope)
    }

    private fun getPeer(from: Owner): Peer {
        return peers.getOrPut(from) {
            val peerConnection = newRTCPeerConnection()

            peerConnection.onnegotiationneeded = { event: dynamic ->
                println("PeerConnection.onnegotiationneeded: $from ${JSON.stringify(event.unsafeCast<Json>())}")

                scope.launch {
                    peerConnection.createOffer()
                        .unsafeCast<Promise<Any?>>().await()
                        .let { peerConnection.setLocalDescription(it) }
                        .unsafeCast<Promise<Any?>>().await()

                    run {
                        peerConnection.localDescription.unsafeCast<Json?>()
                            ?: throw IllegalStateException("localDescription == null")
                    }
                        .let { offer ->
                            FrameClient.Signal.Offer(
                                owner = from,
                                sessionDescription = JSON.stringify(offer),
                            )
                        }
                        .toFrame()
                        .run { sendChannel.send(this) }
                }
            }

            peerConnection.onconnectionstatechange = {
                println("PeerConnection.onconnectionstatechange: $from ${peerConnection.connectionState.unsafeCast<String>()}")
            }

            peerConnection.ondatachannel = { event: dynamic ->
                println("peerConnection.ondatachannel: $from")

                event.channel.onopen = {
                    println("channel.onopen: $from")
                }

                event.channel.onmessage = { message: dynamic ->
                    peerEventUpdate(
                        from = from,
                        peerUpdate = message.data.unsafeCast<String>().toPeerUpdate(),
                    )
                }

                event.channel.onclose = {
                    println("channel.onclose: $from")
                }

                event.channel.onerror = {
                    println("channel.onerror: $from")
                }
            }

            peerConnection.onicecandidate = { peerConnectionIceEvent: dynamic ->
                println("PeerConnection.onicecandidate: $from ${JSON.stringify(peerConnectionIceEvent.candidate.unsafeCast<Json>())}")

                if (peerConnectionIceEvent.unsafeCast<Json?>() != null) {
                    scope.launch {
                        FrameClient.Signal
                            .IceCandidate(
                                owner = from,
                                iceCandidate = JSON.stringify(peerConnectionIceEvent.candidate.unsafeCast<Json>()),
                            )
                            .toFrame()
                            .run { sendChannel.send(this) }
                    }
                }
            }

            val dataChannel = peerConnection.createDataChannel(
                "Data Channel: $from",
                json(
                    "negotiated" to false,
                    "ordered" to false,
                    "maxRetransmits" to 0,
                ),
            )

            dataChannel.onopen = { event: dynamic ->
                println("DataChannel.onopen: $from ${JSON.stringify(event.unsafeCast<Json>())}")
            }

            dataChannel.onmessage = { event: dynamic ->
                println("DataChannel.onmessage: $from ${JSON.stringify(event.unsafeCast<Json>())}")

                peerEventUpdate(
                    from = from,
                    peerUpdate = JSON.parse(event.data.unsafeCast<String>()),
                )
            }

            dataChannel.onclose = { event: dynamic ->
                println("DataChannel.onclose: $from ${JSON.stringify(event.unsafeCast<Json>())}")
            }

            dataChannel.onerror = { event: dynamic ->
                println("DataChannel.onerror: $from ${JSON.stringify(event.unsafeCast<Json>())}")
            }

            Peer(
                peerConnection = peerConnection,
                dataChannel = dataChannel,
            )
        }
    }

    override fun launchTank() {
        if (tank == null) {
            tank = getKoin().get()
        }
    }

    override fun launchShell(
        bearing: Float,
        onBoat: Boolean,
        startPosition: V2,
        sightRange: Float,
    ) {
        shells.add(getKoin().get { parametersOf(startPosition, bearing, onBoat, sightRange) })
    }

    override fun launchBuilder(
        startPosition: V2,
        targetX: Int,
        targetY: Int,
        buildOp: BuilderMission,
    ) {
        builders.add(getKoin().get { parametersOf(startPosition, targetX, targetY, buildOp) })
    }

    private fun peerEventUpdate(from: Owner, peerUpdate: PeerUpdate) {
        val peer = peers[from] ?: throw IllegalStateException("peers[${from}] is null")
        peer.tank = peerUpdate.tank
        peer.shells = peerUpdate.shells
        peer.builder = peerUpdate.builder
    }
}

fun Entity.isShore(owner: Int): Boolean =
    when (this) {
        is Entity.Pill -> isSolid()
        is Entity.Base -> isSolid(owner)
        is Entity.Terrain ->
            when (terrain) {
                TerrainTile.Sea,
                TerrainTile.River,
                TerrainTile.SeaMined,
                -> false

                TerrainTile.Boat,
                TerrainTile.Wall,
                TerrainTile.Swamp0,
                TerrainTile.Swamp1,
                TerrainTile.Swamp2,
                TerrainTile.Swamp3,
                TerrainTile.Crater,
                TerrainTile.Road,
                TerrainTile.Tree,
                TerrainTile.Rubble0,
                TerrainTile.Rubble1,
                TerrainTile.Rubble2,
                TerrainTile.Rubble3,
                TerrainTile.Grass0,
                TerrainTile.Grass1,
                TerrainTile.Grass2,
                TerrainTile.Grass3,
                TerrainTile.WallDamaged0,
                TerrainTile.WallDamaged1,
                TerrainTile.WallDamaged2,
                TerrainTile.WallDamaged3,
                TerrainTile.SwampMined,
                TerrainTile.CraterMined,
                TerrainTile.RoadMined,
                TerrainTile.TreeMined,
                TerrainTile.RubbleMined,
                TerrainTile.GrassMined,
                -> true
            }
    }

fun Entity.isShellable(owner: Int): Boolean =
    when (this) {
        is Entity.Pill -> isSolid()
        is Entity.Base -> isSolid(owner)
        is Entity.Terrain ->
            when (terrain) {
                TerrainTile.Sea,
                TerrainTile.River,
                TerrainTile.SeaMined,
                TerrainTile.Boat,
                TerrainTile.Swamp0,
                TerrainTile.Swamp1,
                TerrainTile.Swamp2,
                TerrainTile.Swamp3,
                TerrainTile.Crater,
                TerrainTile.Road,
                TerrainTile.Rubble0,
                TerrainTile.Rubble1,
                TerrainTile.Rubble2,
                TerrainTile.Rubble3,
                TerrainTile.Grass0,
                TerrainTile.Grass1,
                TerrainTile.Grass2,
                TerrainTile.Grass3,
                TerrainTile.SwampMined,
                TerrainTile.CraterMined,
                TerrainTile.RoadMined,
                TerrainTile.RubbleMined,
                TerrainTile.GrassMined,
                -> false

                TerrainTile.Wall,
                TerrainTile.Tree,
                TerrainTile.WallDamaged0,
                TerrainTile.WallDamaged1,
                TerrainTile.WallDamaged2,
                TerrainTile.WallDamaged3,
                TerrainTile.TreeMined,
                -> true
            }
    }

fun newRTCPeerConnection(): dynamic = js(
    """
    new RTCPeerConnection({
        iceServers: [
            {
                urls: ["stun:robch.dev", "turn:robch.dev"],
                username: "prouser",
                credential: "BE3pJ@",
            },
        ],
    })
    """,
)

private val frameClientSerializer = FrameClient.serializer()

private fun FrameClient.toFrame(): Frame {
    return ProtoBuf
        .encodeToByteArray(
            serializer = frameClientSerializer,
            value = this,
        )
        .let { Frame.Binary(fin = true, data = it) }
}

private val peerUpdateSerializer = PeerUpdate.serializer()

private fun PeerUpdate.toHexString(): String {
    return ProtoBuf.encodeToHexString(
        serializer = peerUpdateSerializer,
        value = this,
    )
}

private fun String.toPeerUpdate(): PeerUpdate {
    return ProtoBuf.decodeFromHexString(
        deserializer = peerUpdateSerializer,
        hex = this,
    )
}
