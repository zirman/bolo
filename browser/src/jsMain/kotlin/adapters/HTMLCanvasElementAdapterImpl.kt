package adapters

import assert.assertNotNull
import kotlinx.browser.window
import kotlinx.serialization.json.JsonObject
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_TEST
import org.khronos.webgl.WebGLRenderingContext.Companion.ONE_MINUS_SRC_ALPHA
import org.khronos.webgl.WebGLRenderingContext.Companion.SRC_ALPHA
import org.w3c.dom.HTMLCanvasElement

class HTMLCanvasElementAdapterImpl(private val canvas: HTMLCanvasElement) : HTMLCanvasElementAdapter {
    override val width: Int get() = canvas.width
    override val height: Int get() = canvas.height
    override val clientWidth: Int get() = canvas.clientWidth
    override val clientHeight: Int get() = canvas.clientHeight

    override fun getWebGlContext(arguments: JsonObject): WebGlRenderingContextAdapter {
        return canvas
            .getContext("webgl", JSON.parse(arguments.toString()))
            ?.let { it as? WebGLRenderingContext }
            ?.apply {
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

                blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
                disable(DEPTH_TEST)
            }
            .assertNotNull("Your browser does not have WebGl")
            .let { WebGlRenderingContextAdapterImpl(it) }
    }

    override fun setOnmousedown(callback: (button: Short, x: Int, y: Int) -> Boolean) {
        canvas.onmousedown = { event ->
            if (callback(event.button, event.clientX, event.clientY)) {
                event.preventDefault()
            }
        }
    }

    override fun setOnmousemove(callback: (button: Short, x: Int, y: Int) -> Boolean) {
        canvas.onmousemove = { event ->
            if (callback(event.button, event.clientX, event.clientY)) {
                event.preventDefault()
            }
        }
    }

    override fun setOnmouseup(callback: (button: Short, x: Int, y: Int) -> Boolean) {
        canvas.onmouseup = { event ->
            if (callback(event.button, event.clientX, event.clientY)) {
                event.preventDefault()
            }
        }
    }
}
