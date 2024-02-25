package adapters

interface WindowAdapter {
    fun setOnkeydown(callback: (keyCode: Int) -> Boolean)
    fun setOnkeyup(callback: (keyCode: Int) -> Boolean)
    fun setOnwheel(callback: (delta: Float) -> Boolean)
    fun setOncontextmenu(callback: (x: Int, y: Int) -> Boolean)
    fun setGamepadconnect(callback: () -> Unit)
    fun setOnInputElementChecked(elementId: String, callback: () -> Unit)
}
