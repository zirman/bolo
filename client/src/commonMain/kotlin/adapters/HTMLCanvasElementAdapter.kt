package adapters

import kotlinx.serialization.json.JsonObject

interface HTMLCanvasElementAdapter {
    val width: Int
    val height: Int
    val clientWidth: Int
    val clientHeight: Int
    fun getWebGlContext(arguments: JsonObject): WebGlRenderingContextAdapter
    fun setOnmousedown(callback: (buttons: Short, x: Int, y: Int) -> Boolean)
    fun setOnmousemove(callback: (buttons: Short, x: Int, y: Int) -> Boolean)
    fun setOnmouseup(callback: (button: Short, x: Int, y: Int) -> Boolean)
}
