package client

import assert.never

actual suspend fun awaitAnimationFrame(): Double = never()
actual fun getDevicePixelRatio(): Double = never()
actual fun getLocationHost(): String = never()
