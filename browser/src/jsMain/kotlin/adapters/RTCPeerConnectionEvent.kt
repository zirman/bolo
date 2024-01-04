package adapters

import kotlin.js.Json

external class RTCPeerConnectionEvent {
    val channel: DataChannel
    val candidate: Json?
}
