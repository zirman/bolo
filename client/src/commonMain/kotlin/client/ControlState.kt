package client

enum class DirectionVertical {
    Up,
    Center,
    Down,
}

enum class DirectionHorizontal {
    Left,
    Center,
    Right,
}

sealed interface MouseEvent {
    data class Drag(val dx: Int, val dy: Int) : MouseEvent
    data class Up(val col: Int, val row: Int, val downCol: Int, val downRow: Int) : MouseEvent
}

enum class BuilderMode {
    Tree,
    Road,
    Wall,
    Pill,
    Mine,
}

data class ControlState(
    val builderMode: BuilderMode,
    val directionHorizontal: DirectionHorizontal,
    val directionVertical: DirectionVertical,
    val fireButton: Boolean,
    val layMineButton: Boolean,
    val mouseEvent: MouseEvent?,
    val deltaY: Float,
)
