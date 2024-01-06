@file:Suppress("MemberVisibilityCanBePrivate")

package client

import bmap.Bmap
import bmap.BmapCode
import frame.Owner
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.Frame
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

class ClientApplicationModule {
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            window.alert("${throwable.message}\n${throwable.stackTraceToString()}")
        }
    }

    private val coroutineScope = CoroutineScope(coroutineExceptionHandler)

    private val httpClient = HttpClient { install(WebSockets) }

    private fun gameModuleFactory(
        sendChannel: SendChannel<Frame>,
        owner: Owner,
        bmap: Bmap,
        receiveChannel: ReceiveChannel<Frame>,
        bmapCode: BmapCode,
    ): GameModule = GameModuleImpl(
        coroutineScope = coroutineScope,
        sendChannel = sendChannel,
        owner = owner,
        bmap = bmap,
        receiveChannel = receiveChannel,
        bmapCode = bmapCode,
    )

    @Suppress("unused")
    private val clientApplication: ClientApplication = ClientApplicationImpl(
        coroutineScope = coroutineScope,
        httpClient = httpClient,
        gameModuleFactory = this::gameModuleFactory,
    )
}
