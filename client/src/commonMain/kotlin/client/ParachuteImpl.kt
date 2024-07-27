package client

import client.math.V2
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import kotlin.random.Random

class ParachuteImpl(
    game: Game,
    targetPosition: V2,
) : AbstractGameProcess(), Parachute, Game by game, KoinComponent {
    companion object {
        private const val SPEED = 0.5859375f
    }

    override var position: V2 = bmap.starts[Random.Default.nextInt(bmap.starts.size)]
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
                tick.set(get<Builder> { parametersOf(position, null, 0, 0) })
                break
            }
        }
    }
}
