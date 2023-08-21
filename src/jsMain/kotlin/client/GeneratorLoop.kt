package client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

abstract class GeneratorLoop<T>(scope: CoroutineScope) {
    private val channel = Channel<T>(Channel.RENDEZVOUS)
    val job = scope.launch { launch() }

    suspend fun resumeWith(x: T) {
        channel.send(x)
    }

    // TODO: refactor to remove suspend from lambda to prevent accidental deadlocks
    suspend fun doWhile(block: suspend (T) -> Boolean) {
        while (true) {
            val x = channel.receive()

            if (block(x).not()) {
                break
            }
        }
    }

    abstract suspend fun launch()
}

suspend fun GeneratorLoop<Tick>.wait(time: Float) {
    var x = 0f

    doWhile {
        x += it.delta
        x < time
    }
}
