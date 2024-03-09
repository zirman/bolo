package client

import bmap.Entity
import bmap.TerrainTile
import math.V2
import math.clamp
import math.dirToVec

class ShellImpl(
    game: Game,
    startPosition: V2,
    override val bearing: Float,
    private val fromBoat: Boolean,
    private val sightRange: Float,
) : AbstractGameProcess(), Shell, Game by game {
    companion object {
        private const val SHELL_VEL: Float = 7f
        private const val LEAD = 1f / 2f
    }

    private val direction: V2 = dirToVec(bearing)

    override var position: V2 = startPosition.add(direction.scale(LEAD))
        private set

    override val consumer: Consumer<Tick> = consumer {
        var timer: Float = (sightRange - LEAD) / SHELL_VEL

        while (true) {
            val tick = next()
            val delta = timer.clamp(0f, tick.delta)
            position = position.add(direction.scale((SHELL_VEL * delta)))
            timer -= delta

            val col: Int = position.x.toInt()
            val row: Int = position.y.toInt()
            val entity = bmap.getEntity(col, row)

            if ((fromBoat && entity.isShore(owner.int)) || entity.isShellable(owner.int)) {
                when (entity) {
                    is Entity.Pill -> {
                        pillDamage(bmap.pills.indexOfFirst { it === entity.ref })
                        tick.remove()
                        return@consumer
                    }

                    is Entity.Base -> {
                        baseDamage(bmap.bases.indexOfFirst { it === entity.ref })
                        tick.remove()
                        return@consumer
                    }

                    is Entity.Terrain -> {
                        // only damage road if it is a bridge
                        if (entity.terrain != TerrainTile.Road ||
                            ((isWater(bmap[col - 1, row]) && isWater(bmap[col + 1, row])) ||
                                    ((isWater(bmap[col, row - 1]) && isWater(bmap[col, row + 1]))))
                        ) {
                            terrainDamage(col, row)
                        }

                        tick.remove()
                        return@consumer
                    }
                }
            } else if (timer <= 0f) {
                tick.remove()
                return@consumer
            }
        }
    }
}
