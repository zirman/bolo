@file:OptIn(
    ExperimentalUnsignedTypes::class,
    kotlinx.coroutines.DelicateCoroutinesApi::class,
    kotlinx.serialization.ExperimentalSerializationApi::class
)

package client

import bmap.BmapCodeReader
import bmap.BmapDamageReader
import bmap.BmapReader
import bmap.loadCodes
import bmap.toBmapExtra
import frame.FrameServer
import frame.Owner
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.ws
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.serialization.protobuf.ProtoBuf
import org.khronos.webgl.WebGLRenderingContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class ClientApplication : KoinComponent {
    private val coroutineExceptionHandler: CoroutineExceptionHandler by inject()
    private val gl: WebGLRenderingContext by inject(named(Element.WebGL))

    init {
        checkWebSocket()
        checkWebRTC()

        GlobalScope.launch(coroutineExceptionHandler) {
            run()
        }
    }

    private suspend fun run() = coroutineScope {
        val tileProgram = async { gl.createTileProgram() }
        val spriteProgram = async { gl.createSpriteProgram() }

        HttpClient { install(WebSockets) }.ws(
            host = window.location.host,
//            port = window.location.port.toInt(),
            path = "/ws",
        ) {
            handleSession(tileProgram.await(), spriteProgram.await())
        }
    }

    private suspend fun DefaultClientWebSocketSession.handleSession(
        tileProgram: TileProgram,
        spriteProgram: SpriteProgram,
    ) {
        try {
            val buffer = run { incoming.receive() as Frame.Binary }.readBytes().asUByteArray()
            val bmapReader = BmapReader(offset = 0, buffer)
            val bmapDamageReader = BmapDamageReader(offset = bmapReader.offset, bmap = bmapReader.bmap, buffer)
            val bmapCodeReader = BmapCodeReader(offset = bmapDamageReader.offset, buffer)

            val bmapExtra = buffer
                .sliceArray(bmapCodeReader.offset.until(buffer.size))
                .toByteArray()
                .toBmapExtra()

            val bmap = bmapReader.bmap
            val bmapCode = bmapCodeReader.bmapCode
            val owner = Owner(bmapExtra.owner)
            bmapExtra.loadCodes(bmap)

            val game = Game(
                sendChannel = outgoing,
                owner = owner,
                bmap = bmap,
                bmapCode = bmapCode,
                tileProgram = tileProgram,
                spriteProgram = spriteProgram,
                scope = this,
            )

            // emit decoded server frames to game
            incoming
                .consumeAsFlow()
                .transform { frame ->
                    when (frame) {
                        is Frame.Binary -> emit(frame)
                        is Frame.Text -> throw IllegalStateException("unexpected text frame")
                        is Frame.Close -> throw IllegalStateException("connection closed by server")
                        is Frame.Ping -> Unit
                        is Frame.Pong -> Unit
                    }
                }
                .map { ProtoBuf.decodeFromByteArray(FrameServer.serializer(), it.readBytes()) }
                .map { game.frameServerFlow.emit(it) }
                .launchIn(this)

            game.launch()
            awaitCancellation()
        } catch (error: Throwable) {
            currentCoroutineContext().ensureActive()
            error.printStackTrace()
            throw error
        }
    }
}
