import kotlinx.browser.document
import org.w3c.dom.HTMLCanvasElement

fun main() {
    (document.getElementById("bolo-canvas") as? HTMLCanvasElement)
        ?.let { println("$it") }
//?.innerHTML = "Hello, Kotlin/JS!"
}
