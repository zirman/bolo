package client

import util.canvasId
import kotlinx.browser.document
import kotlinx.browser.window
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement

val canvas: HTMLCanvasElement by lazy {
    (document.getElementById(canvasId) as? HTMLCanvasElement)
        ?: throw Exception("game.getCanvas not found")
}

val gl: WebGLRenderingContext by lazy {
    ((canvas.getContext("webgl", "{ alpha: false }")
            as? WebGLRenderingContext)
        ?: run {
            throw Exception("Your browser does not have WebGl")
        })
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

fun checkWebSocket() {
    if (window.asDynamic().WebSocket == null) {
        throw Exception("Your browser does not have WebSocket")
    }
}

fun checkWebRTC() {
    if (window.asDynamic().RTCPeerConnection == null) {
        throw Exception("Your browser does not have RTCPeerConnection")
    }
}
