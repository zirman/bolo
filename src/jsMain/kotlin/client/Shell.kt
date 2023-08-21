package client

import bmap.Entity
import bmap.Terrain
import kotlinx.coroutines.CoroutineScope
import math.V2
import math.add
import math.scale
import math.x
import math.y
import util.dirToVec
import util.isWater
import kotlin.math.max
import kotlin.math.min

@ExperimentalUnsignedTypes
class Shell(
    scope: CoroutineScope,
    game: Game,
    startPosition: V2,
    val bearing: Float,
    private val fromBoat: Boolean,
    private val sightRange: Float,
) : GamePublic by game, GeneratorLoop<Tick>(scope) {
    companion object {
        private const val SHELL_VEL: Float = 7f
        private const val LEAD = 1f / 2f
    }

    private val direction: V2 = dirToVec(bearing)

    var position: V2 = startPosition.add(direction.scale(LEAD))
        private set

    override suspend fun launch() {
        var timer: Float = (sightRange - LEAD) / SHELL_VEL

        doWhile { tick ->
            val delta = min(max(0f, timer), tick.delta)
            position = position.add(direction.scale((SHELL_VEL * delta)))
            timer -= delta

            val x: Int = position.x.toInt()
            val y: Int = position.y.toInt()
            val entity = bmap.getEntity(x, y)

            if ((fromBoat && entity.isShore(owner)) || entity.isShellable(owner)) {
                when (entity) {
                    is Entity.Pill -> pillDamage(bmap.pills.indexOfFirst { it === entity.ref })
                    is Entity.Base -> baseDamage(bmap.bases.indexOfFirst { it === entity.ref })
                    is Entity.Terrain -> {
                        // only damage road if it is a bridge
                        if (entity.terrain != Terrain.Road ||
                            ((isWater(bmap[x - 1, y]) && isWater(bmap[x + 1, y])) ||
                                    ((isWater(bmap[x, y - 1]) && isWater(bmap[x, y + 1]))))
                        ) {
                            terrainDamage(x, y)
//                        } else {
                            // TODO: detonate
                        }
                    }
                }

                false
            } else {
                timer > 0f
            }
        }
    }
}
