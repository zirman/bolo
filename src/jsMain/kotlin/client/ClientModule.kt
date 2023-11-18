package client

import io.ktor.utils.io.CancellationException
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineExceptionHandler
import org.khronos.webgl.WebGLRenderingContext
import org.koin.dsl.module

val clientModule = module {
    single<CoroutineExceptionHandler> {
        CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException) {
                window.alert("${throwable.message}\n${throwable.stackTraceToString()}")
            }
        }
    }

    single<WebGLRenderingContext> {
//        val canvas = (document.getElementById(canvasId) as? HTMLCanvasElement)
//            ?: throw Exception("game.getCanvas not found")

        (canvas.getContext("webgl", "{ alpha: false }") as? WebGLRenderingContext
            ?: throw Exception("Your browser does not have WebGl"))
            .apply {
                if (asDynamic().getExtension("OES_texture_float") == null) {
                    throw Exception("Your WebGL does not support floating point texture")
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
