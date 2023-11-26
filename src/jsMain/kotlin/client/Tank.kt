@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package client

import bmap.Entity
import bmap.StartInfo
import bmap.TerrainTile
import bmap.isSolid
import bmap.worldHeight
import frame.FrameClient
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import math.clampCycle
import math.clampRange
import kotlinx.serialization.protobuf.ProtoBuf
import math.pi
import math.tau
import math.V2
import math.add
import math.mag
import math.norm
import math.prj
import math.scale
import math.v2
import math.v2Origin
import math.x
import math.y
import util.armorUnit
import util.dirToVec
import util.getMaxAngularVelocity
import util.getSpeedMax
import util.isDrivable
import util.isShore
import util.minesUnit
import util.refuelArmorTime
import util.refuelMineTime
import util.refuelShellTime
import util.shellsUnit
import util.tankArmorMax
import util.tankMinesMax
import util.tankShellsMax
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

interface Tank : GeneratorLoop<Tick> {
    val position: V2
    val bearing: Float
    val sightRange: Float
    val onBoat: Boolean
    var material: Int
    var hasBuilder: Boolean
}

@Suppress("NAME_SHADOWING")
class TankImpl(
    private val scope: CoroutineScope,
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

    override var bearing: Float = start.dir.toFloat() * (Float.pi / 8f)
        private set

    override var sightRange: Float = 6f
        private set

    override var onBoat: Boolean = true
        private set

    override var material: Int = 0

    init {
        center = v2(x = start.x.toFloat() + (1f / 2f), y = worldHeight - (start.y.toFloat() + (1f / 2f)))
    }

    override suspend fun launch() {
        var reload = 0f
        var tankShells: Int = tankShellsMax
        var tankArmor: Int = tankArmorMax
        var tankMines = 0

        var speed = 0f

        var rotVel = 0f
        val kickDir = 0f
        var kickSpeed = 0f
        var refuelingTime = 0f

        doWhile { tick ->
            val control = tick.control
            val onX = position.x.toInt()
            val onY = position.y.toInt()
            val onTerrain = bmap[onX, onY]
            val terrainUpLeft = bmap[onX - 1, onY - 1]
            val terrainUp = bmap[onX, onY - 1]
            val terrainUpRight = bmap[onX + 1, onY - 1]
            val terrainLeft = bmap[onX - 1, onY]
            val terrainRight = bmap[onX + 1, onY]
            val terrainDownLeft = bmap[onX - 1, onY + 1]
            val terrainDown = bmap[onX, onY + 1]
            val terrainDownRight = bmap[onX + 1, onY + 1]

            // check for destruction
            if (bmap.getEntity(onX, onY).isSolid(owner.int)) {
                var time = 0f
                // super boom
                // drop pills

                doWhile { tick ->
                    time += tick.delta
                    time < 5f
                }

                scope.launch { launchTank(hasBuilder) }
                false
            } else if (!onBoat && onTerrain == TerrainTile.Sea) {
                var time = 0f
                // drop pills

                doWhile { tick ->
                    time += tick.delta
                    time < 5f
                }

                scope.launch { launchTank(hasBuilder) }
                false
            } else if (tankArmor <= 0) {
                var time = 0f
                // fireball
                // drop pills

                doWhile { tick ->
                    time += tick.delta
                    time < 5f
                }

                scope.launch { launchTank(hasBuilder) }
                false
            } else if (onTerrain == TerrainTile.SeaMined) {
                var time = 0f
                // boom
                // drop pills

                doWhile { tick ->
                    time += tick.delta
                    time < 5f
                }

                false
            } else {
                // turning
                val acceleration = 12.566370f
                val maxVelocity: Float = if (onBoat) 5f / 2f else getMaxAngularVelocity(onTerrain)

                when (control.directionHorizontal) {
                    DirectionHorizontal.Left -> {
                        rotVel = min(maxVelocity, rotVel + (acceleration / tick.ticksPerSec))
                        bearing = (bearing + (rotVel / tick.ticksPerSec)).clampCycle(Float.tau)
                    }

                    DirectionHorizontal.Right -> {
                        rotVel = max(-maxVelocity, rotVel - (acceleration / tick.ticksPerSec))
                        bearing = (bearing + (rotVel / tick.ticksPerSec)).clampCycle(Float.tau)
                    }

                    else -> {
                        rotVel = 0f
                    }
                }

                // accelerating
                val max: Float = if (onBoat) 25f / 8f else getSpeedMax(onTerrain)

                when {
                    speed > max ->
                        speed = max(max, speed - (ACC_PER_SEC2 / tick.ticksPerSec))

                    control.directionVertical == DirectionVertical.Up ->
                        speed = min(max, speed + (ACC_PER_SEC2 / tick.ticksPerSec))

                    control.directionVertical == DirectionVertical.Down ->
                        speed = max(0f, speed - (ACC_PER_SEC2 / tick.ticksPerSec))
                }

                // updateKick
                kickSpeed = max(0f, kickSpeed - (12f / tick.ticksPerSec))

                // updatePosition
                position = position
                    .add(
                        dirToVec(bearing)
                            .scale(speed)
                            .add(dirToVec(kickDir).scale(kickSpeed))
                            .scale(tick.delta)
                    )

                // shorePush
                if (onBoat) {
                    val push: V2

                    val fx: Float = position.x - onX
                    val fy: Float = position.y - onY
                    val cx: Float = 1f - fx
                    val cy: Float = 1f - fy
                    val fxc: Boolean = (fx < TANK_RADIUS) && isShore(terrainLeft)
                    val cxc: Boolean = ((1 - fx) < TANK_RADIUS) && isShore(terrainRight)
                    val fyc: Boolean = (fy < TANK_RADIUS) && isShore(terrainUp)
                    val cyc: Boolean = ((1 - fy) < TANK_RADIUS) && isShore(terrainDown)

                    push = when {
                        fxc.not() && fyc.not() && (((fx * fx + fy * fy) < (TANK_RADIUS * TANK_RADIUS)) && isShore(
                            terrainUpLeft
                        ))
                        -> v2(fx, fy)

                        cxc.not() && fyc.not() && (((cx * cx + fy * fy) < (TANK_RADIUS * TANK_RADIUS)) && isShore(
                            terrainUpRight
                        ))
                        -> v2(-cx, fy)

                        fxc.not() && cyc.not() && (((fx * fx + cy * cy) < (TANK_RADIUS * TANK_RADIUS)) && isShore(
                            terrainDownLeft
                        ))
                        -> v2(fx, -cy)

                        cxc.not() && cyc.not() && (((cx * cx + cy * cy) < (TANK_RADIUS * TANK_RADIUS)) && isShore(
                            terrainDownRight
                        ))
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
                        if (control.directionVertical != DirectionVertical.Up) {
                            speed = max(0f, speed - (ACC_PER_SEC2 / tick.ticksPerSec))
                        }
                    }
                }

                // tankCollisions(temp)

                // terrainCollisions
                position = collisionDetect(position)

                // boatLogic
                val x: Int = position.x.toInt()
                val y: Int = position.y.toInt()

                if (x != onX || y != onY) {
                    if (onBoat) {
                        if (isDrivable(bmap[x, y])) {
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

                // shooting
                if (control.shootButton && tankShells > 0 && reload >= RELOAD_SEC) {
                    launchShell(bearing, onBoat, position, sightRange)
                    reload = 0f
                    tankShells--
                }

                // mine laying
                if (control.layMineButton && tankMines > 0 && bmap[x, y].isMinedTerrain().not()) {
                    mineTerrain(x, y)
                }

                reload += tick.delta

                // refueling

                if (speed == 0f) {
                    refuelingTime += tick.delta
                } else {
                    refuelingTime = 0f
                }

                when (val entity = bmap.getEntity(onX, onY)) {
                    is Entity.Base -> {
                        if (tankArmor < tankArmorMax && entity.ref.armor >= armorUnit) {
                            if (refuelingTime >= refuelArmorTime) {
                                tankArmor = (tankArmor + armorUnit).clampRange(0, tankArmorMax)
                                entity.ref.armor -= armorUnit
                                refuelingTime = 0f
                            }
                        } else if (tankShells < tankShellsMax && entity.ref.shells >= shellsUnit) {
                            if (refuelingTime >= refuelShellTime) {
                                tankShells = (tankShells + shellsUnit).clampRange(0, tankShellsMax)
                                entity.ref.shells -= shellsUnit
                                refuelingTime = 0f
                            }
                        } else if (tankMines < tankMinesMax && entity.ref.mines >= minesUnit) {
                            if (refuelingTime >= refuelMineTime) {
                                tankMines = (tankMines + minesUnit).clampRange(0, tankMinesMax)
                                entity.ref.mines -= minesUnit
                                refuelingTime = 0f
                            }
                        }
                    }

                    is Entity.Pill -> {}
                    is Entity.Terrain -> {}
                }

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

                true
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
        if (!lxc && !lyc && sqr < rr && bmap.getEntity(fx - 1, fy - 1).isSolid(owner.int)) {
            val sca: Float = TANK_RADIUS / sqrt(sqr)
            return v2((fx + sca * lx), (fy + sca * ly))
        }

        sqr = hx * hx + ly * ly
        if (!hxc && !lyc && sqr < rr && bmap.getEntity(fx + 1, fy - 1).isSolid(owner.int)) {
            val sca: Float = TANK_RADIUS / sqrt(sqr)
            return v2((fx + (1 - sca * hx)), (fy + sca * ly))
        }

        sqr = lx * lx + hy * hy
        if (!lxc && !hyc && sqr < rr && bmap.getEntity(fx - 1, fy + 1).isSolid(owner.int)) {
            val sca: Float = TANK_RADIUS / sqrt(sqr)
            return v2((fx + sca * lx), (fy + (1f - sca * hy)))
        }

        sqr = hx * hx + hy * hy
        if (!hxc && !hyc && sqr < rr && bmap.getEntity(fx + 1, fy + 1).isSolid(owner.int)) {
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
}
