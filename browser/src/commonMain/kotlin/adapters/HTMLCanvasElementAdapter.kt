package adapters

import kotlinx.serialization.json.JsonObject

interface HTMLCanvasElementAdapter {
    val width: Int
    val height: Int
    val clientWidth: Int
    val clientHeight: Int
    fun getWebGlContext(arguments: JsonObject): WebGlRenderingContextAdapter
}
