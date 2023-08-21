@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package client

import bmap.Bmap
import bmap.BmapCode
import bmap.Entity
import bmap.Terrain
import bmap.border
import bmap.isSolid
import bmap.worldHeight
import bmap.worldWidth
import frame.FrameClient
import frame.FrameServer
import io.ktor.websocket.Frame
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitAnimationFrame
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.protobuf.ProtoBuf
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_TEST
import org.khronos.webgl.WebGLRenderingContext.Companion.ONE_MINUS_SRC_ALPHA
import org.khronos.webgl.WebGLRenderingContext.Companion.SRC_ALPHA
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import math.M4
import math.V2
import math.add
import math.orthographicProj2d
import math.scale
import math.v2Origin
import math.x
import math.y
import util.dirToVec
import kotlin.js.Date
import kotlin.math.max
import kotlin.random.Random

data class Tick(
    val control: ControlState,
    val ticksPerSec: Float,
    val delta: Float,
)

sealed interface BuildOp {
    data class Terrain(
        val terrain: bmap.Terrain,
        val x: Int,
        val y: Int,
        val result: (Boolean) -> Unit
    ) : BuildOp

    data object PillPlacement : BuildOp

    data class PillRepair(
        val index: Int,
        val material: Int,
    ) : BuildOp
}

interface GamePublic {
    val bmap: Bmap
    val random: Random
    val owner: Int
    val sendChannel: SendChannel<Frame>
    var center: V2
    fun launchTank(scope: CoroutineScope)
    fun launchShell(scope: CoroutineScope, bearing: Float, onBoat: Boolean, startPosition: V2, sightRange: Float)
    fun launchBuilder(scope: CoroutineScope, startPosition: V2, targetX: Int, targetY: Int, buildOp: BuilderMission)
    suspend fun terrainDamage(x: Int, y: Int)
    suspend fun buildTerrain(x: Int, y: Int, t: Terrain, result: (Boolean) -> Unit)
    suspend fun baseDamage(index: Int)
    suspend fun pillDamage(index: Int)
    val tank: Tank?
    var isBuilderInTank: Boolean
    operator fun get(x: Int, y: Int): Entity
}

