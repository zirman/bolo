package client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

interface GeneratorLoop<T> {
    val job: Job
    suspend fun resumeWith(x: T)
    suspend fun doWhile(block: suspend (T) -> Boolean)
    suspend fun launch()
}

abstract class GeneratorLoopImpl<T>(scope: CoroutineScope) : GeneratorLoop<T> {
    private val channel = Channel<T>(Channel.RENDEZVOUS)
    override val job = scope.launch { launch() }

    override suspend fun resumeWith(x: T) {
        channel.send(x)
    }

    // TODO: refactor to remove suspend from lambda to prevent accidental deadlocks
    override suspend fun doWhile(block: suspend (T) -> Boolean) {
        for (x in channel) {
            if (block(x).not()) {
                break
            }
        }
    }

    abstract override suspend fun launch()
}

suspend fun GeneratorLoopImpl<Tick>.wait(time: Float) {
    var x = 0f

    doWhile {
        x += it.delta
        x < time
    }
}
