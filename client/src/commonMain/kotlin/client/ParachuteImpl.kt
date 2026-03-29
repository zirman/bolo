package client

import client.math.V2
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.random.Random

@AssistedInject
class ParachuteImpl(
    game: Game,
    private val builderFactory: BuilderImpl.Factory,
    @Assisted targetPosition: V2,
) : AbstractGameProcess(), Parachute, Game by game {
    @AssistedFactory
    interface Factory {
        fun create(targetPosition: V2): ParachuteImpl
    }

    companion object {
        private const val SPEED = 0.5859375f
    }

    override var position: V2 = bmap.starts[Random.nextInt(bmap.starts.size)]
        .let { V2.create(it.col + .5f, it.row + .5f) }
        private set

    override val consumer: Consumer<Tick> = consumer {
        while (true) {
            val tick = next()
            val diff = targetPosition.sub(position)
            val mag = diff.magnitude
            val move = SPEED * tick.delta

            if (mag > move) {
                position = diff.scale(move / mag).add(position)
            } else {
                tick.set(
                    builderFactory.create(
                        startPosition = position,
                        buildMission = null,
                        material = 0,
                        mines = 0,
                        pillIndex = null,
                    )
                )
                break
            }
        }
    }
}
