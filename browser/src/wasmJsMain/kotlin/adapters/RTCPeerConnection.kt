package adapters

import kotlin.js.Promise

external class RTCPeerConnection(configuration: JsAny) : JsAny {
    val connectionState: String
    val localDescription: JsAny?
    var onnegotiationneeded: (event: RTCPeerConnectionEvent) -> Unit
    var onconnectionstatechange: (event: RTCPeerConnectionEvent) -> Unit
    var ondatachannel: (event: RTCPeerConnectionEvent) -> Unit
    var onicecandidate: (event: RTCPeerConnectionEvent) -> Unit
    fun setRemoteDescription(description: JsAny): Promise<JsAny?>
    fun setLocalDescription(description: JsAny): Promise<JsAny?>
    fun addIceCandidate(candidate: JsAny): Promise<JsAny?>
    fun createOffer(): Promise<JsAny?>
    fun createAnswer(): Promise<JsAny?>
    fun createDataChannel(label: String, options: JsAny): DataChannel
}
