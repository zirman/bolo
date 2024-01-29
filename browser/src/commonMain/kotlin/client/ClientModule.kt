package client

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import kotlin.coroutines.cancellation.CancellationException

val clientModule = module {
    single<CoroutineScope> {
        CoroutineScope(CoroutineExceptionHandler { _, throwable ->
            if (throwable is CancellationException) return@CoroutineExceptionHandler
            alert("${throwable.message}\n${throwable.stackTraceToString()}")
        })
    }

    single<HttpClient> {
        HttpClient { install(WebSockets) }
    }

    single<ClientApplication> {
        ClientApplicationImpl(
            coroutineScope = get(),
            httpClient = get(),
        )
    } withOptions {
        createdAtStart()
    }
}
