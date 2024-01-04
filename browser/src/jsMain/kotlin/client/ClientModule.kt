package client

import adapters.HTMLCanvasElementAdapter
import adapters.HTMLCanvasElementAdapterImpl
import bmap.Bmap
import bmap.BmapCode
import adapters.RTCPeerConnectionAdapter
import adapters.RTCPeerConnectionAdapterImpl
import adapters.WindowAdapter
import adapters.WindowAdapterImpl
import frame.Owner
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.Frame
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import math.V2
import org.khronos.webgl.WebGLRenderingContext
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.js.json

enum class Element {
    Canvas,
    WebGL,
}

enum class WebGlProgram {
    Tile,
    Sprite,
}

val clientModule = module {
    single<ClientApplication> { ClientApplication(get(), get()) } withOptions {
        createdAtStart()
    }

    single<CoroutineExceptionHandler> {
        CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException) {
                window.alert("${throwable.message}\n${throwable.stackTraceToString()}")
            }
        }
    }

    single<CoroutineScope> { CoroutineScope(get<CoroutineExceptionHandler>()) }

    single<HTMLCanvasElementAdapter>(named(Element.Canvas)) {
        HTMLCanvasElementAdapterImpl(
            document.getElementById(canvasId) as? org.w3c.dom.HTMLCanvasElement
                ?: throw IllegalStateException("Canvas not found")
        )
    }

    single<WebGLRenderingContext>(named(Element.WebGL)) {
        run {
            get<HTMLCanvasElementAdapter>(named(Element.Canvas))
                .getContext("webgl", "{ alpha: false }") as? WebGLRenderingContext
                ?: throw IllegalStateException("Your browser does not have WebGl")
        }
            .apply {
                if (getExtension("OES_texture_float") == null) {
                    throw IllegalStateException("Your WebGL does not support floating point texture")
                }

                fun resize() {
                    val realToCSSPixels = window.devicePixelRatio
                    val displayWidth = (canvas.clientWidth * realToCSSPixels).toInt()
                    val displayHeight = (canvas.clientHeight * realToCSSPixels).toInt()

                    if (canvas.width != displayWidth ||
                        canvas.height != displayHeight
                    ) {
                        canvas.width = displayWidth
                        canvas.height = displayHeight
                        viewport(x = 0, y = 0, displayWidth, displayHeight)
                    }
                }

                resize()
                window.onresize = { resize() }
            }
    }

    single<Deferred<TileProgram>>(named(WebGlProgram.Tile)) {
        get<WebGLRenderingContext>(named(Element.WebGL)).createTileProgram(get())
    }

    single<Deferred<SpriteProgram>>(named(WebGlProgram.Sprite)) {
        get<WebGLRenderingContext>(named(Element.WebGL)).createSpriteProgram(get())
    }

    single<HttpClient> { HttpClient { install(WebSockets) } }

    single<WindowAdapter> { WindowAdapterImpl() }

    single<Control> { Control(get()) }

    single<Game> {
            (
                sendChannel: SendChannel<Frame>,
                owner: Owner,
                bmap: Bmap,
                receiveChannel: ReceiveChannel<Frame>,
                bmapCode: BmapCode,
            ),
        ->
        GameImpl(
            scope = get(),
            control = get(),
            gl = get(named(Element.WebGL)),
            canvas = get(named(Element.Canvas)),
            tileProgram = get(named(WebGlProgram.Tile)),
            spriteProgram = get(named(WebGlProgram.Sprite)),
            sendChannel = sendChannel,
            owner = owner,
            bmap = bmap,
            receiveChannel = receiveChannel,
            bmapCode = bmapCode,
        )
    }

    single<RTCPeerConnectionAdapter> {
        RTCPeerConnectionAdapterImpl(
            json(
                "iceServers" to arrayOf(
                    json(
                        "urls" to arrayOf("stun:robch.dev", "turn:robch.dev"),
                        "username" to "prouser",
                        "credential" to "BE3pJ@",
                    ),
                ),
            ),
        )
    }

    factory<Tank> { (hasBuilder: Boolean) ->
        TankImpl(
            scope = get(),
            game = get(),
            hasBuilder = hasBuilder,
        )
    }

    factory<Shell> {
            (
                startPosition: V2,
                bearing: Float,
                fromBoat: Boolean,
                sightRange: Float,
            ),
        ->
        ShellImpl(
            scope = get(),
            game = get(),
            startPosition = startPosition,
            bearing = bearing,
            fromBoat = fromBoat,
            sightRange = sightRange,
        )
    }

    factory<Builder> {
            (
                startPosition: V2,
                buildOp: BuilderMission,
            ),
        ->
        BuilderImpl(
            scope = get(),
            game = get(),
            startPosition = startPosition,
            buildMission = buildOp,
        )
    }
}
