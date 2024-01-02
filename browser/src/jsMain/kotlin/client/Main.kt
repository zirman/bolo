package client

import org.koin.core.context.startKoin

@JsExport
fun blah() {
    println("wat")
}

fun main() {
    startKoin {
        modules(clientModule)
        createEagerInstances()
    }
}
