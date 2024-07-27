package client

import common.bmap.Entity
import common.bmap.TerrainTile
import client.math.V2
import client.math.squared
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import kotlin.math.min
import kotlin.math.sqrt

enum class BuildResult {
    Success,
    Failed,
    Mined,
}

class BuilderImpl(
    game: Game,
    startPosition: V2,
    private val buildMission: BuilderMission?,
    private var material: Int,
    private var mines: Int,
) : AbstractGameProcess(), Builder, Game by game, KoinComponent {
    class BuilderKilled(val listIterator: MutableListIterator<GameProcess>?) : Throwable()

    companion object {
        private const val BUILDER_RADIUS = 1f / 8f
        private const val MAX_SPEED = 25f / 8f
        private const val BUILD_TIME = .25f
        const val TREE_MATERIAL = 4
        const val ROAD_MATERIAL = 2
        const val WALL_MATERIAL = 2
        const val BOAT_MATERIAL = 20
        const val PILL_MATERIAL = 4

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
                    -> 0f
                }
            }

        private fun Entity.isSolid(owner: Int): Boolean = when (this) {
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

    override var position: V2 = startPosition
        private set

    private suspend fun ConsumerScope<Tick>.harvest(col: Int, row: Int): Tick {
        var buildResult: BuildResult? = null
        var done = false

        buildTerrain(col, row, TerrainTile.Grass3) {
            buildResult = it
        }

        // wait for servers response
        var timeDelta = 0f

        while (true) {
            val tick = next()
            timeDelta += tick.delta

            when (buildResult) {
                BuildResult.Success -> {
                    material = TREE_MATERIAL
                    done = true
                }

                BuildResult.Failed -> {
                    done = true
                }

                BuildResult.Mined -> {
                    throw BuilderKilled(tick)
                }

                null -> {
                }
            }

            buildResult = null

            if (done && timeDelta >= BUILD_TIME) {
                return tick
            }
        }
    }

    private suspend fun ConsumerScope<Tick>.build(
        col: Int,
        row: Int,
        terrainTile: TerrainTile,
    ): Tick {
        var buildResult: BuildResult? = null
        var done = false

        buildTerrain(col, row, terrainTile) {
            buildResult = it
        }

        // wait for servers response
        var timeDelta = 0f

        while (true) {
            val tick = next()
            timeDelta += tick.delta

            when (buildResult) {
                BuildResult.Success -> {
                    material = 0
                    done = true
                }

                BuildResult.Failed -> {
                    done = true
                }

                BuildResult.Mined -> {
                    throw BuilderKilled(tick)
                }

                null -> {
                }
            }

            buildResult = null

            if (done && timeDelta >= BUILD_TIME) {
                return tick
            }
        }
    }

    private suspend fun ConsumerScope<Tick>.placeMine(col: Int, row: Int): Tick {
        var buildResult: BuildResult? = null
        var done = false

        mineTerrain(col, row) {
            buildResult = it
        }

        // wait for servers response
        var timeDelta = 0f

        while (true) {
            val tick = next()
            timeDelta += tick.delta

            when (buildResult) {
                BuildResult.Success -> {
                    mines = 0
                    done = true
                }

                BuildResult.Failed -> {
                    done = true
                }

                BuildResult.Mined -> {
                    throw BuilderKilled(tick)
                }

                null -> {
                }
            }

            buildResult = null

            if (done && timeDelta >= BUILD_TIME) {
                return tick
            }
        }
    }

    override val consumer: Consumer<Tick> = consumer {
        try {
            buildMission?.run { gotoTarget(this) }
            gotoTank()
        } catch (builderKilled: BuilderKilled) {
            tank?.setNextBuilderMission(null)
            builderKilled.listIterator?.set(get<Parachute> { parametersOf(position) })
        }
    }

    private suspend fun ConsumerScope<Tick>.gotoTarget(buildMission: BuilderMission): Tick {
        val targetPosition: V2 = V2.create(buildMission.col + .5f, buildMission.row + .5f)

        while (true) {
            val tick = next()
            val diff = targetPosition.sub(position)
            val mag = diff.magnitude
            val col = position.x.toInt()
            val row = position.y.toInt()

            val speed = this@BuilderImpl[col, row].builderSpeed(owner.int).let {
                if (it == 0f && col == buildMission.col && row == buildMission.row) {
                    MAX_SPEED
                } else {
                    it
                }
            }

            val move = speed * tick.delta

            if (mag > move) {
                val oldPosition = position
                position = diff.scale(move / mag).add(position).collisionDetect()

                // check if stuck
                if (position.sub(oldPosition).magnitude < 0.001) {
                    return tick
                }
            } else {
                when (buildMission) {
                    is BuilderMission.HarvestTree -> {
                        harvest(buildMission.col, buildMission.row)
                    }

                    is BuilderMission.BuildWall -> {
                        build(buildMission.col, buildMission.row, TerrainTile.Wall)
                    }

                    is BuilderMission.BuildRoad -> {
                        build(buildMission.col, buildMission.row, TerrainTile.Road)
                    }

                    is BuilderMission.BuildBoat -> {
                        build(buildMission.col, buildMission.row, TerrainTile.Boat)
                    }

                    is BuilderMission.PlaceMine -> {
                        placeMine(buildMission.col, buildMission.row)
                    }

                    is BuilderMission.PlacePill -> {
//                        placeMine(buildMission.col, buildMission.row) { buildResult ->
//                            when (buildResult) {
//                                BuildResult.Success -> {
//                                }
//
//                                BuildResult.Failed -> {
//                                }
//
//                                BuildResult.Mined -> {
//                                    set(get<Parachute>())
//                                }
//                            }
//                        }
                    }

                    is BuilderMission.RepairPill -> {
                    }
                }

                return tick
            }
        }
    }

    private suspend fun ConsumerScope<Tick>.gotoTank(): Tick {
        while (true) {
            val tick = next()
            val tank = tank ?: continue
            val diff = tank.position.sub(position)
            val mag = diff.magnitude
            val col = position.x.toInt()
            val row = position.y.toInt()

            val speed = this@BuilderImpl[col, row].builderSpeed(owner.int).let { speed ->
                if (speed == 0f && col == buildMission?.col && row == buildMission.row) {
                    MAX_SPEED
                } else {
                    speed
                }
            }

            val move = speed * tick.delta

            if (mag >= move) {
                position = diff.scale(move / mag).add(position).collisionDetect()
            } else {
                tank.material = min(tank.material + material, TankImpl.TANK_MATERIAL_MAX)
                setMaterialStatusBar(tank.material.toFloat() / TankImpl.TANK_MATERIAL_MAX)
                material = 0

                tank.mines = min(tank.mines + mines, TankImpl.TANK_MINES_MAX)
                setMinesStatusBar(tank.mines.toFloat() / TankImpl.TANK_MINES_MAX)
                mines = 0

                val nextBuilderMission = tank.getNextBuilderMission()
                if (nextBuilderMission != null) {
                    nextBuilderMission.builderMode.tryBuilderAction(
                        tank = tank,
                        col = nextBuilderMission.col,
                        row = nextBuilderMission.row,
                    )?.run { tick.set(this) }
                } else {
                    tank.hasBuilder = true
                    tick.remove()
                }

                return tick
            }
        }
    }

    private fun V2.collisionDetect(): V2 {
        val rr: Float = BUILDER_RADIUS.squared

        val fx: Int = x.toInt()
        val fy: Int = y.toInt()
        val lx: Float = x - fx.toFloat()
        val hx: Float = 1f - lx
        val ly: Float = y - fy.toFloat()
        val hy: Float = 1f - ly

        fun isSolid(col: Int, row: Int): Boolean {
            return (col != buildMission?.col || row != buildMission.row) &&
                    bmap.getEntity(col, row).isSolid(owner.int)
        }

        val lxc: Boolean = lx < BUILDER_RADIUS && isSolid(fx - 1, fy)
        val hxc: Boolean = hx < BUILDER_RADIUS && isSolid(fx + 1, fy)
        val lyc: Boolean = ly < BUILDER_RADIUS && isSolid(fx, fy - 1)
        val hyc: Boolean = hy < BUILDER_RADIUS && isSolid(fx, fy + 1)

        var sqr: Float = lx * lx + ly * ly
        if (!lxc && !lyc && sqr < rr && isSolid(fx - 1, fy - 1)) {
            val sca: Float = BUILDER_RADIUS / sqrt(sqr)
            return V2.create((fx + sca * lx), (fy + sca * ly))
        }

        sqr = hx * hx + ly * ly
        if (!hxc && !lyc && sqr < rr && isSolid(fx + 1, fy - 1)) {
            val sca: Float = BUILDER_RADIUS / sqrt(sqr)
            return V2.create((fx + (1 - sca * hx)), (fy + sca * ly))
        }

        sqr = lx * lx + hy * hy
        if (!lxc && !hyc && sqr < rr && isSolid(fx - 1, fy + 1)) {
            val sca: Float = BUILDER_RADIUS / sqrt(sqr)
            return V2.create((fx + sca * lx), (fy + (1f - sca * hy)))
        }

        sqr = hx * hx + hy * hy
        if (!hxc && !hyc && sqr < rr && isSolid(fx + 1, fy + 1)) {
            val sca: Float = BUILDER_RADIUS / sqrt(sqr)
            return V2.create((fx + (1f - sca * hx)), (fy + (1f - sca * hy)))
        }

        return V2.create(
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
