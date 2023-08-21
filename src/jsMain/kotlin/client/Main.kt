@file:OptIn(ExperimentalUnsignedTypes::class, DelicateCoroutinesApi::class, ExperimentalSerializationApi::class)

package client

import bmap.BmapCodeReader
import bmap.BmapDamageReader
import bmap.BmapExtra
import bmap.BmapReader
import bmap.loadCodes
import frame.FrameServer
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.ws
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import util.awaitPair

fun main() {
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            window.alert("${throwable.message}\n${throwable.stackTraceToString()}")
        }
    }

    GlobalScope.launch(coroutineExceptionHandler) {
        gl // force initialization
        checkWebSocket()
        checkWebRTC()

        val (tileProgram, spriteProgram) =
            awaitPair(
                scope = this,
                { gl.createTileProgram() },
                { gl.createSpriteProgram() },
            )

        HttpClient { install(WebSockets) }.ws(
            host = window.location.host,
//            port = window.location.port.toInt(),
            path = "/ws",
        ) {
            val buffer = (incoming.receive() as Frame.Binary).readBytes().asUByteArray()
            val bmapReader = BmapReader(offset = 0, buffer)
            val bmapDamageReader = BmapDamageReader(offset = bmapReader.offset, bmap = bmapReader.bmap, buffer)
            val bmapCodeReader = BmapCodeReader(offset = bmapDamageReader.offset, buffer)

            val bmapExtra = ProtoBuf
                .decodeFromByteArray(
                    BmapExtra.serializer(),
                    buffer.sliceArray(bmapCodeReader.offset.until(buffer.size)).toByteArray(),
                )

            val bmap = bmapReader.bmap
            val bmapCode = bmapCodeReader.bmapCode
            val owner = bmapExtra.owner
            bmapExtra.loadCodes(bmap)

            val game = Game(outgoing, owner, bmap, bmapCode, tileProgram, spriteProgram)

            // emit decoded server frames to game
            incoming
                .consumeAsFlow()
                .transform { frame ->
                    when (frame) {
                        is Frame.Binary -> emit(frame)
                        is Frame.Text -> throw Exception("unexpected text frame")
                        is Frame.Close -> throw Exception("connection closed by server")
                        is Frame.Ping -> Unit
                        is Frame.Pong -> Unit
                    }
                }
                .map { ProtoBuf.decodeFromByteArray(FrameServer.serializer(), it.readBytes()) }
                .map { game.frameServerFlow.emit(it) }
                .launchIn(this)

            // suspends until game ends or error
            game.run()
        }
    }
}
