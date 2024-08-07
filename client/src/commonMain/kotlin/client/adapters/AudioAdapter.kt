package client.adapters

interface AudioAdapter {
    fun play()
    val paused: Boolean
    fun cloneNode(): AudioAdapter
}
