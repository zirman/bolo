package client

import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(clientModule)
    }

    ClientApplication()
}
