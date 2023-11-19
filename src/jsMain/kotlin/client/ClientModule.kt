package client

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.utils.io.CancellationException
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import org.khronos.webgl.WebGLRenderingContext
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.w3c.dom.HTMLCanvasElement
import util.canvasId

enum class Element {
    Canvas,
    WebGL,
}

enum class WebGlProgram {
    Tile,
    Sprite,
}

val clientModule = module(createdAtStart = true) {
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

    single<HTMLCanvasElement>(named(Element.Canvas)) {
        document.getElementById(canvasId) as? HTMLCanvasElement ?: throw IllegalStateException("Canvas not found")
    }

    single<WebGLRenderingContext>(named(Element.WebGL)) {
        run {
            get<HTMLCanvasElement>(named(Element.Canvas))
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

    single<CoroutineScope> { CoroutineScope(get<CoroutineExceptionHandler>()) }

    single<Deferred<TileProgram>>(named(WebGlProgram.Tile)) {
        get<WebGLRenderingContext>(named(Element.WebGL)).createTileProgram(get())
    }

    single<Deferred<SpriteProgram>>(named(WebGlProgram.Sprite)) {
        get<WebGLRenderingContext>(named(Element.WebGL)).createSpriteProgram(get())
    }

    single<HttpClient> { HttpClient { install(WebSockets) } }

    single<Game> { parameters ->
        GameImpl(
            scope = get(),
            gl = get(named(Element.WebGL)),
            canvas = get(named(Element.Canvas)),
            tileProgram = get(named(WebGlProgram.Tile)),
            spriteProgram = get(named(WebGlProgram.Sprite)),
            sendChannel = parameters.get(),
            owner = parameters.get(),
            bmap = parameters.get(),
            receiveChannel = parameters.get(),
            bmapCode = parameters.get(),
        )
    }

    factory<Tank> {
        TankImpl(
            scope = get(),
            game = get(),
        )
    }

    factory<Shell> { parameters ->
        ShellImpl(
            scope = get(),
            game = get(),
            startPosition = parameters.get(),
            bearing = parameters.get(),
            fromBoat = parameters.get(),
            sightRange = parameters.get(),
        )
    }

    factory<Builder> { parameters ->
        BuilderImpl(
            scope = get(),
            game = get(),
            startPosition = parameters.get(),
            targetX = parameters.get(),
            targetY = parameters.get(),
            buildOp = parameters.get(),
        )
    }
}
