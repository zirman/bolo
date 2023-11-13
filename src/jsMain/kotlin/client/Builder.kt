package client

import bmap.Entity
import bmap.Terrain
import kotlinx.coroutines.CoroutineScope
import math.V2
import math.add
import math.mag
import math.scale
import math.sub
import math.v2
import math.x
import math.y
import kotlin.math.sqrt

sealed interface BuilderMission {
    data object HarvestTree : BuilderMission
    data object BuildWall : BuilderMission
    data object BuildRoad : BuilderMission
    data object BuildBoat : BuilderMission
    data object PlaceMine : BuilderMission
    data class PlacePill(val index: Int, val material: Int) : BuilderMission
    data class RepairPill(val index: Int, val material: Int) : BuilderMission
}

class Builder(
    scope: CoroutineScope,
    game: Game,
    startPosition: V2,
    private val targetX: Int,
    private val targetY: Int,
    private val buildOp: BuilderMission,
) : GamePublic by game, GeneratorLoop<Tick>(scope) {
    companion object {
        private const val BUILDER_RADIUS = 1f / 8f
        private const val MAX_SPEED = 25f / 8f

        private fun Entity.builderSpeed(owner: Int): Float =
            when (this) {
                is Entity.Pill ->
                    if (ref.armor > 0) 0f else MAX_SPEED

                is Entity.Base ->
                    if (ref.owner != 0xff && ref.owner != owner && ref.armor >= 5) 0f else MAX_SPEED

                is Entity.Terrain ->
                    when (terrain) {
                        Terrain.Swamp0,
                        Terrain.Swamp1,
                        Terrain.Swamp2,
                        Terrain.Swamp3,
                        Terrain.Crater,
                        Terrain.Rubble0,
                        Terrain.Rubble1,
                        Terrain.Rubble2,
                        Terrain.Rubble3,
                        Terrain.SwampMined,
                        Terrain.CraterMined,
                        Terrain.RubbleMined,
                        -> MAX_SPEED / 4f

                        Terrain.Tree,
                        Terrain.ForestMined,
                        -> MAX_SPEED / 2f

                        Terrain.Grass0,
                        Terrain.Grass1,
                        Terrain.Grass2,
                        Terrain.Grass3,
                        Terrain.GrassMined,
                        Terrain.Road,
                        Terrain.Boat,
                        Terrain.RoadMined,
                        -> MAX_SPEED

                        Terrain.River,
                        Terrain.Sea,
                        Terrain.Wall,
                        Terrain.WallDamaged0,
                        Terrain.WallDamaged1,
                        Terrain.WallDamaged2,
                        Terrain.WallDamaged3,
                        Terrain.SeaMined,
                        -> 0f
                    }
            }

        private fun Entity.isSolid(owner: Int): Boolean =
            when (this) {
                is Entity.Pill ->
                    ref.armor > 0

                is Entity.Base ->
                    ref.owner != 0xff && ref.owner != owner && ref.armor >= 5

                is Entity.Terrain ->
                    when (terrain) {
                        Terrain.Wall,
                        Terrain.WallDamaged0,
                        Terrain.WallDamaged1,
                        Terrain.WallDamaged2,
                        Terrain.WallDamaged3,
                        -> true

                        Terrain.River -> true
                        Terrain.Sea,
                        Terrain.SeaMined,
                        -> true

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
                        Terrain.Boat,
                        Terrain.SwampMined,
                        Terrain.CraterMined,
                        Terrain.RoadMined,
                        Terrain.ForestMined,
                        Terrain.RubbleMined,
                        Terrain.GrassMined,
                        -> false
                    }
            }
    }

    var position: V2 = startPosition
        private set

    private var material: Int = 0

    override suspend fun launch() {
        doWhile { tick ->
            val oldPosition = position
            val arrived = moveTo(tick.delta, v2(targetX + 0.5f, targetY + 0.5f))

            if (arrived) {
                when (buildOp) {
                    is BuilderMission.HarvestTree -> {
                        var waiting = true

                        buildTerrain(targetX, targetY, Terrain.Grass3) { success ->
                            if (success) {
                                material += 4
                            }

                            waiting = false
                        }

                        wait(1f)
                        doWhile { waiting }
                    }

                    is BuilderMission.BuildWall -> {
                        var waiting = true

                        buildTerrain(targetX, targetY, Terrain.Wall) { success ->
                            if (success) {
                                material -= 2
                            }

                            waiting = false
                        }

                        wait(1f)
                        doWhile { waiting }
                    }

                    is BuilderMission.BuildRoad -> {
                        var waiting = true

                        buildTerrain(targetX, targetY, Terrain.Road) { success ->
                            if (success) {
                                material -= 2
                            }

                            waiting = false
                        }

                        wait(1f)
                        doWhile { waiting }
                    }

                    is BuilderMission.BuildBoat -> {
                        var waiting = true

                        buildTerrain(targetX, targetY, Terrain.Wall) { success ->
                            if (success) {
                                material -= 4
                            }

                            waiting = false
                        }

                        wait(1f)
                        doWhile { waiting }
                    }

                    is BuilderMission.PlaceMine -> {
                    }

                    is BuilderMission.PlacePill -> {
                    }

                    is BuilderMission.RepairPill -> {
                    }
                }

                false
            } else {
                // check if stuck
                position.sub(oldPosition).mag() >= 0.001
            }
        }

        doWhile { tick ->
            val tank = tank

            if (tank != null) {
                if (moveTo(tick.delta, tank.position)) {
                    tank.material += material
                    isBuilderInTank = true
                    false
                } else {
                    true
                }
            } else {
                true
            }
        }
    }

    private fun moveTo(delta: Float, targetPosition: V2): Boolean {
        val diff = targetPosition.sub(position)
        val mag = diff.mag()
        val speed = this[position.x.toInt(), position.y.toInt()].builderSpeed(owner.int)
        val move = speed * delta

        return if (mag >= move) {
            position = diff.scale(move / mag).add(position).collisionDetect()
            false
        } else {
            true
        }
    }

    private fun V2.collisionDetect(): V2 {
        val rr: Float = BUILDER_RADIUS * BUILDER_RADIUS

        val fx: Int = x.toInt()
        val fy: Int = y.toInt()
        val lx: Float = x - fx.toFloat()
        val hx: Float = 1f - lx
        val ly: Float = y - fy.toFloat()
        val hy: Float = 1f - ly

        val lxc: Boolean = lx < BUILDER_RADIUS && bmap.getEntity(fx - 1, fy).isSolid(owner.int)
        val hxc: Boolean = hx < BUILDER_RADIUS && bmap.getEntity(fx + 1, fy).isSolid(owner.int)
        val lyc: Boolean = ly < BUILDER_RADIUS && bmap.getEntity(fx, fy - 1).isSolid(owner.int)
        val hyc: Boolean = hy < BUILDER_RADIUS && bmap.getEntity(fx, fy + 1).isSolid(owner.int)

        var sqr: Float = lx * lx + ly * ly
        if (!lxc && !lyc && sqr < rr && bmap.getEntity(fx - 1, fy - 1).isSolid(owner.int)) {
            val sca: Float = BUILDER_RADIUS / sqrt(sqr)
            return v2((fx + sca * lx), (fy + sca * ly))
        }

        sqr = hx * hx + ly * ly
        if (!hxc && !lyc && sqr < rr && bmap.getEntity(fx + 1, fy - 1).isSolid(owner.int)) {
            val sca: Float = BUILDER_RADIUS / sqrt(sqr)
            return v2((fx + (1 - sca * hx)), (fy + sca * ly))
        }

        sqr = lx * lx + hy * hy
        if (!lxc && !hyc && sqr < rr && bmap.getEntity(fx - 1, fy + 1).isSolid(owner.int)) {
            val sca: Float = BUILDER_RADIUS / sqrt(sqr)
            return v2((fx + sca * lx), (fy + (1f - sca * hy)))
        }

        sqr = hx * hx + hy * hy
        if (!hxc && !hyc && sqr < rr && bmap.getEntity(fx + 1, fy + 1).isSolid(owner.int)) {
            val sca: Float = BUILDER_RADIUS / sqrt(sqr)
            return v2((fx + (1f - sca * hx)), (fy + (1f - sca * hy)))
        }

        return v2(
            x = when {
                lxc -> fx + BUILDER_RADIUS
                hxc -> fx + (1f - BUILDER_RADIUS)
                else -> x
            },
            y = when {
                lyc -> fy + BUILDER_RADIUS
                hyc -> fy + (1f - BUILDER_RADIUS)
                else -> y
            }
        )
    }
}
