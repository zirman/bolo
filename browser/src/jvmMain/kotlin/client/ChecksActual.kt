package client

import assert.never

actual suspend fun awaitAnimationFrame(): Double {
    never()
}

actual val devicePixelRatio: Double get() = never()
