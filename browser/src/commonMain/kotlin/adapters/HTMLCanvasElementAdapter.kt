package adapters

interface HTMLCanvasElementAdapter {
    val width: Int
    val height: Int
    val clientWidth: Int
    val clientHeight: Int
    fun getContext(contextId: String, arguments: String): Any?
}
