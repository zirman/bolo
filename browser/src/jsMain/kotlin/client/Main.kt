package client

import org.koin.core.context.startKoin

fun main() {
//    val i = 38
//
//    measureTime {
//        println("kotlin/js fib($i) ${fib(i)}")
//    }.also { println("kotlin/js time: ${it.inWholeMilliseconds}") }

    startKoin {
        modules(clientModule)
        createEagerInstances()
    }
}
