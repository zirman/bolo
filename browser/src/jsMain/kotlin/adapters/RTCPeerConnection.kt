package adapters

import kotlin.js.Json
import kotlin.js.Promise

external class RTCPeerConnection(configuration: Json) {
    val connectionState: String
    val localDescription: Json?
    var onnegotiationneeded: (event: RTCPeerConnectionEvent) -> Unit
    var onconnectionstatechange: (event: RTCPeerConnectionEvent) -> Unit
    var ondatachannel: (event: RTCPeerConnectionEvent) -> Unit
    var onicecandidate: (event: RTCPeerConnectionEvent) -> Unit
    fun setRemoteDescription(description: Json): Promise<Unit>
    fun setLocalDescription(description: Json): Promise<Unit>
    fun addIceCandidate(candidate: Json): Promise<Unit>
    fun createOffer(): Promise<Json>
    fun createAnswer(): Promise<Json>
    fun createDataChannel(label: String, options: Json): DataChannel
}
