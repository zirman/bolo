package client

import adapters.AudioAdapter
import adapters.HTMLCanvasElementAdapter
import adapters.RTCPeerConnectionAdapter
import adapters.Uint8ArrayAdapter
import adapters.WindowAdapter
import kotlinx.serialization.json.JsonObject

expect val windowAdapter: WindowAdapter
expect val htmlCanvasElementAdapter: HTMLCanvasElementAdapter

expect suspend fun awaitAnimationFrame(): Double
expect fun getDevicePixelRatio(): Double
expect fun getLocationHost(): String
expect fun alert(message: String)
expect fun uint8ArrayAdapterFactory(length: Int): Uint8ArrayAdapter
expect fun rtcPeerConnectionAdapterFactory(configuration: JsonObject): RTCPeerConnectionAdapter
expect fun audioAdapterFactory(src: String): AudioAdapter
expect fun setShellsStatusBar(percent: Double)
expect fun setArmorStatusBar(percent: Double)
expect fun setMinesStatusBar(percent: Double)
