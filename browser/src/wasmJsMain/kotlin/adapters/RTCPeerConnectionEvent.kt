package adapters

import kotlin.js.JsAny

external interface RTCPeerConnectionEvent : JsAny {
    val channel: DataChannel
    val candidate: JsAny?
}
