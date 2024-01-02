package client

import kotlinx.browser.window

actual fun checkWebSocket() {
    if (window.asDynamic().WebSocket == null) {
        throw IllegalStateException("Your browser does not have WebSocket")
    }
}

actual fun checkWebRTC() {
    if (window.asDynamic().RTCPeerConnection == null) {
        throw IllegalStateException("Your browser does not have RTCPeerConnection")
    }
}
