package client

import kotlinx.browser.window

fun checkWebSocket() {
    if (window.asDynamic().WebSocket == null) {
        throw IllegalStateException("Your browser does not have WebSocket")
    }
}

fun checkWebRTC() {
    if (window.asDynamic().RTCPeerConnection == null) {
        throw IllegalStateException("Your browser does not have RTCPeerConnection")
    }
}
