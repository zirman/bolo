package client

import kotlin.time.measureTime

fun main() {
    ClientApplicationModule()

    if (runFibonacciBenchmark) {
        val i = 38

        measureTime {
            println("kotlin/js fib($i) ${fib(i)}")
        }.also { println("kotlin/js time: ${it.inWholeMilliseconds}") }
    }
}
