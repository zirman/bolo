package client

abstract class AbstractGameProcess : GameProcess {
    suspend fun ConsumerScope<Tick>.wait(time: Float): Tick {
        var totalDelta = 0f

        while (true) {
            val tick = next()
            totalDelta += tick.delta

            if (totalDelta >= time) {
                return tick
            }
        }
    }
}
