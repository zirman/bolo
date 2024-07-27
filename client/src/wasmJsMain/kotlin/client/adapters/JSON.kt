package client.adapters

external object JSON : JsAny {
    fun stringify(any: JsAny?): String
    fun parse(string: String): JsAny?
}
