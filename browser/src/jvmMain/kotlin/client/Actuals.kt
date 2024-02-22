package client

import adapters.AudioAdapter
import adapters.HTMLCanvasElementAdapter
import adapters.RTCPeerConnectionAdapter
import adapters.Uint8ArrayAdapter
import adapters.WindowAdapter
import assert.never
import kotlinx.serialization.json.JsonObject

actual fun alert(message: String): Unit = never()
actual suspend fun awaitAnimationFrame(): Double = never()
actual fun getDevicePixelRatio(): Double = never()
actual fun getLocationHost(): String = never()
actual val windowAdapter: WindowAdapter = never()
actual val htmlCanvasElementAdapter: HTMLCanvasElementAdapter = never()
actual fun rtcPeerConnectionAdapterFactory(configuration: JsonObject): RTCPeerConnectionAdapter = never()
actual fun uint8ArrayAdapterFactory(length: Int): Uint8ArrayAdapter = never()
actual fun audioAdapterFactory(src: String): AudioAdapter = never()
actual fun setShellsStatusBar(percent: Double): Unit = never()
actual fun setArmorStatusBar(percent: Double): Unit = never()
actual fun setMinesStatusBar(percent: Double): Unit = never()
actual fun setBuilderMode(builderMode: BuilderMode): Unit = never()
