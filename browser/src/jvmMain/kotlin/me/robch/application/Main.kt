package me.robch.application

import client.fib
import client.runFibonacciBenchmark
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlin.time.measureTime

fun main() {
    if (runFibonacciBenchmark) {
        val i = 38

        measureTime {
            println("kotlin/jvm fib($i) ${fib(i)}")
        }.also { println("kotlin/jvm time: ${it.inWholeMilliseconds} ms") }
    }

    embeddedServer(
        factory = Netty,
        port = 8080,
        module = Application::ktorModule
    ).start(wait = true)
}
