package client

import kotlinx.browser.window

sealed class DirectionVertical {
    object Up : DirectionVertical()
    object Center : DirectionVertical()
    object Down : DirectionVertical()
}

sealed class DirectionHorizontal {
    object Left : DirectionHorizontal()
    object Center : DirectionHorizontal()
    object Right : DirectionHorizontal()
}

sealed class Mouse {
    data class Drag(val x: Int, val y: Int) : Mouse()
    data class Up(val x: Int, val y: Int) : Mouse()
}

sealed class BuilderMode {
    object Tree : BuilderMode()
    object Road : BuilderMode()
    object Wall : BuilderMode()
    object Pill : BuilderMode()
    object Mine : BuilderMode()
}

data class ControlState(
    val builderMode: BuilderMode,
    val directionHorizontal: DirectionHorizontal,
    val directionVertical: DirectionVertical,
    val shootButton: Boolean,
    val mouse: Mouse?,
)

object Control {
    private var builderMode: BuilderMode = BuilderMode.Tree
    private val directionHorizontal: DirectionHorizontal
        get() =
            when {
                keyA || keyLeft && (keyD || keyRight).not() -> DirectionHorizontal.Left
                keyD || keyRight && (keyA || keyLeft).not() -> DirectionHorizontal.Right
                else -> DirectionHorizontal.Center
            }

    private val directionVertical: DirectionVertical
        get() =
            when {
                keyW || keyUp && (keyS || keyDown).not() -> DirectionVertical.Up
                keyS || keyDown && (keyW || keyUp).not() -> DirectionVertical.Down
                else -> DirectionVertical.Center
            }

    private val shootButton: Boolean get() = keySpace

    private val mouse: Mouse?
        get() =
            when {
                mouseUp -> Mouse.Up(mouseX, mouseY)
                mouseDrag -> Mouse.Drag(mouseDragX, mouseDragY)
                else -> null
            }

    fun getControlState(): ControlState {
        val controlState = ControlState(
            builderMode,
            directionHorizontal,
            directionVertical,
            shootButton,
            mouse,
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
    private var mouseDown = false
    private var mouseX: Int = 0
    private var mouseY: Int = 0
    private var mouseDrag = false
    private var mouseDragX: Int = 0
    private var mouseDragY: Int = 0
    private var mouseUp = false

    init {
        window.onkeydown = { event ->
            var preventDefault = true

            when (event.which) {
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
                else -> preventDefault = false
            }

            if (preventDefault) {
                event.preventDefault()
            }
        }

        window.onkeyup = { event ->
            var preventDefault = true

            when (event.which) {
                32 -> keySpace = false
                37 -> keyLeft = false
                38 -> keyUp = false
                39 -> keyRight = false
                40 -> keyDown = false
                65 -> keyA = false
                68 -> keyD = false
                83 -> keyS = false
                87 -> keyW = false
                else -> preventDefault = false
            }

            if (preventDefault) {
                event.preventDefault()
            }
        }

        window.onmousedown = { event ->
            mouseDown = true
            mouseX = event.clientX
            mouseY = event.clientY
            mouseDrag = false
            mouseDragX = 0
            mouseDragY = 0
            event.preventDefault()
        }

        window.onmousemove = { event ->
            if (mouseDown) {
                mouseDrag = true
                mouseDragX += event.clientX - mouseX
                mouseDragY += event.clientY - mouseY
            }

            mouseX = event.clientX
            mouseY = event.clientY
            event.preventDefault()
        }

        window.onmouseup = { event ->
            mouseDown = false
            mouseUp = !mouseDrag
            event.preventDefault()
        }

        window.addEventListener("gamepadconnect", {
            // TODO
        })
    }
}
