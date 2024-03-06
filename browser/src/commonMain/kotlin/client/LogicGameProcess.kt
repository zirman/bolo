package client

class LogicGameProcess(
    private val block: suspend ConsumerScope<Tick>.() -> Tick,
) : AbstractGameProcess() {
    override val consumer: Consumer<Tick> = consumer {
        block()
    }
}
