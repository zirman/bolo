package client

import org.koin.core.context.startKoin
import kotlin.time.measureTime

fun main() {
    if (runFibonacciBenchmark) {
        val i = 38

        measureTime {
            println("kotlin/js fib($i) ${fib(i)}")
        }.also { println("kotlin/js time: ${it.inWholeMilliseconds}") }
    } else {
        startKoin {
            modules(clientModule, gameModule)
            createEagerInstances()
        }
    }
}
