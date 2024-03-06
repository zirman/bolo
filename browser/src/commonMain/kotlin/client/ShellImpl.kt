package client

import bmap.Entity
import bmap.TerrainTile
import kotlinx.coroutines.CoroutineScope
import math.V2
import math.add
import math.clamp
import math.dirToVec
import math.scale
import math.x
import math.y

class ShellImpl(
    scope: CoroutineScope,
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

    init {
        launchIn(scope)
    }

    private val direction: V2 = dirToVec(bearing)

    override var position: V2 = startPosition.add(direction.scale(LEAD))
        private set

    override suspend fun run(): Tick {
        var timer: Float = (sightRange - LEAD) / SHELL_VEL

        while (true) {
            val tick = tickChannel.receive()
            val delta = timer.clamp(0f, tick.delta)
            position = position.add(direction.scale((SHELL_VEL * delta)))
            timer -= delta

            val x: Int = position.x.toInt()
            val y: Int = position.y.toInt()
            val entity = bmap.getEntity(x, y)

            if ((fromBoat && entity.isShore(owner.int)) || entity.isShellable(owner.int)) {
                when (entity) {
                    is Entity.Pill -> {
                        pillDamage(bmap.pills.indexOfFirst { it === entity.ref })
                        tick.remove()
                        return tick
                    }

                    is Entity.Base -> {
                        baseDamage(bmap.bases.indexOfFirst { it === entity.ref })
                        tick.remove()
                        return tick
                    }

                    is Entity.Terrain -> {
                        // only damage road if it is a bridge
                        if (entity.terrain != TerrainTile.Road ||
                            ((isWater(bmap[x - 1, y]) && isWater(bmap[x + 1, y])) ||
                                    ((isWater(bmap[x, y - 1]) && isWater(bmap[x, y + 1]))))
                        ) {
                            terrainDamage(x, y)
                        }

                        tick.remove()
                        return tick
                    }
                }
            } else if (timer <= 0f) {
                tick.remove()
                return tick
            }
        }
    }
}
