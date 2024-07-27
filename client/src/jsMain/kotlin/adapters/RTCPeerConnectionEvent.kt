package adapters

import kotlin.js.Json

external interface RTCPeerConnectionEvent {
    val channel: DataChannel
    val candidate: Json?
}
