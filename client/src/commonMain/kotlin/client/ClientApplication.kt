package client

import client.bmap.BmapCodeReader
import client.bmap.BmapDamageReader
import client.bmap.loadCodes
import common.bmap.BmapReader
import common.frame.Owner
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.wss
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

interface ClientApplication

@SingleIn(AppScope::class)
@Inject
class ClientApplicationImpl(
    scope: CoroutineScope,
    val httpClient: HttpClient,
    val gameGraphFactory: GameGraph.Factory,
) : ClientApplication {
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
        gameGraphFactory.createGameGraph(
            outgoing = outgoing,
            incoming = incoming,
            bmap = bmap,
            bmapCode = bmapCode,
            owner = owner,
        ).game
        awaitCancellation()
    }
}
