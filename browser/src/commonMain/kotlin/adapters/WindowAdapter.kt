package adapters

interface WindowAdapter {
    fun setOnkeydown(callback: (keyCode: Int) -> Boolean)
    fun setOnkeyup(callback: (keyCode: Int) -> Boolean)
    fun setOnmousedown(callback: (x: Int, y: Int) -> Boolean)
    fun setOnmousemove(callback: (x: Int, y: Int) -> Boolean)
    fun setOnmouseup(callback: (x: Int, y: Int) -> Boolean)
    fun setGamepadconnect(callback: () -> Unit)
}
