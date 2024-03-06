package client

class Tick(
    val control: ControlState,
    val ticksPerSec: Float,
    val delta: Float,
    private val listIterator: MutableListIterator<EntityLoop>,
) : MutableListIterator<EntityLoop> by listIterator
