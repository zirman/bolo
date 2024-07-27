package adapters

interface DataChannelAdapter {
    val readyState: String
    fun setOnopen(callback: (String) -> Unit)
    fun setOnmessage(callback: (String) -> Unit)
    fun setOnclose(callback: (String) -> Unit)
    fun setOnerror(callback: (String) -> Unit)
    fun send(message: String)
}
