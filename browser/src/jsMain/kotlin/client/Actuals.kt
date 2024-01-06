package client

import kotlinx.browser.window
import kotlinx.coroutines.awaitAnimationFrame

actual suspend fun awaitAnimationFrame(): Double {
    return window.awaitAnimationFrame()
}

actual val devicePixelRatio: Double get() = window.devicePixelRatio
