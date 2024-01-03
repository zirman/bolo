package adapters

import kotlinx.browser.window

class WindowAdapterImpl : WindowAdapter {
    override fun setOnkeydown(callback: (Int) -> Boolean) {
        window.onkeydown = { event ->
            if (callback(event.which)) {
                event.preventDefault()
            }
        }
    }

    override fun setOnkeyup(callback: (Int) -> Boolean) {
        window.onkeyup = { event ->
            if (callback(event.which)) {
                event.preventDefault()
            }
        }
    }

    override fun setOnmousedown(callback: (Int, Int) -> Boolean) {
        window.onmousedown = { event ->
            if (callback(event.clientX, event.clientY)) {
                event.preventDefault()
            }
        }
    }

    override fun setOnmousemove(callback: (Int, Int) -> Boolean) {
        window.onmousemove = { event ->
            if (callback(event.clientX, event.clientY)) {
                event.preventDefault()
            }
        }
    }

    override fun setOnmouseup(callback: (Int, Int) -> Boolean) {
        window.onmouseup = { event ->
            if (callback(event.clientX, event.clientY)) {
                event.preventDefault()
            }
        }
    }

    override fun setGamepadconnect(callback: () -> Unit) {
        window.addEventListener("gamepadconnect", {
            callback()
        })
    }
}
