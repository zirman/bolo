@file:Suppress("MemberVisibilityCanBePrivate")

package client

import adapters.HTMLCanvasElementAdapterImpl
import bmap.Bmap
import bmap.BmapCode
import adapters.RTCPeerConnectionAdapterImpl
import adapters.RenderingContextAdapterImpl
import adapters.WindowAdapterImpl
import assert.assertNotNull
import frame.Owner
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.Frame
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import math.V2
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_TEST
import org.khronos.webgl.WebGLRenderingContext.Companion.ONE_MINUS_SRC_ALPHA
import org.khronos.webgl.WebGLRenderingContext.Companion.SRC_ALPHA
import kotlin.js.json

class ClientApplicationModuleImpl : ClientApplicationModule {
    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            window.alert("${throwable.message}\n${throwable.stackTraceToString()}")
        }
    }

    override val coroutineScope = CoroutineScope(coroutineExceptionHandler)

    override val httpClient = HttpClient { install(WebSockets) }

    fun gameModuleFactory(
        sendChannel: SendChannel<Frame>,
        owner: Owner,
        bmap: Bmap,
        receiveChannel: ReceiveChannel<Frame>,
        bmapCode: BmapCode,
    ): GameModule = GameModuleImpl(
        coroutineScope = coroutineScope,
        sendChannel = sendChannel,
        owner = owner,
        bmap = bmap,
        receiveChannel = receiveChannel,
        bmapCode = bmapCode,
    )

    override val clientApplication: ClientApplication = ClientApplicationImpl(
        coroutineScope = coroutineScope,
        httpClient = httpClient,
        gameModuleFactory = this::gameModuleFactory,
    )
}

class GameModuleImpl(
    override val coroutineScope: CoroutineScope,
    override val sendChannel: SendChannel<Frame>,
    override val owner: Owner,
    override val bmap: Bmap,
    override val receiveChannel: ReceiveChannel<Frame>,
    override val bmapCode: BmapCode,
) : GameModule {
    override val htmlCanvasElementAdapter = HTMLCanvasElementAdapterImpl(
        document.getElementById(canvasId) as? org.w3c.dom.HTMLCanvasElement
            ?: throw IllegalStateException("Canvas not found")
    )

    val webGLRenderingContext = htmlCanvasElementAdapter
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

    val tileProgram = webGLRenderingContext.createTileProgram(coroutineScope)
    val spriteProgram = webGLRenderingContext.createSpriteProgram(coroutineScope)
    override val windowAdapter = WindowAdapterImpl()
    override val control = Control(windowAdapter)

    override fun rtcPeerConnectionAdapterFactory() = RTCPeerConnectionAdapterImpl(
        json(
            "iceServers" to arrayOf(
                json(
                    "urls" to arrayOf("stun:robch.dev", "turn:robch.dev"),
                    "username" to "prouser",
                    "credential" to "BE3pJ@",
                ),
            ),
        ),
    )

    override fun tankFactory(hasBuilder: Boolean): Tank {
        return TankImpl(
            scope = coroutineScope,
            game = game,
            hasBuilder = hasBuilder,
        )
    }

    override fun shellFactory(
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

    override fun builderFactory(
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

    val tileArray = ImageTileArrayImpl(bmap, owner)

    override val game = GameImpl(
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
