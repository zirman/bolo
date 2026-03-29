@file:OptIn(ExperimentalWasmJsInterop::class)

package client.adapters

external interface DataChannelEvent : JsAny {
    val data: String
}
