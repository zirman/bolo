package client

import bmap.Entity
import bmap.TerrainTile
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

class BuilderImpl(
    scope: CoroutineScope,
    game: Game,
    startPosition: V2,
    private val buildMission: BuilderMission,
) : GeneratorLoopImpl<Tick>(scope), Builder, Game by game {
    companion object {
        private const val BUILDER_RADIUS = 1f / 8f
        private const val MAX_SPEED = 25f / 8f

        private fun Entity.builderSpeed(owner: Int): Float =
            when (this) {
                is Entity.Pill -> if (ref.armor > 0) 0f else MAX_SPEED
                is Entity.Base -> if (ref.owner != 0xff && ref.owner != owner && ref.armor >= 5) 0f else MAX_SPEED
                is Entity.Terrain -> when (terrain) {
                    TerrainTile.Swamp0,
                    TerrainTile.Swamp1,
                    TerrainTile.Swamp2,
                    TerrainTile.Swamp3,
                    TerrainTile.Crater,
                    TerrainTile.Rubble0,
                    TerrainTile.Rubble1,
                    TerrainTile.Rubble2,
                    TerrainTile.Rubble3,
                    TerrainTile.SwampMined,
                    TerrainTile.CraterMined,
                    TerrainTile.RubbleMined,
                    -> MAX_SPEED / 4f

                    TerrainTile.Tree,
                    TerrainTile.TreeMined,
                    -> MAX_SPEED / 2f

                    TerrainTile.Grass0,
                    TerrainTile.Grass1,
                    TerrainTile.Grass2,
                    TerrainTile.Grass3,
                    TerrainTile.GrassMined,
                    TerrainTile.Road,
                    TerrainTile.Boat,
                    TerrainTile.RoadMined,
                    -> MAX_SPEED

                    TerrainTile.River,
                    TerrainTile.Sea,
                    TerrainTile.Wall,
                    TerrainTile.WallDamaged0,
                    TerrainTile.WallDamaged1,
                    TerrainTile.WallDamaged2,
                    TerrainTile.WallDamaged3,
                    TerrainTile.SeaMined,
                    -> {

                        0f
                    }
                }
            }

        private fun Entity.isSolid(owner: Int): Boolean {
            return when (this) {
                is Entity.Pill -> ref.armor > 0
                is Entity.Base -> ref.owner != 0xff && ref.owner != owner && ref.armor >= 5
                is Entity.Terrain -> when (terrain) {
                    TerrainTile.Wall,
                    TerrainTile.WallDamaged0,
                    TerrainTile.WallDamaged1,
                    TerrainTile.WallDamaged2,
                    TerrainTile.WallDamaged3,
                    -> true

                    TerrainTile.River -> true
                    TerrainTile.Sea,
                    TerrainTile.SeaMined,
                    -> true

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
                    TerrainTile.Boat,
                    TerrainTile.SwampMined,
                    TerrainTile.CraterMined,
                    TerrainTile.RoadMined,
                    TerrainTile.TreeMined,
                    TerrainTile.RubbleMined,
                    TerrainTile.GrassMined,
                    -> false
                }
            }
        }
    }

    override var position: V2 = startPosition
        private set

    private var material: Int = 0

    override suspend fun launch() {
        doWhile { tick ->
            val oldPosition = position
            val arrived = moveTo(tick.delta, v2(buildMission.x + 0.5f, buildMission.y + 0.5f))

            if (arrived) {
                when (buildMission) {
                    is BuilderMission.HarvestTree -> {
                        var waiting = true

                        buildTerrain(buildMission.x, buildMission.y, TerrainTile.Grass3) { success ->
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

                        buildTerrain(buildMission.x, buildMission.y, TerrainTile.Wall) { success ->
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

                        buildTerrain(buildMission.x, buildMission.y, TerrainTile.Road) { success ->
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

                        buildTerrain(buildMission.x, buildMission.y, TerrainTile.Boat) { success ->
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
            tank?.run {
                if (moveTo(tick.delta, position)) {
                    material += material
                    hasBuilder = true
                    nextBuilderMission?.run {
                        launchBuilder(position, this)
                        nextBuilderMission = null
                    }
                    false
                } else {
                    true
                }
            } ?: true
        }
    }

    private fun moveTo(delta: Float, targetPosition: V2): Boolean {
        val diff = targetPosition.sub(position)
        val mag = diff.mag()
        val x = position.x.toInt()
        val y = position.y.toInt()
        val speed = this[x, y].builderSpeed(owner.int).let {
            if (it == 0.0f && x == buildMission.x && y == buildMission.y) {
                MAX_SPEED
            } else {
                it
            }
        }
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

        fun isSolid(x: Int, y: Int): Boolean {
            return (x != buildMission.x ||
                    y != buildMission.y) && bmap.getEntity(x, y).isSolid(owner.int)
        }

        val lxc: Boolean = lx < BUILDER_RADIUS && isSolid(fx - 1, fy)
        val hxc: Boolean = hx < BUILDER_RADIUS && isSolid(fx + 1, fy)
        val lyc: Boolean = ly < BUILDER_RADIUS && isSolid(fx, fy - 1)
        val hyc: Boolean = hy < BUILDER_RADIUS && isSolid(fx, fy + 1)

        var sqr: Float = lx * lx + ly * ly
        if (!lxc && !lyc && sqr < rr && isSolid(fx - 1, fy - 1)) {
            val sca: Float = BUILDER_RADIUS / sqrt(sqr)
            return v2((fx + sca * lx), (fy + sca * ly))
        }

        sqr = hx * hx + ly * ly
        if (!hxc && !lyc && sqr < rr && isSolid(fx + 1, fy - 1)) {
            val sca: Float = BUILDER_RADIUS / sqrt(sqr)
            return v2((fx + (1 - sca * hx)), (fy + sca * ly))
        }

        sqr = lx * lx + hy * hy
        if (!lxc && !hyc && sqr < rr && isSolid(fx - 1, fy + 1)) {
            val sca: Float = BUILDER_RADIUS / sqrt(sqr)
            return v2((fx + sca * lx), (fy + (1f - sca * hy)))
        }

        sqr = hx * hx + hy * hy
        if (!hxc && !hyc && sqr < rr && isSolid(fx + 1, fy + 1)) {
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
