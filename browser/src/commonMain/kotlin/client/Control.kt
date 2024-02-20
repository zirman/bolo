package client

import adapters.WindowAdapter

class Control(window: WindowAdapter) {
    private var builderMode: BuilderMode = BuilderMode.Tree
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

    private val mouse: Mouse?
        get() = when {
            mouseUp -> Mouse.Up(mouseX, mouseY)
            mouseDrag -> Mouse.Drag(mouseDragX, mouseDragY)
            else -> null
        }

    fun getControlState(): ControlState {
        val controlState = ControlState(
            builderMode = builderMode,
            directionHorizontal = directionHorizontal,
            directionVertical = directionVertical,
            fireButton = fireButton,
            layMineButton = layMineButton,
            mouse = mouse,
        )

        mouseUp = false
        mouseDragX = 0
        mouseDragY = 0

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
    private var mouseDown = false
    private var mouseX: Int = 0
    private var mouseY: Int = 0
    private var mouseDrag = false
    private var mouseDragX: Int = 0
    private var mouseDragY: Int = 0
    private var mouseUp = false

    init {
        window.setOnkeydown { keyCode ->
            when (keyCode) {
                16 -> keyShift = true
                32 -> keySpace = true
                37 -> keyLeft = true
                38 -> keyUp = true
                39 -> keyRight = true
                40 -> keyDown = true
                49 -> builderMode = BuilderMode.Tree
                50 -> builderMode = BuilderMode.Road
                51 -> builderMode = BuilderMode.Wall
                52 -> builderMode = BuilderMode.Pill
                54 -> builderMode = BuilderMode.Mine
                65 -> keyA = true
                68 -> keyD = true
                87 -> keyW = true
                83 -> keyS = true
                else -> return@setOnkeydown false
            }

            true
        }

        window.setOnkeyup { keyCode ->
            when (keyCode) {
                16 -> keyShift = false
                32 -> keySpace = false
                37 -> keyLeft = false
                38 -> keyUp = false
                39 -> keyRight = false
                40 -> keyDown = false
                65 -> keyA = false
                68 -> keyD = false
                83 -> keyS = false
                87 -> keyW = false
                else -> return@setOnkeyup false
            }

            true
        }

        window.setOnmousedown { x, y ->
            mouseDown = true
            mouseX = x
            mouseY = y
            mouseDrag = false
            mouseDragX = 0
            mouseDragY = 0
            true
        }

        window.setOnmousemove { x, y ->
            if (mouseDown) {
                mouseDrag = true
                mouseDragX += x - mouseX
                mouseDragY += y - mouseY
            }

            mouseX = x
            mouseY = y
            true
        }

        window.setOnmouseup { x, y ->
            mouseDown = false
            mouseUp = mouseDrag.not()
            true
        }

        window.setGamepadconnect {
            // TODO: gamepad support
        }
    }
}
