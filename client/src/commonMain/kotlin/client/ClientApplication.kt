package client

import common.bmap.BmapReader
import common.bmap.loadCodes
import common.frame.Owner
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.wss
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

interface ClientApplication

class ClientApplicationImpl(
    val scope: CoroutineScope,
    val httpClient: HttpClient,
) : ClientApplication, KoinComponent {
    init {
        scope.launch(CoroutineName("ClientApplicationImpl")) {
            httpClient.wss(
                host = getLocationHost(),
                path = "/wss",
            ) {
                handleSession()
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.handleSession() {
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

        // instantiate game
        get<Game> {
            parametersOf(
                outgoing,
                owner,
                bmap,
                incoming,
                bmapCode,
            )
        }

        awaitCancellation()
    }
}
