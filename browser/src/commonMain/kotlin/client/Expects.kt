package client

import adapters.AudioAdapter
import adapters.HTMLCanvasElementAdapter
import adapters.RTCPeerConnectionAdapter
import adapters.Uint8ArrayAdapter
import adapters.WindowAdapter
import kotlinx.serialization.json.JsonObject

expect val windowAdapter: WindowAdapter
expect val htmlCanvasElementAdapter: HTMLCanvasElementAdapter

expect fun alert(message: String)
expect suspend fun awaitAnimationFrame(): Float
expect fun getDevicePixelRatio(): Float
expect fun getLocationHost(): String
expect fun uint8ArrayAdapterFactory(length: Int): Uint8ArrayAdapter
expect fun rtcPeerConnectionAdapterFactory(configuration: JsonObject): RTCPeerConnectionAdapter
expect fun audioAdapterFactory(src: String): AudioAdapter
expect fun setArmorStatusBar(percent: Float)
expect fun setShellsStatusBar(percent: Float)
expect fun setMinesStatusBar(percent: Float)
expect fun setMaterialStatusBar(percent: Float)
expect fun setBuilderMode(builderMode: BuilderMode)
