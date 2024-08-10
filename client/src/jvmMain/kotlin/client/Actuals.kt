package client

import client.adapters.AudioAdapter
import client.adapters.HTMLCanvasElementAdapter
import client.adapters.RTCPeerConnectionAdapter
import client.adapters.Uint8ArrayAdapter
import client.adapters.WindowAdapter
import kotlinx.serialization.json.JsonObject

actual fun alert(message: String): Unit = throw UnsupportedOperationException("Alert")
actual suspend fun awaitAnimationFrame(): Float = throw UnsupportedOperationException("AwaitAnimationFrame")
actual fun getDevicePixelRatio(): Float = throw UnsupportedOperationException("GetDevicePixelRatio")
actual fun getLocationHost(): String = throw UnsupportedOperationException("GetLocationHost")
actual val windowAdapter: WindowAdapter = throw UnsupportedOperationException("GetWindowAdapter")
actual val htmlCanvasElementAdapter: HTMLCanvasElementAdapter = throw UnsupportedOperationException("GetHtmlCanvasElementAdapter")
actual fun rtcPeerConnectionAdapterFactory(configuration: JsonObject): RTCPeerConnectionAdapter = throw UnsupportedOperationException("GetRtcPeerConnectionAdapter")
actual fun uint8ArrayAdapterFactory(length: Int): Uint8ArrayAdapter = throw UnsupportedOperationException("GetUint8ArrayAdapter")
actual fun audioAdapterFactory(src: String): AudioAdapter = throw UnsupportedOperationException("GetAudioAdapter")
actual fun setArmorStatusBar(percent: Float): Unit = throw UnsupportedOperationException("SetArmorStatusBar")
actual fun setShellsStatusBar(percent: Float): Unit = throw UnsupportedOperationException("SetShellsStatusBar")
actual fun setMinesStatusBar(percent: Float): Unit = throw UnsupportedOperationException("SetMinesStatusBar")
actual fun setMaterialStatusBar(percent: Float): Unit = throw UnsupportedOperationException("SetMaterialStatusBar")
actual fun setBuilderMode(builderMode: BuilderMode): Unit = throw UnsupportedOperationException("SetBuilderMode")
