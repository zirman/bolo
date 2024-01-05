package client

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope

interface ClientApplicationModule {
    val coroutineExceptionHandler: CoroutineExceptionHandler
    val coroutineScope: CoroutineScope
    val httpClient: HttpClient
    val clientApplication: ClientApplication
}
