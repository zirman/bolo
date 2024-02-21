package client

import bmap.Entity
import bmap.StartInfo
import bmap.TerrainTile
import bmap.isMinedTerrain
import bmap.isSolid
import bmap.worldHeight
import frame.FrameClient
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.protobuf.ProtoBuf
import math.V2
import math.add
import math.clamp
import math.dirToVec
import math.mag
import math.norm
import math.pi
import math.prj
import math.scale
import math.tau
import math.v2
import math.v2Origin
import math.x
import math.y
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class TankImpl(
    private val scope: CoroutineScope,
    private val tankShotAudioManager: AudioManager,
    game: Game,
    override var hasBuilder: Boolean,
) : GeneratorLoopImpl<Tick>(scope), Tank, Game by game {
    companion object {
        private const val TANK_RADIUS: Float = 3f / 8f
        private const val FORCE_PUSH: Float = 25f / 16f
        private const val RELOAD_SEC: Float = 1f / 3f
        private const val MAX_BOAT_SPEED: Float = 25f / 8f
        private const val ACC_PER_SEC2: Float = MAX_BOAT_SPEED * (3f / 4f)
        // private const val forceKick: Float = 25f / 8f
        // private const val maxSpeedRoad: Float = maxSpeedBoat
    }

    private val start: StartInfo = bmap.starts[random.nextInt(bmap.starts.size)]

    override var position: V2 = v2(x = start.x.toFloat() + (1f / 2f), y = start.y.toFloat() + (1f / 2f))
        private set

    override var bearing: Float = start.direction.toFloat() * (Float.pi / 8f)
        private set

    override var sightRange: Float = 6f
        private set

    override var onBoat: Boolean = true
        private set

    override var material: Int = 0

    override var nextBuilderMission: BuilderMission? = null

    init {
        center = v2(x = start.x.toFloat() + (1f / 2f), y = worldHeight - (start.y.toFloat() + (1f / 2f)))
    }

    private var reload: Float = 0f
    private var tankShells: Int = tankShellsMax
    private var tankArmor: Int = tankArmorMax
    private var tankMines: Int = 0

    private var speed: Float = 0f

    private var rotVel: Float = 0f
    private var kickDir: Float = 0f
    private var kickSpeed: Float = 0f
    private var refuelingTime: Float = 0f

    inner class TerrainKernel(val tick: Tick) {
        val onX: Int = position.x.toInt()
        val onY: Int = position.y.toInt()
        val onTerrain: TerrainTile = bmap[onX, onY]
        val terrainUpLeft: TerrainTile = bmap[onX - 1, onY - 1]
        val terrainUp: TerrainTile = bmap[onX, onY - 1]
        val terrainUpRight: TerrainTile = bmap[onX + 1, onY - 1]
        val terrainLeft: TerrainTile = bmap[onX - 1, onY]
        val terrainRight: TerrainTile = bmap[onX + 1, onY]
        val terrainDownLeft: TerrainTile = bmap[onX - 1, onY + 1]
        val terrainDown: TerrainTile = bmap[onX, onY + 1]
        val terrainDownRight: TerrainTile = bmap[onX + 1, onY + 1]
    }

    override suspend fun launch() {
        doWhile { tick ->
            val terrainKernel = TerrainKernel(tick)

            // check for destruction
            when {
                bmap.getEntity(terrainKernel.onX, terrainKernel.onY).isSolid(owner.int) -> {
                    superBoom()
                }

                onBoat.not() &&
                        (terrainKernel.onTerrain == TerrainTile.Sea ||
                                terrainKernel.onTerrain == TerrainTile.SeaMined) -> {
                    sink()
                }

                tankArmor <= 0 -> {
                    fireball()
                }

                else -> {
                    tick.turning(terrainKernel.onTerrain)
                    tick.accelerating(terrainKernel.onTerrain)
                    tick.updateKick()
                    tick.updatePosition()
                    terrainKernel.shorePush()
                    // TODO: tankCollisions(temp)
                    position = collisionDetect(position)
                    terrainKernel.boatLogic()
                    tick.firing()
                    terrainKernel.mineLaying(tick)
                    terrainKernel.refueling(tick)
                    terrainKernel.updateServerPosition()
                    true
                }
            }
        }
    }

    private fun Tick.turning(onTerrain: TerrainTile) {
        // turning
        val acceleration = 12.566370f
        val maxVelocity: Float = if (onBoat) 5f / 2f else onTerrain.getMaxAngularVelocity()

        when (control.directionHorizontal) {
            DirectionHorizontal.Left -> {
                rotVel = min(maxVelocity, rotVel + (acceleration / ticksPerSec))
                bearing = (bearing + (rotVel / ticksPerSec)).mod(Float.tau)
            }

            DirectionHorizontal.Right -> {
                rotVel = max(-maxVelocity, rotVel - (acceleration / ticksPerSec))
                bearing = (bearing + (rotVel / ticksPerSec)).mod(Float.tau)
            }

            else -> {
                rotVel = 0f
            }
        }
    }

    private fun Tick.accelerating(onTerrain: TerrainTile) {
        val max: Float = if (onBoat) 25f / 8f else onTerrain.getSpeedMax()

        when {
            speed > max ->
                speed = max(max, speed - (ACC_PER_SEC2 / ticksPerSec))

            control.directionVertical == DirectionVertical.Up ->
                speed = min(max, speed + (ACC_PER_SEC2 / ticksPerSec))

            control.directionVertical == DirectionVertical.Down ->
                speed = max(0f, speed - (ACC_PER_SEC2 / ticksPerSec))
        }
    }

    private fun Tick.updateKick() {
        kickSpeed = max(0f, kickSpeed - (12f / ticksPerSec))
    }

    private fun Tick.updatePosition() {
        position = position.add(
            dirToVec(bearing)
                .scale(speed)
                .add(dirToVec(kickDir).scale(kickSpeed))
                .scale(delta)
        )
    }

    private fun TerrainKernel.shorePush() {
        // shorePush
        if (onBoat) {
            val push: V2

            val fx: Float = position.x - onX
            val fy: Float = position.y - onY
            val cx: Float = 1f - fx
            val cy: Float = 1f - fy
            val fxc: Boolean = (fx < TANK_RADIUS) && terrainLeft.isShore()
            val cxc: Boolean = ((1 - fx) < TANK_RADIUS) && terrainRight.isShore()
            val fyc: Boolean = (fy < TANK_RADIUS) && terrainUp.isShore()
            val cyc: Boolean = ((1 - fy) < TANK_RADIUS) && terrainDown.isShore()

            push = when {
                fxc.not() && fyc.not() &&
                        (((fx * fx + fy * fy) < (TANK_RADIUS * TANK_RADIUS)) && terrainUpLeft.isShore())
                -> v2(fx, fy)

                cxc.not() && fyc.not() &&
                        (((cx * cx + fy * fy) < (TANK_RADIUS * TANK_RADIUS)) && terrainUpRight.isShore())
                -> v2(-cx, fy)

                fxc.not() && cyc.not() &&
                        (((fx * fx + cy * cy) < (TANK_RADIUS * TANK_RADIUS)) && terrainDownLeft.isShore())
                -> v2(fx, -cy)

                cxc.not() && cyc.not() &&
                        (((cx * cx + cy * cy) < (TANK_RADIUS * TANK_RADIUS)) && terrainDownRight.isShore())
                -> v2(-cx, -cy)

                else -> when {
                    fxc -> when {
                        fyc -> v2(fy, fx)
                        cyc -> v2(cy, -fx)
                        else -> v2(fx, 0f)
                    }

                    cxc -> when {
                        fyc -> v2(-fy, cx)
                        cyc -> v2(-cy, -cx)
                        else -> v2(-cx, 0f)
                    }

                    else -> when {
                        fyc -> v2(0f, fy)
                        cyc -> v2(0f, -cy)
                        else -> v2Origin
                    }
                }
            }

            if (push.mag() > 0.00001) {
                val f: Float = push.prj(dirToVec(bearing).scale(speed)).mag()

                if (f < FORCE_PUSH) {
                    position = position.add(push.norm().scale(FORCE_PUSH / tick.ticksPerSec))
                }

                // apply breaks if not accelerating
                if (tick.control.directionVertical != DirectionVertical.Up) {
                    speed = max(0f, speed - (ACC_PER_SEC2 / tick.ticksPerSec))
                }
            }
        }
    }

    private fun collisionDetect(p: V2): V2 {
        val rr: Float = TANK_RADIUS * TANK_RADIUS

        val fx: Int = p.x.toInt()
        val fy: Int = p.y.toInt()
        val lx: Float = p.x - fx.toFloat()
        val hx: Float = 1f - lx
        val ly: Float = p.y - fy.toFloat()
        val hy: Float = 1f - ly

        val lxc: Boolean = lx < TANK_RADIUS && bmap.getEntity(fx - 1, fy).isSolid(owner.int)
        val hxc: Boolean = hx < TANK_RADIUS && bmap.getEntity(fx + 1, fy).isSolid(owner.int)
        val lyc: Boolean = ly < TANK_RADIUS && bmap.getEntity(fx, fy - 1).isSolid(owner.int)
        val hyc: Boolean = hy < TANK_RADIUS && bmap.getEntity(fx, fy + 1).isSolid(owner.int)

        var sqr: Float = lx * lx + ly * ly
        if (lxc.not() && lyc.not() && sqr < rr && bmap.getEntity(fx - 1, fy - 1).isSolid(owner.int)) {
            val sca: Float = TANK_RADIUS / sqrt(sqr)
            return v2((fx + sca * lx), (fy + sca * ly))
        }

        sqr = hx * hx + ly * ly
        if (hxc.not() && lyc.not() && sqr < rr && bmap.getEntity(fx + 1, fy - 1).isSolid(owner.int)) {
            val sca: Float = TANK_RADIUS / sqrt(sqr)
            return v2((fx + (1 - sca * hx)), (fy + sca * ly))
        }

        sqr = lx * lx + hy * hy
        if (lxc.not() && hyc.not() && sqr < rr && bmap.getEntity(fx - 1, fy + 1).isSolid(owner.int)) {
            val sca: Float = TANK_RADIUS / sqrt(sqr)
            return v2((fx + sca * lx), (fy + (1f - sca * hy)))
        }

        sqr = hx * hx + hy * hy
        if (hxc.not() && hyc.not() && sqr < rr && bmap.getEntity(fx + 1, fy + 1).isSolid(owner.int)) {
            val sca: Float = TANK_RADIUS / sqrt(sqr)
            return v2((fx + (1f - sca * hx)), (fy + (1f - sca * hy)))
        }

        return v2(
            x = when {
                lxc -> fx + TANK_RADIUS
                hxc -> fx + (1f - TANK_RADIUS)
                else -> p.x
            },
            y = when {
                lyc -> fy + TANK_RADIUS
                hyc -> fy + (1f - TANK_RADIUS)
                else -> p.y
            }
        )
    }

    private suspend fun TerrainKernel.boatLogic() {
        val x: Int = position.x.toInt()
        val y: Int = position.y.toInt()

        if (x != onX || y != onY) {
            if (onBoat) {
                if (bmap[x, y].isDrivable()) {
                    onBoat = false

                    if (onTerrain == TerrainTile.River) {
                        buildTerrain(onX, onY, TerrainTile.Boat) {}
                    }
                } else if (bmap[x, y] == TerrainTile.Boat) {
                    terrainDamage(x, y)
                }
            } else if (bmap[x, y] == TerrainTile.Boat) {
                onBoat = true
                terrainDamage(x, y)
            }
        }
    }

    private fun Tick.firing() {
        if (control.fireButton && tankShells > 0 && reload >= RELOAD_SEC) {
            launchShell(bearing, onBoat, position, sightRange)
            reload = 0f
            tankShells--
            tankShotAudioManager.play()
        }
        reload += delta
    }

    private suspend fun TerrainKernel.mineLaying(tick: Tick) {
        if (tick.control.layMineButton && tankMines > 0 && bmap[onX, onY].isMinedTerrain().not()) {
            mineTerrain(onX, onY)
        }
    }

    private fun TerrainKernel.refueling(tick: Tick) {
        if (speed == 0f) {
            refuelingTime += tick.delta
        } else {
            refuelingTime = 0f
        }

        when (val entity = bmap.getEntity(onX, onY)) {
            is Entity.Base -> {
                if (tankArmor < tankArmorMax && entity.ref.armor >= armorUnit) {
                    if (refuelingTime >= refuelArmorTime) {
                        tankArmor = (tankArmor + armorUnit).clamp(0..tankArmorMax)
                        entity.ref.armor -= armorUnit
                        refuelingTime = 0f
                    }
                } else if (tankShells < tankShellsMax && entity.ref.shells >= shellsUnit) {
                    if (refuelingTime >= refuelShellTime) {
                        tankShells = (tankShells + shellsUnit).clamp(0..tankShellsMax)
                        entity.ref.shells -= shellsUnit
                        refuelingTime = 0f
                    }
                } else if (tankMines < tankMinesMax && entity.ref.mines >= minesUnit) {
                    if (refuelingTime >= refuelMineTime) {
                        tankMines = (tankMines + minesUnit).clamp(0..tankMinesMax)
                        entity.ref.mines -= minesUnit
                        refuelingTime = 0f
                    }
                }
            }

            is Entity.Pill -> {}
            is Entity.Terrain -> {}
        }
    }

    private suspend fun TerrainKernel.updateServerPosition() {
        if (position.x.toInt() != onX || position.y.toInt() != onY) {
            ProtoBuf
                .encodeToByteArray(
                    FrameClient.serializer(),
                    FrameClient.Position(
                        x = position.x.toInt(),
                        y = position.y.toInt(),
                    ),
                )
                .let { Frame.Binary(fin = true, it) }
                .let { sendChannel.send(it) }
        }
    }

    private suspend fun superBoom(): Boolean {
        var time = 0f
        // TODO: super boom
        // TODO: drop pills

        doWhile { tick ->
            time += tick.delta
            time < 5f
        }

        scope.launch { launchTank(hasBuilder) }
        return false
    }

    private suspend fun sink(): Boolean {
        var time = 0f
        // TODO: drop pills

        doWhile { tick ->
            time += tick.delta
            time < 5f
        }

        scope.launch { launchTank(hasBuilder) }
        return false
    }

    private suspend fun fireball(): Boolean {
        var time = 0f
        // TODO: fireball
        // TODO: drop pills

        doWhile { tick ->
            time += tick.delta
            time < 5f
        }

        scope.launch { launchTank(hasBuilder) }
        return false
    }
}
