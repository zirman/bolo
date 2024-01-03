package client

@JsFun("() => window.WebSocket === null")
external fun webSocketNotAvailable(): Boolean

@JsFun("() => window.RTCPeerConnection === null")
external fun rtcPeerConnectionNotAvailable(): Boolean

actual fun checkWebSocket() {
    if (webSocketNotAvailable()) {
        throw IllegalStateException("Your browser does not have WebSocket")
    }
}

actual fun checkWebRTC() {
    if (rtcPeerConnectionNotAvailable()) {
        throw IllegalStateException("Your browser does not have RTCPeerConnection")
    }
}
