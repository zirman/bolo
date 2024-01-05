package client

import kotlinx.coroutines.Job

interface GeneratorLoop<T> {
    val job: Job
    suspend fun resumeWith(x: T)
    suspend fun doWhile(block: suspend (T) -> Boolean)
    suspend fun launch()

    suspend fun GeneratorLoop<Tick>.wait(time: Float) {
        var x = 0f

        doWhile {
            x += it.delta
            x < time
        }
    }
}
