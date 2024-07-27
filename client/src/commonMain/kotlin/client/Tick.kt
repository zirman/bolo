package client

class Tick(
    val frameCount: Int,
    val control: ControlState,
    val ticksPerSec: Float,
    val delta: Float,
    private val gameProcessesIterator: MutableListIterator<GameProcess>,
) : MutableListIterator<GameProcess> by gameProcessesIterator
