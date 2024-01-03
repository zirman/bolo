package adapters

import org.w3c.dom.HTMLCanvasElement

class HTMLCanvasElementAdapterImpl(private val canvas: HTMLCanvasElement) : HTMLCanvasElementAdapter {
    override val width: Int get() = canvas.width
    override val height: Int get() = canvas.height
    override val clientWidth: Int get() = canvas.clientWidth
    override val clientHeight: Int get() = canvas.clientHeight
    override fun getContext(contextId: String, arguments: String): Any? {
        return canvas.getContext(contextId, arguments)
    }
}
