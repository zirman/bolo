package client

import bmap.Bmap
import bmap.BmapCode
import bmap.BmapCodeReader
import bmap.BmapDamageReader
import bmap.BmapReader
import bmap.loadCodes
import bmap.toBmapExtra
import frame.Owner
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.ws
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

interface GameModule
interface ClientApplication

class ClientApplicationImpl(
    val coroutineScope: CoroutineScope,
    val httpClient: HttpClient,
    val gameModuleFactory: (
        sendChannel: SendChannel<Frame>,
        owner: Owner,
        bmap: Bmap,
        receiveChannel: ReceiveChannel<Frame>,
        bmapCode: BmapCode,
    ) -> GameModule,
) : ClientApplication {
    init {
        coroutineScope.launch {
            httpClient.ws(
                host = getLocationHost(),
//            port = window.location.port.toInt(),
                path = "/ws",
            ) {
                handleSession()
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.handleSession() {
        try {
            val buffer = run { incoming.receive() as Frame.Binary }.readBytes().asUByteArray()
            val bmapReader = BmapReader(offset = 0, buffer)
            val bmapDamageReader = BmapDamageReader(offset = bmapReader.offset, bmap = bmapReader.bmap, buffer)
            val bmapCodeReader = BmapCodeReader(offset = bmapDamageReader.offset, buffer)

            val bmapExtra = buffer
                .sliceArray(bmapCodeReader.offset..<buffer.size)
                .toByteArray()
                .toBmapExtra()

            val bmap = bmapReader.bmap
            val bmapCode = bmapCodeReader.bmapCode
            val owner = Owner(bmapExtra.owner)
            bmapExtra.loadCodes(bmap)

            gameModuleFactory(
                outgoing,
                owner,
                bmap,
                incoming,
                bmapCode,
            )

            awaitCancellation()
        } catch (error: Throwable) {
            currentCoroutineContext().ensureActive()
            error.printStackTrace()
            throw error
        }
    }
}