class Game(
    override val sendChannel: SendChannel<Frame>,
    override val owner: Int,
    override val bmap: Bmap,
    private val bmapCode: BmapCode,
    private val tileProgram: (clipMatrix: M4, tiles: TileArray) -> Unit,
    private val spriteProgram: (M4, List<SpriteInstance>) -> Unit,
) : GamePublic {
    override val random = Random(Date.now().toInt())
    override var center: V2 = v2Origin
    override var isBuilderInTank: Boolean = true

    val frameServerFlow = MutableSharedFlow<FrameServer>()

    private val frameRegulator: MutableSet<Double> = mutableSetOf()

    private val tileArray: TileArray = TileArray(bmap, owner)
    private val buildQueue: MutableList<BuildOp> = mutableListOf()

    private val zoomLevel: Float = 2f
    private val tanks = mutableListOf<Tank>()
    private val shells = mutableListOf<Shell>()
    private val builders = mutableListOf<Builder>()

    suspend fun run() = coroutineScope {
        launchServerFlow(this)
        launchGameLoop(this)
        launchTank(this)
    }

    override val tank get() = tanks.firstOrNull { it.isVisible }

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

    override suspend fun buildTerrain(x: Int, y: Int, t: Terrain, result: (Boolean) -> Unit) {
        buildQueue.add(BuildOp.Terrain(t, x, y, result))

        ProtoBuf
            .encodeToByteArray(
                FrameClient.serializer(),
                FrameClient.TerrainBuild(
                    terrain = t,
                    x = x,
                    y = y,
                ),
            )
            .let { Frame.Binary(fin = true, it) }
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

    private fun render() {
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

//            for (peer of peers.values()) {
//                spriteArray.push({
//                    x: peer.positionX,
//                    math.math.getY: peer.positionY,
//                    spriteIndex:
//                    (peer.onBoat ? client.Sprite.TankEnemyBoat0 : client.Sprite.TankEnemy0) +
//                    Math.floor((peer.bearing + (Float.util.getPi * (1f / 16f))) * (8f / Float.util.getPi)) % 16,
//                })
//            }

            val sprites = mutableListOf<SpriteInstance>()

            for (builder in builders) {
                SpriteInstance(
                    x = builder.position.x,
                    y = builder.position.y,
                    sprite = Sprite.Lgm0,
                ).let { sprites.add(it) }
            }

            for (tank in tanks) {
                if (tank.isVisible) {
                    SpriteInstance(
                        x = tank.position.x,
                        y = tank.position.y,
                        sprite = (if (tank.onBoat) Sprite.TankBoat0 else Sprite.Tank0).withBearing(tank.bearing),
                    ).let { sprites.add(it) }

                    val reticulePosition = tank.position.add(dirToVec(tank.bearing).scale(tank.sightRange))

                    SpriteInstance(
                        x = reticulePosition.x,
                        y = reticulePosition.y,
                        sprite = Sprite.Reticule,
                    ).let { sprites.add(it) }
                }
            }

            for (shell in shells) {
                SpriteInstance(
                    x = shell.position.x,
                    y = shell.position.y,
                    sprite = Sprite.Shell0.withBearing(shell.bearing),
                ).let { sprites.add(it) }
            }

            spriteProgram(clipMatrix, sprites)
        } catch (error: Throwable) {
            println("error $error")
        }
    }

    override suspend fun terrainDamage(x: Int, y: Int) {
        bmap.damage(x, y)
        tileArray.update(x, y)

        ProtoBuf
            .encodeToByteArray(
                FrameClient.serializer(),
                FrameClient.TerrainDamage(
                    code = bmapCode[x, y],
                    x = x,
                    y = y,
                ),
            )
            .let { Frame.Binary(fin = true, it) }
            .let { sendChannel.send(it) }
    }

    override suspend fun baseDamage(index: Int) {
        val base = bmap.bases[index]
        base.armor = max(0, base.armor - 8)

        ProtoBuf
            .encodeToByteArray(
                FrameClient.serializer(),
                FrameClient.BaseDamage(
                    index,
                    code = base.code,
                ),
            )
            .let { Frame.Binary(fin = true, it) }
            .let { sendChannel.send(it) }
    }

    override suspend fun pillDamage(index: Int) {
        val pill = bmap.pills[index]
        pill.armor = max(0, pill.armor - 1)
        tileArray.update(pill.x, pill.y)

        ProtoBuf
            .encodeToByteArray(
                FrameClient.serializer(),
                FrameClient.PillDamage(
                    index,
                    code = pill.code,
                    x = pill.x,
                    y = pill.y,
                ),
            )
            .let { Frame.Binary(fin = true, it) }
            .let { sendChannel.send(it) }
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

    private fun launchGameLoop(scope: CoroutineScope) = scope.launch {
        while (true) {
            val time = window.awaitAnimationFrame()
            frameRegulator.removeAll { time - it > 1000.0 }
            frameRegulator.add(time)
            val ticksPerSec = max(1, frameRegulator.size).toFloat()

            val tick =
                Tick(
                    control = Control.getControlState(),
                    ticksPerSec = ticksPerSec,
                    delta = 1f / ticksPerSec,
                )

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

                    if (isBuilderInTank && sqrX in border.until(worldWidth - border) && sqrY in border.until(worldHeight - border)) {
                        when (tick.control.builderMode) {
                            is BuilderMode.Tree -> {
                                if (bmap[sqrX, sqrY] == Terrain.Tree) {
                                    tank?.let { tank ->
                                        launchBuilder(scope, tank.position, sqrX, sqrY, BuilderMission.HarvestTree)
                                        isBuilderInTank = false
                                    }
                                }
                            }

                            is BuilderMode.Road -> {
                                // TODO: proper check
                                if (bmap[sqrX, sqrY] == Terrain.Grass3) {
                                    tank?.let { tank ->
                                        launchBuilder(scope, tank.position, sqrX, sqrY, BuilderMission.BuildRoad)
                                        isBuilderInTank = false
                                    }
                                }
                            }

                            is BuilderMode.Wall -> {
                                if (bmap[sqrX, sqrY] == Terrain.Grass3) {
                                    tank?.let { tank ->
                                        launchBuilder(scope, tank.position, sqrX, sqrY, BuilderMission.BuildWall)
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

            tanks.removeAll { it.job.isCompleted }

            for (tank in tanks) {
                tank.resumeWith(tick)
            }

            shells.removeAll { it.job.isCompleted }

            for (shell in shells) {
                shell.resumeWith(tick)
            }

            builders.removeAll { it.job.isCompleted }

            for (builder in builders) {
                builder.resumeWith(tick)
            }

            render()
        }
    }

    private fun launchServerFlow(scope: CoroutineScope) = frameServerFlow.map { frameServer ->
        when (frameServer) {
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
//                buildQueue.removeAt(0) as BuildOp.PillRepair
//                tankMaterial = (tankMaterial + frameServer.material).clampRange(0, tankMaterialMax)
            }

            is FrameServer.PillRepairFailed -> {
//                val pillRepair = buildQueue.removeAt(0) as BuildOp.PillRepair
//                tankMaterial = (tankMaterial + pillRepair.material).clampRange(0, tankMaterialMax)
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
//                buildQueue.removeAt(0) is BuildOp.PillPlacement
                //Unit
            }

            is FrameServer.PillPlacementFailed -> {
//                buildQueue.removeAt(0) is BuildOp.PillPlacement
//                tankMaterial = (tankMaterial + pillPerMaterial).clampRange(0, tankMaterialMax)
            }
        }
    }.launchIn(scope)

    override fun launchTank(scope: CoroutineScope) {
        tanks.add(Tank(scope, this))
    }

    override fun launchShell(
        scope: CoroutineScope,
        bearing: Float,
        onBoat: Boolean,
        startPosition: V2,
        sightRange: Float
    ) {
        shells.add(Shell(scope, this, startPosition, bearing, onBoat, sightRange))
    }

    override fun launchBuilder(
        scope: CoroutineScope,
        startPosition: V2,
        targetX: Int,
        targetY: Int,
        buildOp: BuilderMission,
    ) {
        builders.add(Builder(scope, this, startPosition, targetX, targetY, buildOp))
    }
}

fun Entity.isShore(owner: Int): Boolean =
    when (this) {
        is Entity.Pill -> isSolid()
        is Entity.Base -> isSolid(owner)
        is Entity.Terrain ->
            when (terrain) {
                Terrain.Sea,
                Terrain.River,
                Terrain.SeaMined -> false

                Terrain.Boat,
                Terrain.Wall,
                Terrain.Swamp0,
                Terrain.Swamp1,
                Terrain.Swamp2,
                Terrain.Swamp3,
                Terrain.Crater,
                Terrain.Road,
                Terrain.Tree,
                Terrain.Rubble0,
                Terrain.Rubble1,
                Terrain.Rubble2,
                Terrain.Rubble3,
                Terrain.Grass0,
                Terrain.Grass1,
                Terrain.Grass2,
                Terrain.Grass3,
                Terrain.WallDamaged0,
                Terrain.WallDamaged1,
                Terrain.WallDamaged2,
                Terrain.WallDamaged3,
                Terrain.SwampMined,
                Terrain.CraterMined,
                Terrain.RoadMined,
                Terrain.ForestMined,
                Terrain.RubbleMined,
                Terrain.GrassMined -> true
            }
    }

fun Entity.isShellable(owner: Int): Boolean =
    when (this) {
        is Entity.Pill -> isSolid()
        is Entity.Base -> isSolid(owner)
        is Entity.Terrain ->
            when (terrain) {
                Terrain.Sea,
                Terrain.River,
                Terrain.SeaMined,
                Terrain.Boat,
                Terrain.Swamp0,
                Terrain.Swamp1,
                Terrain.Swamp2,
                Terrain.Swamp3,
                Terrain.Crater,
                Terrain.Road,
                Terrain.Rubble0,
                Terrain.Rubble1,
                Terrain.Rubble2,
                Terrain.Rubble3,
                Terrain.Grass0,
                Terrain.Grass1,
                Terrain.Grass2,
                Terrain.Grass3,
                Terrain.SwampMined,
                Terrain.CraterMined,
                Terrain.RoadMined,
                Terrain.RubbleMined,
                Terrain.GrassMined -> false

                Terrain.Wall,
                Terrain.Tree,
                Terrain.WallDamaged0,
                Terrain.WallDamaged1,
                Terrain.WallDamaged2,
                Terrain.WallDamaged3,
                Terrain.ForestMined -> true
            }
    }
