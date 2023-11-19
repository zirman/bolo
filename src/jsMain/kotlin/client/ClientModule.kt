package client

import io.ktor.utils.io.CancellationException
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineExceptionHandler
import org.khronos.webgl.WebGLRenderingContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.w3c.dom.HTMLCanvasElement
import util.canvasId

enum class Element {
    Canvas,
    WebGL,
}

val clientModule = module {
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
}
