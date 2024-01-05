package client

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope

interface ClientApplication {
    val coroutineScope: CoroutineScope
    val httpClient: HttpClient
}
