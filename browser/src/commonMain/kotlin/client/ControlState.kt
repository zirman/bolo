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

sealed interface Mouse {
    data class Drag(val x: Int, val y: Int) : Mouse
    data class Up(val x: Int, val y: Int) : Mouse
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
    val shootButton: Boolean,
    val layMineButton: Boolean,
    val mouse: Mouse?,
)
