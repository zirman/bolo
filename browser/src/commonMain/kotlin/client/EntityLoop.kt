package client

import kotlinx.coroutines.CoroutineScope

interface EntityLoop {
    fun launchIn(scope: CoroutineScope)
    suspend fun run(): Tick
    suspend fun step(tick: Tick)
}
