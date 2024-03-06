package client

interface GameProcess {
    val consumer: Consumer<Tick>
}
