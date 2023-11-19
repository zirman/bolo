package client

import org.koin.core.context.startKoin

fun main() {
    val koin = startKoin {
        modules(clientModule)
    }.koin

    koin.get<ClientApplication>()
}
