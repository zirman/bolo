package client

@JsFun("() => window.WebSocket === null")
external fun webSocketNull(): JsBoolean

@JsFun("() => window.RTCPeerConnection === null")
external fun rtcPeerConnectionNull(): JsBoolean

actual fun checkWebSocket() {
    if (webSocketNull().toBoolean()) {
        throw IllegalStateException("Your browser does not have WebSocket")
    }
}

actual fun checkWebRTC() {
    if (rtcPeerConnectionNull().toBoolean()) {
        throw IllegalStateException("Your browser does not have RTCPeerConnection")
    }
}
