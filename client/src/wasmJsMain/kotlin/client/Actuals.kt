package client

import adapters.AudioAdapter
import adapters.AudioAdapterImpl
import adapters.HTMLCanvasElementAdapter
import adapters.HTMLCanvasElementAdapterImpl
import adapters.JSON
import adapters.RTCPeerConnectionAdapter
import adapters.RTCPeerConnectionAdapterImpl
import adapters.Uint8ArrayAdapter
import adapters.Uint8ArrayAdapterImpl
import adapters.WindowAdapter
import adapters.WindowAdapterImpl
import assert.assertNotNull
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.JsonObject
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLProgressElement
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual fun alert(message: String) {
    window.alert(message)
}

actual suspend fun awaitAnimationFrame(): Float = suspendCoroutine { continuation ->
    window.requestAnimationFrame { continuation.resume(it.toFloat()) }
}

actual fun getDevicePixelRatio(): Float = window.devicePixelRatio.toFloat()
actual fun getLocationHost(): String = window.location.host
actual val windowAdapter: WindowAdapter = WindowAdapterImpl()

actual val htmlCanvasElementAdapter: HTMLCanvasElementAdapter = HTMLCanvasElementAdapterImpl(
    document.getElementById(CANVAS_ID).assertNotNull("Canvas not found") as HTMLCanvasElement,
)

actual fun rtcPeerConnectionAdapterFactory(configuration: JsonObject): RTCPeerConnectionAdapter =
    RTCPeerConnectionAdapterImpl(JSON.parse(configuration.toString())!!)

actual fun uint8ArrayAdapterFactory(length: Int): Uint8ArrayAdapter = Uint8ArrayAdapterImpl(Uint8Array(length))

actual fun audioAdapterFactory(src: String): AudioAdapter = AudioAdapterImpl(src)

private val armorProgressElement = document.getElementById(ARMOR_ID) as HTMLProgressElement
actual fun setArmorStatusBar(percent: Float) {
    armorProgressElement.value = percent.toDouble()
}

private val shellProgressElement = document.getElementById(SHELLS_ID) as HTMLProgressElement
actual fun setShellsStatusBar(percent: Float) {
    shellProgressElement.value = percent.toDouble()
}

private val minesProgressElement = document.getElementById(MINES_ID) as HTMLProgressElement
actual fun setMinesStatusBar(percent: Float) {
    minesProgressElement.value = percent.toDouble()
}

private val materialProgressElement = document.getElementById(MATERIAL_ID) as HTMLProgressElement
actual fun setMaterialStatusBar(percent: Float) {
    materialProgressElement.value = percent.toDouble()
}

private val builderModeTreeElement = document.getElementById(BUILDER_MODE_TREE_ID) as HTMLInputElement
private val builderModeRoadElement = document.getElementById(BUILDER_MODE_ROAD_ID) as HTMLInputElement
private val builderModeWallElement = document.getElementById(BUILDER_MODE_WALL_ID) as HTMLInputElement
private val builderModePillElement = document.getElementById(BUILDER_MODE_PILL_ID) as HTMLInputElement
private val builderModeMineElement = document.getElementById(BUILDER_MODE_MINE_ID) as HTMLInputElement
actual fun setBuilderMode(builderMode: BuilderMode) {
    when (builderMode) {
        BuilderMode.Tree -> {
            builderModeTreeElement.checked = true
        }

        BuilderMode.Road -> {
            builderModeRoadElement.checked = true
        }

        BuilderMode.Wall -> {
            builderModeWallElement.checked = true
        }

        BuilderMode.Pill -> {
            builderModePillElement.checked = true
        }

        BuilderMode.Mine -> {
            builderModeMineElement.checked = true
        }
    }
}
