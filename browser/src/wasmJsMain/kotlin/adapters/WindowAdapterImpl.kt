package adapters

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLInputElement

class WindowAdapterImpl : WindowAdapter {
    override fun setOnkeydown(callback: (keyCode: Int) -> Boolean) {
        window.onkeydown = { event ->
            if (callback(event.which)) {
                event.preventDefault()
            }
        }
    }

    override fun setOnkeyup(callback: (keyCode: Int) -> Boolean) {
        window.onkeyup = { event ->
            if (callback(event.which)) {
                event.preventDefault()
            }
        }
    }

    override fun setOncontextmenu(callback: (x: Int, y: Int) -> Boolean) {
        window.oncontextmenu = { event ->
            if (callback(event.clientX, event.clientY)) {
                event.preventDefault()
            }
        }
    }

    override fun setOnInputElementChecked(elementId: String, callback: () -> Unit) {
        val inputElement = document.getElementById(elementId) as HTMLInputElement
        inputElement.onchange = { _ ->
            if (inputElement.checked) {
                callback()
            }
        }
    }

    override fun setGamepadconnect(callback: () -> Unit) {
        window.addEventListener("gamepadconnect") {
            callback()
        }
    }
}
