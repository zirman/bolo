package adapters

import org.w3c.dom.Audio

class AudioAdapterImpl private constructor(private val audio: Audio) : AudioAdapter {
    constructor(src: String) : this(Audio(src))

    override fun play() {
        audio.play()
    }

    override val paused get() = audio.paused

    override fun cloneNode(): AudioAdapter = AudioAdapterImpl(audio.cloneNode() as Audio)
}
