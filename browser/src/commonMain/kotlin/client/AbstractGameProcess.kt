package client

abstract class AbstractGameProcess : GameProcess {
    protected abstract val duplexIterator: DuplexIterator<Tick, Unit>

    final override suspend fun step(tick: Tick) {
        duplexIterator.next(tick)
    }

    suspend fun DuplexScope<Unit, Tick>.wait(time: Float): Tick {
        var x = 0f

        while (true) {
            val tick = yieldGet(Unit)
            x += tick.delta

            if (x >= time) {
                return tick
            }
        }
    }
}
