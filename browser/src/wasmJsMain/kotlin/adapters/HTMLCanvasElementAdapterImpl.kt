package adapters

import kotlinx.serialization.json.JsonObject
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.RenderingContext

class RenderingContextAdapterImpl(val renderingContext: RenderingContext) : RenderingContextAdapter

class HTMLCanvasElementAdapterImpl(private val canvas: HTMLCanvasElement) : HTMLCanvasElementAdapter {
    override val width: Int get() = canvas.width
    override val height: Int get() = canvas.height
    override val clientWidth: Int get() = canvas.clientWidth
    override val clientHeight: Int get() = canvas.clientHeight
    override fun getContext(contextId: String, arguments: JsonObject): RenderingContextAdapter? {
        return canvas.getContext(contextId, JSON.parse(arguments.toString()))?.let { RenderingContextAdapterImpl(it) }
    }
}
