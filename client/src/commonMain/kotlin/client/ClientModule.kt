package client

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

val clientModule = module {
    single<CoroutineScope> {
        CoroutineScope(object : AbstractCoroutineContextElement(CoroutineExceptionHandler), CoroutineExceptionHandler {
            private var once = false

            override fun handleException(context: CoroutineContext, exception: Throwable) {
                if (once || exception is CancellationException) return
                once = true
                exception.printStackTrace()
                alert(exception.message ?: "An error occurred")
            }
        })
    }

    single<HttpClient> {
        HttpClient { install(WebSockets) }
    }

    single<ClientApplication> {
        ClientApplicationImpl(
            scope = get(),
            httpClient = get(),
        )
    } withOptions {
        createdAtStart()
    }
}
