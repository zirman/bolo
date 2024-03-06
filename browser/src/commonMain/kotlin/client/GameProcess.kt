package client

import kotlinx.coroutines.CoroutineScope

interface GameProcess {
    fun launchIn(scope: CoroutineScope)
    suspend fun run(): Tick
    suspend fun step(tick: Tick)
}
