package adapters

import kotlinx.serialization.json.JsonObject

interface HTMLCanvasElementAdapter {
    val width: Int
    val height: Int
    val clientWidth: Int
    val clientHeight: Int
    fun getWebGlContext(arguments: JsonObject): WebGlRenderingContextAdapter
    fun setOnmousedown(callback: (x: Int, y: Int) -> Boolean)
    fun setOnmousemove(callback: (x: Int, y: Int) -> Boolean)
    fun setOnmouseup(callback: (x: Int, y: Int) -> Boolean)
}
