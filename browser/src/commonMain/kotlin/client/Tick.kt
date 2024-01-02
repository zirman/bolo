package client

data class Tick(
    val control: ControlState,
    val ticksPerSec: Float,
    val delta: Float,
)
