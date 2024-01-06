package client

import kotlinx.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual suspend fun awaitAnimationFrame(): Double = suspendCoroutine { continuation ->
    window.requestAnimationFrame { continuation.resume(it) }
}

actual fun getDevicePixelRatio(): Double = window.devicePixelRatio
actual fun getLocationHost(): String = window.location.host
