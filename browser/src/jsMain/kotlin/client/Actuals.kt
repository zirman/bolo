package client

import kotlinx.browser.window
import kotlinx.coroutines.awaitAnimationFrame

actual suspend fun awaitAnimationFrame(): Double = window.awaitAnimationFrame()
actual fun getDevicePixelRatio(): Double = window.devicePixelRatio
actual fun getLocationHost(): String = window.location.host
