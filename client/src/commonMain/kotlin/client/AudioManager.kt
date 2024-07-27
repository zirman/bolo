package client

import adapters.AudioAdapter

class AudioManager(private val src: String) {
    private val audioPool: MutableList<AudioAdapter> = mutableListOf()

    fun play() {
        run {
            audioPool.find { it.paused }
                ?: (audioPool.firstOrNull()?.cloneNode() ?: audioAdapterFactory(src)).also { audioPool.add(it) }
        }.play()
    }
}
