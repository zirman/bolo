package client

import adapters.AudioAdapter
import adapters.AudioAdapterImpl
import adapters.HTMLCanvasElementAdapter
import adapters.HTMLCanvasElementAdapterImpl
import adapters.RTCPeerConnectionAdapter
import adapters.RTCPeerConnectionAdapterImpl
import adapters.Uint8ArrayAdapter
import adapters.Uint8ArrayAdapterImpl
import adapters.WindowAdapter
import adapters.WindowAdapterImpl
import assert.assertNotNull
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.awaitAnimationFrame
import kotlinx.serialization.json.JsonObject
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLProgressElement

actual suspend fun awaitAnimationFrame(): Double = window.awaitAnimationFrame()
actual fun getDevicePixelRatio(): Double = window.devicePixelRatio
actual fun getLocationHost(): String = window.location.host
actual val windowAdapter: WindowAdapter = WindowAdapterImpl()

actual val htmlCanvasElementAdapter: HTMLCanvasElementAdapter = HTMLCanvasElementAdapterImpl(
    document.getElementById(canvasId).assertNotNull("Canvas not found") as HTMLCanvasElement,
)

actual fun rtcPeerConnectionAdapterFactory(configuration: JsonObject): RTCPeerConnectionAdapter =
    RTCPeerConnectionAdapterImpl(JSON.parse(configuration.toString()))

actual fun uint8ArrayAdapterFactory(length: Int): Uint8ArrayAdapter = Uint8ArrayAdapterImpl(Uint8Array(length))

actual fun audioAdapterFactory(src: String): AudioAdapter = AudioAdapterImpl(src)

private val shellProgressElement = document.getElementById(shellsId) as HTMLProgressElement
actual fun setShellsStatusBar(percent: Double) {
    shellProgressElement.value = percent
}

private val armorProgressElement = document.getElementById(armorId) as HTMLProgressElement
actual fun setArmorStatusBar(percent: Double) {
    armorProgressElement.value = percent
}

private val minesProgressElement = document.getElementById(minesId) as HTMLProgressElement
actual fun setMinesStatusBar(percent: Double) {
    minesProgressElement.value = percent
}

actual fun alert(message: String) {
    window.alert(message)
}
