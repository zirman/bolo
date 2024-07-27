package adapters

interface WindowAdapter {
    fun setOnkeydown(callback: (keycode: Int) -> Boolean)
    fun setOnkeyup(callback: (keycode: Int) -> Boolean)
    fun setOnwheel(callback: (delta: Float) -> Boolean)
    fun setOncontextmenu(callback: (x: Int, y: Int) -> Boolean)
    fun setGamepadconnect(callback: () -> Unit)
    fun setOnInputElementChecked(elementId: String, callback: () -> Unit)
}
