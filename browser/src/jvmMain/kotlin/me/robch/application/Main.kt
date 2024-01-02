package me.robch.application

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
//    val i = 38
//
//    measureTime {
//        println("kotlin/wasm fib($i) ${fib(i)}")
//    }.also { println("kotlin/wasm time: ${it.inWholeMilliseconds} ms") }

    embeddedServer(
        factory = Netty,
        port = 8080,
        module = Application::ktorModule
    ).start(wait = true)
}
