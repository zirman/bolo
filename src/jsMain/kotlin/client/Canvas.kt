package client

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement
import util.canvasId

val canvas: HTMLCanvasElement by lazy {
    (document.getElementById(canvasId) as? HTMLCanvasElement)
        ?: throw Exception("game.getCanvas not found")
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
