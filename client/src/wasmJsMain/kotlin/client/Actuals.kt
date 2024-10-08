package client

import client.adapters.AudioAdapter
import client.adapters.AudioAdapterImpl
import client.adapters.HTMLCanvasElementAdapter
import client.adapters.HTMLCanvasElementAdapterImpl
import client.adapters.JSON
import client.adapters.RTCPeerConnectionAdapter
import client.adapters.RTCPeerConnectionAdapterImpl
import client.adapters.Uint8ArrayAdapter
import client.adapters.Uint8ArrayAdapterImpl
import client.adapters.WindowAdapter
import client.adapters.WindowAdapterImpl
import common.ARMOR_ID
import common.BUILDER_MODE_MINE_ID
import common.BUILDER_MODE_PILL_ID
import common.BUILDER_MODE_ROAD_ID
import common.BUILDER_MODE_TREE_ID
import common.BUILDER_MODE_WALL_ID
import common.CANVAS_ID
import common.MATERIAL_ID
import common.MINES_ID
import common.SHELLS_ID
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
    checkNotNull(document.getElementById(CANVAS_ID)) { "Canvas not found" } as HTMLCanvasElement
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
