package client

abstract class AbstractGameProcess : GameProcess {
    suspend fun ConsumerScope<Tick>.wait(time: Float): Tick {
        var x = 0f

        while (true) {
            val tick = next()
            x += tick.delta

            if (x >= time) {
                return tick
            }
        }
    }
}
