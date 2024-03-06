package client

class LogicGameProcess(
    private val block: suspend DuplexScope<Unit, Tick>.() -> Tick,
) : AbstractGameProcess() {
    override val duplexIterator: DuplexIterator<Tick, Unit> = duplexIterator {
        block()
    }
}
