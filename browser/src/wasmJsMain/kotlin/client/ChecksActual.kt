package client

fun webSocketNotAvailable(): Boolean = js("window.WebSocket === null")
fun rtcPeerConnectionNotAvailable(): Boolean = js("window.RTCPeerConnection === null")

fun checkWebSocket() {
    if (webSocketNotAvailable()) {
        throw IllegalStateException("Your browser does not have WebSocket")
    }
}

fun checkWebRTC() {
    if (rtcPeerConnectionNotAvailable()) {
        throw IllegalStateException("Your browser does not have RTCPeerConnection")
    }
}
