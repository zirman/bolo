package client

import adapters.HTMLCanvasElementAdapterImpl
import adapters.JSON
import adapters.RTCPeerConnectionAdapterImpl
import adapters.RenderingContextAdapterImpl
import adapters.Uint8ArrayAdapterImpl
import adapters.WindowAdapterImpl
import assert.assertNotNull
import bmap.Bmap
import bmap.BmapCode
import bmap.worldHeight
import bmap.worldWidth
import frame.Owner
import io.ktor.websocket.Frame
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import math.V2
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_TEST
import org.khronos.webgl.WebGLRenderingContext.Companion.ONE_MINUS_SRC_ALPHA
import org.khronos.webgl.WebGLRenderingContext.Companion.SRC_ALPHA

class GameModule(
    val coroutineScope: CoroutineScope,
    val sendChannel: SendChannel<Frame>,
    val owner: Owner,
    val bmap: Bmap,
    val receiveChannel: ReceiveChannel<Frame>,
    val bmapCode: BmapCode,
) {
    private val htmlCanvasElementAdapter = HTMLCanvasElementAdapterImpl(
        document.getElementById(canvasId) as? org.w3c.dom.HTMLCanvasElement
            ?: throw IllegalStateException("Canvas not found")
    )

    private val webGLRenderingContext = htmlCanvasElementAdapter
        .getContext(
            contextId = "webgl",
            arguments = buildJsonObject {
                put("alpha", JsonPrimitive(false))
            },
        )
        .let { it as? RenderingContextAdapterImpl }
        .assertNotNull("Your browser does not have WebGl")
        .let { it.renderingContext as WebGLRenderingContext }
        .apply {
            if (getExtension("OES_texture_float") == null) {
                throw IllegalStateException("Your WebGL does not support floating point texture")
            }

            fun resize() {
                val realToCSSPixels = window.devicePixelRatio
                val displayWidth = (canvas.clientWidth * realToCSSPixels).toInt()
                val displayHeight = (canvas.clientHeight * realToCSSPixels).toInt()

                if (canvas.width != displayWidth ||
                    canvas.height != displayHeight
                ) {
                    canvas.width = displayWidth
                    canvas.height = displayHeight
                    viewport(x = 0, y = 0, displayWidth, displayHeight)
                }
            }

            resize()
            window.onresize = { resize() }

            blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
            disable(DEPTH_TEST)
        }

    private val tileProgram = webGLRenderingContext.createTileProgram(coroutineScope)
    private val spriteProgram = webGLRenderingContext.createSpriteProgram(coroutineScope)
    private val windowAdapter = WindowAdapterImpl()
    private val control = Control(windowAdapter)

    private fun rtcPeerConnectionAdapterFactory() = RTCPeerConnectionAdapterImpl(
        JSON.parse(
            buildJsonObject {
                put(
                    "iceServers", buildJsonArray {
                        add(
                            buildJsonObject {
                                put("urls", buildJsonArray {
                                    add(JsonPrimitive("stun:robch.dev"))
                                    add(JsonPrimitive("turn:robch.dev"))
                                })
                                put("username", JsonPrimitive("prouser"))
                                put("credential", JsonPrimitive("BE3pJ@"))
                            }
                        )
                    }
                )
            }.toString()
        )!!,
    )

    private fun tankFactory(hasBuilder: Boolean): Tank {
        return TankImpl(
            scope = coroutineScope,
            game = game,
            hasBuilder = hasBuilder,
        )
    }

    private fun shellFactory(
        startPosition: V2,
        bearing: Float,
        fromBoat: Boolean,
        sightRange: Float,
    ): Shell {
        return ShellImpl(
            scope = coroutineScope,
            game = game,
            startPosition = startPosition,
            bearing = bearing,
            fromBoat = fromBoat,
            sightRange = sightRange,
        )
    }

    private fun builderFactory(
        startPosition: V2,
        buildOp: BuilderMission,
    ): Builder {
        return BuilderImpl(
            scope = coroutineScope,
            game = game,
            startPosition = startPosition,
            buildMission = buildOp,
        )
    }

    private val tileArray = ImageTileArrayImpl(
        bmap = bmap,
        owner = owner,
        imageTiles = Uint8ArrayAdapterImpl(Uint8Array(worldWidth * worldHeight)),
    )

    private val game = GameImpl(
        scope = coroutineScope,
        sendChannel = sendChannel,
        receiveChannel = receiveChannel,
        control = control,
        canvas = htmlCanvasElementAdapter,
        tileProgram = tileProgram,
        spriteProgram = spriteProgram,
        tileArray = tileArray,
        owner = owner,
        bmap = bmap,
        bmapCode = bmapCode,
        rtcPeerConnectionAdapterFactory = this::rtcPeerConnectionAdapterFactory,
        tankFactory = this::tankFactory,
        builderFactory = this::builderFactory,
        shellFactory = this::shellFactory,
    )
}
