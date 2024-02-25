package client

import adapters.HTMLCanvasElementAdapter
import adapters.WindowAdapter

class Control(window: WindowAdapter, canvas: HTMLCanvasElementAdapter) {
    private var builderMode: BuilderMode = BuilderMode.Tree.also { setBuilderMode(it) }
        set(value) {
            field = value
            setBuilderMode(value)
        }

    private val directionHorizontal: DirectionHorizontal
        get() = when {
            keyA || keyLeft && (keyD || keyRight).not() -> DirectionHorizontal.Left
            keyD || keyRight && (keyA || keyLeft).not() -> DirectionHorizontal.Right
            else -> DirectionHorizontal.Center
        }

    private val directionVertical: DirectionVertical
        get() = when {
            keyW || keyUp && (keyS || keyDown).not() -> DirectionVertical.Up
            keyS || keyDown && (keyW || keyUp).not() -> DirectionVertical.Down
            else -> DirectionVertical.Center
        }

    private val fireButton: Boolean get() = keySpace
    private val layMineButton: Boolean get() = keyShift

    private val mouseEvent: MouseEvent?
        get() {
            return if (mouseUpEvent != null) {
                mouseUpEvent.also { mouseUpEvent = null }
            } else if (rightMouseDown) {
                val dx = dragX - x
                val dy = dragY - y
                x = dragX
                y = dragY
                MouseEvent.Drag(dx, dy)
            } else {
                null
            }
        }

    fun getControlState(): ControlState {
        val controlState = ControlState(
            builderMode = builderMode,
            directionHorizontal = directionHorizontal,
            directionVertical = directionVertical,
            fireButton = fireButton,
            layMineButton = layMineButton,
            mouseEvent = mouseEvent,
        )

        return controlState
    }

    private var keyA: Boolean = false
    private var keyD: Boolean = false
    private var keyS: Boolean = false
    private var keyW: Boolean = false
    private var keyUp: Boolean = false
    private var keyLeft: Boolean = false
    private var keyDown: Boolean = false
    private var keyRight: Boolean = false
    private var keySpace: Boolean = false
    private var keyShift: Boolean = false
    private var rightMouseDown = false
    private var x: Int = 0
    private var y: Int = 0
    private var dragX: Int = 0
    private var dragY: Int = 0
    private var mouseUpEvent: MouseEvent.Up? = null

    init {
        window.setOnkeydown { keyCode ->
            when (keyCode) {
                TAB_KEYCODE -> {
                    // ignore tab
                }

                SHIFT_KEYCODE -> {
                    keyShift = true
                }

                SPACE_KEYCODE -> {
                    keySpace = true
                }

                LEFT_ARROW_KEYCODE -> {
                    keyLeft = true
                }

                UP_ARROW_KEYCODE -> {
                    keyUp = true
                }

                RIGHT_MOUSE_BUTTON_ID -> {
                    keyRight = true
                }

                DOWN_ARROW_KEYCODE -> {
                    keyDown = true
                }

                ONE_KEYCODE -> {
                    builderMode = BuilderMode.Tree
                }

                TWO_KEYCODE -> {
                    builderMode = BuilderMode.Road
                }

                THREE_KEYCODE -> {
                    builderMode = BuilderMode.Wall
                }

                FOUR_KEYCODE -> {
                    builderMode = BuilderMode.Pill
                }

                FIVE_KEYCODE -> {
                    builderMode = BuilderMode.Mine
                }

                A_KEYCODE -> {
                    keyA = true
                }

                D_KEYCODE -> {
                    keyD = true
                }

                W_KEYCODE -> {
                    keyW = true
                }

                S_KEYCODE -> {
                    keyS = true
                }

                else -> {
                    return@setOnkeydown false
                }
            }

            true
        }

        window.setOnkeyup { keyCode ->
            when (keyCode) {
                SHIFT_KEYCODE -> {
                    keyShift = false
                }

                SPACE_KEYCODE -> {
                    keySpace = false
                }

                LEFT_ARROW_KEYCODE -> {
                    keyLeft = false
                }

                UP_ARROW_KEYCODE -> {
                    keyUp = false
                }

                RIGHT_ARROW_KEYCODE -> {
                    keyRight = false
                }

                DOWN_ARROW_KEYCODE -> {
                    keyDown = false
                }

                A_KEYCODE -> {
                    keyA = false
                }

                D_KEYCODE -> {
                    keyD = false
                }

                S_KEYCODE -> {
                    keyS = false
                }

                W_KEYCODE -> {
                    keyW = false
                }

                else -> {
                    return@setOnkeyup false
                }
            }

            true
        }

        canvas.setOnmousedown { button, x, y ->
            this.x = x
            this.y = y
            dragX = x
            dragY = y

            when (button.toInt()) {
                LEFT_MOUSE_BUTTON_ID -> {
                    true
                }

                RIGHT_MOUSE_BUTTON_ID -> {
                    rightMouseDown = true
                    true
                }

                else -> {
                    false
                }
            }
        }

        canvas.setOnmousemove { _, x, y ->
            dragX = x
            dragY = y
            true
        }

        canvas.setOnmouseup { button, x, y ->
            if (button.toInt() == LEFT_MOUSE_BUTTON_ID) {
                mouseUpEvent = MouseEvent.Up(x, y, this.x, this.y)
            }

            rightMouseDown = false
            true
        }

        // do not show context menu
        window.setOncontextmenu { _, _ ->
            true
        }

        window.setOnInputElementChecked(BUILDER_MODE_TREE_ID) {
            builderMode = BuilderMode.Tree
        }

        window.setOnInputElementChecked(BUILDER_MODE_ROAD_ID) {
            builderMode = BuilderMode.Road
        }

        window.setOnInputElementChecked(BUILDER_MODE_WALL_ID) {
            builderMode = BuilderMode.Wall
        }

        window.setOnInputElementChecked(BUILDER_MODE_PILL_ID) {
            builderMode = BuilderMode.Pill
        }

        window.setOnInputElementChecked(BUILDER_MODE_MINE_ID) {
            builderMode = BuilderMode.Mine
        }

        window.setGamepadconnect {
            // TODO: gamepad support
        }
    }

    companion object {
        private const val TAB_KEYCODE = 9
        private const val SHIFT_KEYCODE = 16
        private const val SPACE_KEYCODE = 32
        private const val LEFT_ARROW_KEYCODE = 37
        private const val UP_ARROW_KEYCODE = 38
        private const val RIGHT_ARROW_KEYCODE = 39
        private const val DOWN_ARROW_KEYCODE = 40
        private const val ONE_KEYCODE = 49
        private const val TWO_KEYCODE = 50
        private const val THREE_KEYCODE = 51
        private const val FOUR_KEYCODE = 52
        private const val FIVE_KEYCODE = 53
        private const val A_KEYCODE = 65
        private const val D_KEYCODE = 68
        private const val S_KEYCODE = 83
        private const val W_KEYCODE = 87
        private const val LEFT_MOUSE_BUTTON_ID = 0
        private const val RIGHT_MOUSE_BUTTON_ID = 2
    }
}
