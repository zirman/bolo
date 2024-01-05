package client

import kotlinx.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual suspend fun awaitAnimationFrame(): Double {
    return suspendCoroutine { continuation ->
        window.requestAnimationFrame {
            continuation.resume(it)
        }
    }
}

actual val devicePixelRatio: Double get() = window.devicePixelRatio
