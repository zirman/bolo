package client

import client.adapters.AudioAdapter
import client.adapters.HTMLCanvasElementAdapter
import client.adapters.RTCPeerConnectionAdapter
import client.adapters.Uint8ArrayAdapter
import client.adapters.WindowAdapter
import common.assert.never
import kotlinx.serialization.json.JsonObject

actual fun alert(message: String): Unit = never()
actual suspend fun awaitAnimationFrame(): Float = never()
actual fun getDevicePixelRatio(): Float = never()
actual fun getLocationHost(): String = never()
actual val windowAdapter: WindowAdapter = never()
actual val htmlCanvasElementAdapter: HTMLCanvasElementAdapter = never()
actual fun rtcPeerConnectionAdapterFactory(configuration: JsonObject): RTCPeerConnectionAdapter = never()
actual fun uint8ArrayAdapterFactory(length: Int): Uint8ArrayAdapter = never()
actual fun audioAdapterFactory(src: String): AudioAdapter = never()
actual fun setArmorStatusBar(percent: Float): Unit = never()
actual fun setShellsStatusBar(percent: Float): Unit = never()
actual fun setMinesStatusBar(percent: Float): Unit = never()
actual fun setMaterialStatusBar(percent: Float): Unit = never()
actual fun setBuilderMode(builderMode: BuilderMode): Unit = never()
