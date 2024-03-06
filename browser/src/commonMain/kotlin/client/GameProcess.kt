package client

interface GameProcess {
    suspend fun step(tick: Tick)
}
