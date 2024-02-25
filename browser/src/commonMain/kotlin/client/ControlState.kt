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
    data class Drag(val dx: Int, val dy : Int) : MouseEvent
    data class Up(val x: Int, val y: Int, val downX: Int, val downY: Int) : MouseEvent
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
)
