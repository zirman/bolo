package adapters

import kotlin.js.Json

class DataChannelAdapterImpl(private val dataChannel: dynamic) : DataChannelAdapter {
    override val readyState: String
        get() = dataChannel.readyState.unsafeCast<String>()

    override fun setOnopen(callback: (event: String) -> Unit) {
        dataChannel.onopen = { event: dynamic ->
            callback(event)
        }
    }

    override fun setOnmessage(callback: (data: String) -> Unit) {
        dataChannel.onmessage = { event: dynamic ->
            callback(event.data.unsafeCast<String>())
        }
    }

    override fun setOnclose(callback: (event: String) -> Unit) {
        dataChannel.onclose = { event: dynamic ->
            callback(JSON.stringify(event.unsafeCast<Json>()))
        }
    }

    override fun setOnerror(callback: (event: String) -> Unit) {
        dataChannel.onerror = { event: dynamic ->
            callback(JSON.stringify(event.unsafeCast<Json>()))
        }
    }

    override fun send(message: String) {
        dataChannel.send(message)
    }
}
