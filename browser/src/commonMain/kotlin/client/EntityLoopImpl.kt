package client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

abstract class EntityLoopImpl : EntityLoop {
    private val _tickChannel = Channel<Tick>()
    protected val tickChannel: ReceiveChannel<Tick> get() = _tickChannel

    final override fun launchIn(scope: CoroutineScope) {
        // Unconfined allows the step() method to resume the run() method
        scope.launch(Dispatchers.Unconfined) {
            try {
                run()
            } finally {
                _tickChannel.close()
            }
        }
    }

    final override suspend fun step(tick: Tick) {
        _tickChannel.send(tick)
    }

    suspend fun wait(time: Float): Tick {
        var x = 0f

        while (true) {
            val tick = tickChannel.receive()
            x += tick.delta

            if (x >= time) {
                return tick
            }
        }
    }
}
