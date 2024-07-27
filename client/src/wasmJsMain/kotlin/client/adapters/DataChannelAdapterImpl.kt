package client.adapters

class DataChannelAdapterImpl(private val dataChannel: DataChannel) : DataChannelAdapter {
    override val readyState: String by dataChannel::readyState

    override fun setOnopen(callback: (event: String) -> Unit) {
        dataChannel.onopen = { event ->
            callback(JSON.stringify(event))
        }
    }

    override fun setOnmessage(callback: (data: String) -> Unit) {
        dataChannel.onmessage = { event ->
            callback(event.data)
        }
    }

    override fun setOnclose(callback: (event: String) -> Unit) {
        dataChannel.onclose = { event ->
            callback(JSON.stringify(event))
        }
    }

    override fun setOnerror(callback: (event: String) -> Unit) {
        dataChannel.onerror = { event ->
            callback(JSON.stringify(event))
        }
    }

    override fun send(message: String) {
        dataChannel.send(message)
    }
}
