package client

import common.bmap.Base
import common.bmap.Entity
import common.bmap.StartInfo
import common.bmap.TerrainTile
import common.bmap.WORLD_HEIGHT
import common.bmap.isSolid
import common.ARMOR_UNIT
import common.MIENS_UNIT
import common.REFUEL_ARMOR_TIME
import common.REFUEL_MINE_TIME
import common.REFUEL_SHELL_TIME
import common.SHELL_UNIT
import common.getMaxAngularVelocity
import common.getSpeedMax
import common.isDrivable
import common.isMined
import common.isShore
import common.frame.FrameClient
import io.ktor.websocket.Frame
import kotlinx.serialization.protobuf.ProtoBuf
import client.math.V2
import client.math.dirToVec
import client.math.pi
import client.math.squared
import client.math.tau
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

class TankImpl(
    private val tankShotAudioManager: AudioManager,
    game: Game,
    override var hasBuilder: Boolean,
) : AbstractGameProcess(), Tank, Game by game, KoinComponent {
    companion object {
        private const val TANK_RADIUS: Float = 3f / 8f
        private const val FORCE_PUSH: Float = 25f / 16f
        private const val RELOAD_SEC: Float = 1f / 3f
        private const val MAX_SPEED: Float = 25f / 8f
        private const val ACC_PER_SEC2: Float = MAX_SPEED * (3f / 4f)
        private const val MAX_TURN_RATE: Float = 5f / 2f
        private const val TURN_RATE_PER_SEC2 = 12.566370f
        private const val MIN_SIGHT_RANGE: Float = 1f
        private const val MAX_SIGHT_RANGE: Float = 6f
        const val TANK_SHELLS_MAX = 40
        const val TANK_MINES_MAX = 40
        const val TANK_ARMOR_MAX = 40
        const val TANK_MATERIAL_MAX = 40
        // private const val FORCE_KICK: Float = 25f / 8f
    }

    private val start: StartInfo = bmap.starts[Random.Default.nextInt(bmap.starts.size)]

    override var position: V2 = V2.create(x = start.col.toFloat() + .5f, y = start.row.toFloat() + .5f)
        private set

    override var bearing: Float = start.direction.toFloat() * (Float.pi / 8f)
        private set

    override var sightRange: Float = MAX_SIGHT_RANGE
        private set

    override var onBoat: Boolean = true
        private set

    override var material: Int = 0

    private var _nextBuilderMission: NextBuilderMission? = null
    override fun getNextBuilderMission(): NextBuilderMission? {
        val nextBuilderMission = _nextBuilderMission
        _nextBuilderMission = null
        return nextBuilderMission
    }

    override fun setNextBuilderMission(nextBuilderMission: NextBuilderMission?) {
        _nextBuilderMission = nextBuilderMission
    }

    private var reload: Float = 0f
    private var shells: Int = TANK_SHELLS_MAX
    private var armor: Int = TANK_ARMOR_MAX
    override var mines: Int = 0

    private var speed: Float = 0f

    private var rotVel: Float = 0f
    private var kickDir: Float = 0f
    private var kickSpeed: Float = 0f
    private var refuelingTime: Float = 0f

    init {
        center = V2.create(x = start.col.toFloat() + .5f, y = WORLD_HEIGHT - (start.row.toFloat() + .5f))
        setArmorStatusBar(armor.toFloat() / TANK_ARMOR_MAX)
        setShellsStatusBar(shells.toFloat() / TANK_SHELLS_MAX)
        setMinesStatusBar(mines.toFloat() / TANK_MINES_MAX)
        setMaterialStatusBar(material.toFloat() / TANK_MATERIAL_MAX)
    }

    override val consumer: Consumer<Tick> = consumer {
        while (true) {
            val tick = next()
            val terrainKernel = TerrainKernel(tick)
            val devicePixelRatio = getDevicePixelRatio()

            sightRange = (sightRange - (tick.control.deltaY * devicePixelRatio / (zoomLevel * 128)))
                .coerceIn(MIN_SIGHT_RANGE, MAX_SIGHT_RANGE)

            // check for destruction
            when {
                // superboom
                bmap.getEntity(terrainKernel.onCol, terrainKernel.onRow).isSolid(owner.int) -> {
                    // TODO: super boom
                    // TODO: drop pills
                    tick.set(LogicGameProcess {
                        wait(5f).apply {
                            set(get<Tank> { parametersOf(hasBuilder) })
                        }
                    })

                    return@consumer
                }

                // sink
                onBoat.not() &&
                        (terrainKernel.onTerrain == TerrainTile.Sea ||
                                terrainKernel.onTerrain == TerrainTile.SeaMined) -> {
                    // TODO: drop pills
                    tick.set(LogicGameProcess {
                        wait(5f).apply {
                            set(get<Tank> { parametersOf(hasBuilder) })
                        }
                    })

                    return@consumer
                }

                // armor depleted
                armor <= 0 -> {
                    // TODO: fireball
                    // TODO: drop pills
                    tick.set(LogicGameProcess {
                        wait(5f).apply {
                            set(get<Tank> { parametersOf(hasBuilder) })
                        }
                    })

                    return@consumer
                }

                else -> {
                    tick.turning(terrainKernel)
                    tick.accelerating(terrainKernel)
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
                }
            }
        }
    }

    private fun Tick.turning(terrainKernel: TerrainKernel) {
        val maxVelocity: Float = if (onBoat || terrainKernel.onBase != null) {
            MAX_TURN_RATE
        } else {
            terrainKernel.onTerrain.getMaxAngularVelocity()
        }

        when (control.directionHorizontal) {
            DirectionHorizontal.Left -> {
                rotVel = min(maxVelocity, rotVel + (TURN_RATE_PER_SEC2 / ticksPerSec))
                bearing = (bearing + (rotVel / ticksPerSec)).mod(Float.tau)
            }

            DirectionHorizontal.Right -> {
                rotVel = max(-maxVelocity, rotVel - (TURN_RATE_PER_SEC2 / ticksPerSec))
                bearing = (bearing + (rotVel / ticksPerSec)).mod(Float.tau)
            }

            else -> {
                rotVel = 0f
            }
        }
    }

    private fun Tick.accelerating(terrainKernel: TerrainKernel) {
        val max = if (onBoat || terrainKernel.onBase != null) MAX_SPEED else terrainKernel.onTerrain.getSpeedMax()

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
        if (onBoat) {
            val push: V2

            val fx: Float = position.x - onCol
            val fy: Float = position.y - onRow
            val cx: Float = 1f - fx
            val cy: Float = 1f - fy
            val fxc: Boolean = (fx < TANK_RADIUS) && terrainLeft.isShore()
            val cxc: Boolean = ((1 - fx) < TANK_RADIUS) && terrainRight.isShore()
            val fyc: Boolean = (fy < TANK_RADIUS) && terrainUp.isShore()
            val cyc: Boolean = ((1 - fy) < TANK_RADIUS) && terrainDown.isShore()

            push = when {
                fxc.not() && fyc.not() &&
                        (((fx * fx + fy * fy) < (TANK_RADIUS.squared)) && terrainUpLeft.isShore())
                    -> V2.create(fx, fy)

                cxc.not() && fyc.not() &&
                        (((cx * cx + fy * fy) < (TANK_RADIUS.squared)) && terrainUpRight.isShore())
                    -> V2.create(-cx, fy)

                fxc.not() && cyc.not() &&
                        (((fx * fx + cy * cy) < (TANK_RADIUS.squared)) && terrainDownLeft.isShore())
                    -> V2.create(fx, -cy)

                cxc.not() && cyc.not() &&
                        (((cx * cx + cy * cy) < (TANK_RADIUS.squared)) && terrainDownRight.isShore())
                    -> V2.create(-cx, -cy)

                else -> when {
                    fxc -> when {
                        fyc -> V2.create(fy, fx)
                        cyc -> V2.create(cy, -fx)
                        else -> V2.create(fx, 0f)
                    }

                    cxc -> when {
                        fyc -> V2.create(-fy, cx)
                        cyc -> V2.create(-cy, -cx)
                        else -> V2.create(-cx, 0f)
                    }

                    else -> when {
                        fyc -> V2.create(0f, fy)
                        cyc -> V2.create(0f, -cy)
                        else -> V2.ORIGIN
                    }
                }
            }

            if (push.magnitude > 0.00001) {
                val f: Float = push.prj(dirToVec(bearing).scale(speed)).magnitude

                if (f < FORCE_PUSH) {
                    position = position.add(push.normalize.scale(FORCE_PUSH / tick.ticksPerSec))
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
            return V2.create((fx + sca * lx), (fy + sca * ly))
        }

        sqr = hx * hx + ly * ly
        if (hxc.not() && lyc.not() && sqr < rr && bmap.getEntity(fx + 1, fy - 1).isSolid(owner.int)) {
            val sca: Float = TANK_RADIUS / sqrt(sqr)
            return V2.create((fx + (1 - sca * hx)), (fy + sca * ly))
        }

        sqr = lx * lx + hy * hy
        if (lxc.not() && hyc.not() && sqr < rr && bmap.getEntity(fx - 1, fy + 1).isSolid(owner.int)) {
            val sca: Float = TANK_RADIUS / sqrt(sqr)
            return V2.create((fx + sca * lx), (fy + (1f - sca * hy)))
        }

        sqr = hx * hx + hy * hy
        if (hxc.not() && hyc.not() && sqr < rr && bmap.getEntity(fx + 1, fy + 1).isSolid(owner.int)) {
            val sca: Float = TANK_RADIUS / sqrt(sqr)
            return V2.create((fx + (1f - sca * hx)), (fy + (1f - sca * hy)))
        }

        return V2.create(
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

    private fun TerrainKernel.boatLogic() {
        val col: Int = position.x.toInt()
        val row: Int = position.y.toInt()

        if (col != onCol || row != onRow) {
            if (onBoat) {
                if (bmap[col, row].isDrivable()) {
                    onBoat = false

                    if (onTerrain == TerrainTile.River) {
                        // TODO: drop boat
                    }
                } else if (bmap[col, row] == TerrainTile.Boat) {
                    terrainDamage(col, row)
                }
            } else if (bmap[col, row] == TerrainTile.Boat) {
                onBoat = true
                terrainDamage(col, row)
            }
        }
    }

    private fun Tick.firing() {
        if (control.fireButton && shells > 0 && reload >= RELOAD_SEC) {
            add(get<Shell> { parametersOf(position, bearing, onBoat, sightRange) })
            reload = 0f
            shells--
            setShellsStatusBar(shells.toFloat() / TANK_SHELLS_MAX)
            tankShotAudioManager.play()
        }
        reload += delta
    }

    private fun TerrainKernel.mineLaying(tick: Tick) {
        if (tick.control.layMineButton && mines > 0 && bmap[onCol, onRow].isMined().not()) {
            // mineTerrain(onCol, onRow) {}
        }
    }

    private fun TerrainKernel.refueling(tick: Tick) {
        if (speed == 0f) {
            refuelingTime += tick.delta
        } else {
            refuelingTime = 0f
        }

        when (val entity = bmap.getEntity(onCol, onRow)) {
            is Entity.Base -> {
                if (armor < TANK_ARMOR_MAX && entity.ref.armor >= ARMOR_UNIT) {
                    if (refuelingTime >= REFUEL_ARMOR_TIME) {
                        armor = (armor + ARMOR_UNIT).coerceIn(0, TANK_ARMOR_MAX)
                        setArmorStatusBar(armor.toFloat() / TANK_ARMOR_MAX)
                        entity.ref.armor -= ARMOR_UNIT
                        refuelingTime = 0f
                    }
                } else if (shells < TANK_SHELLS_MAX && entity.ref.shells >= SHELL_UNIT) {
                    if (refuelingTime >= REFUEL_SHELL_TIME) {
                        shells = (shells + SHELL_UNIT).coerceIn(0, TANK_SHELLS_MAX)
                        setShellsStatusBar(shells.toFloat() / TANK_SHELLS_MAX)
                        entity.ref.shells -= SHELL_UNIT
                        refuelingTime = 0f
                    }
                } else if (mines < TANK_MINES_MAX && entity.ref.mines >= MIENS_UNIT) {
                    if (refuelingTime >= REFUEL_MINE_TIME) {
                        mines = (mines + MIENS_UNIT).coerceIn(0, TANK_MINES_MAX)
                        setMinesStatusBar(mines.toFloat() / TANK_MINES_MAX)
                        entity.ref.mines -= MIENS_UNIT
                        refuelingTime = 0f
                    }
                }
            }

            is Entity.Pill -> {}
            is Entity.Terrain -> {}
        }
    }

    private fun TerrainKernel.updateServerPosition() {
        if (position.x.toInt() != onCol || position.y.toInt() != onRow) {
            ProtoBuf
                .encodeToByteArray(
                    FrameClient.serializer(),
                    FrameClient.Position(
                        col = position.x.toInt(),
                        row = position.y.toInt(),
                    ),
                )
                .let { Frame.Binary(fin = true, it) }
                .let { sendChannel.trySend(it).getOrThrow() }
        }
    }

    inner class TerrainKernel(val tick: Tick) {
        val onCol: Int = position.x.toInt()
        val onRow: Int = position.y.toInt()
        val onBase: Base? = bmap.bases.firstOrNull { it.col == onCol && it.row == onRow }
        val onTerrain: TerrainTile = bmap[onCol, onRow]
        val terrainUpLeft: TerrainTile = bmap[onCol - 1, onRow - 1]
        val terrainUp: TerrainTile = bmap[onCol, onRow - 1]
        val terrainUpRight: TerrainTile = bmap[onCol + 1, onRow - 1]
        val terrainLeft: TerrainTile = bmap[onCol - 1, onRow]
        val terrainRight: TerrainTile = bmap[onCol + 1, onRow]
        val terrainDownLeft: TerrainTile = bmap[onCol - 1, onRow + 1]
        val terrainDown: TerrainTile = bmap[onCol, onRow + 1]
        val terrainDownRight: TerrainTile = bmap[onCol + 1, onRow + 1]
    }
}
