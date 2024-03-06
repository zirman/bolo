package client

import kotlinx.coroutines.CoroutineScope

class LogicGameProcess(
    scope: CoroutineScope,
    private val block: suspend LogicGameProcess.() -> Tick,
) : AbstractGameProcess() {
    init {
        launchIn(scope)
    }

    override suspend fun run(): Tick {
        return block()
    }
}
