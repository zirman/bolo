package adapters

external interface DataChannel {
    val readyState: String
    var onopen: (DataChannelEvent) -> Unit
    var onmessage: (DataChannelEvent) -> Unit
    var onclose: (DataChannelEvent) -> Unit
    var onerror: (DataChannelEvent) -> Unit
    fun send(message: String)
}
