package load

import kotlinx.coroutines.suspendCancellableCoroutine
import math.V2
import math.v2
import org.w3c.dom.Image
import org.w3c.dom.events.Event
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.cos
import kotlin.math.sin

suspend fun loadImage(src: String): Image = suspendCancellableCoroutine { continuation ->
    val image = Image()
    image.src = src

    val handler = object : (Event) -> Unit {
        override fun invoke(event: Event) {
            image.removeEventListener("load", this)
            image.removeEventListener("error", this)

            when (event.type) {
                "load" ->
                    continuation.resume(image)

                "error" ->
                    continuation.resumeWithException(IllegalStateException("error loading image"))
            }
        }
    }

    image.addEventListener("load", handler)
    image.addEventListener("error", handler)

    continuation.invokeOnCancellation {
        image.removeEventListener("load", handler)
        image.removeEventListener("error", handler)
    }
}

fun dirToVec(bearing: Float): V2 = v2(cos(bearing), -sin(bearing))
