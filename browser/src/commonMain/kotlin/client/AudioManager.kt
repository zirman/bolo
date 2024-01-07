package client

import adapters.AudioAdapter

class AudioManager(src: String) {
    private val audioPool: MutableList<AudioAdapter> = mutableListOf(audioAdapterFactory(src))

    fun play() {
        run {
            audioPool.find { it.paused }
                ?: audioPool.first().cloneNode().also { audioPool.add(it) }
        }.play()
    }
}
