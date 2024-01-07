package client

expect suspend fun awaitAnimationFrame(): Double
expect fun getDevicePixelRatio(): Double
expect fun getLocationHost(): String
expect fun alert(message: String)
