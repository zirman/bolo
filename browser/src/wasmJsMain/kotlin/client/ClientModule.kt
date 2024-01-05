@file:Suppress("MemberVisibilityCanBePrivate")

package client

import adapters.HTMLCanvasElementAdapterImpl
import adapters.JSON
import adapters.RTCPeerConnectionAdapterImpl
import adapters.RenderingContextAdapterImpl
import adapters.WindowAdapterImpl
import assert.assertNotNull
import bmap.Bmap
import bmap.BmapCode
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
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import math.V2
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.DEPTH_TEST
import org.khronos.webgl.WebGLRenderingContext.Companion.ONE_MINUS_SRC_ALPHA
import org.khronos.webgl.WebGLRenderingContext.Companion.SRC_ALPHA

class ClientApplicationModuleImpl : ClientApplicationModule {
    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            window.alert("${throwable.message}\n${throwable.stackTraceToString()}")
        }
    }

    override val coroutineScope = CoroutineScope(coroutineExceptionHandler)

    override val httpClient = HttpClient { install(WebSockets) }

    override lateinit var clientApplication: ClientApplication

    override fun start() {
        ClientApplicationImpl(this)
    }
}

class GameModuleImpl(
    override val clientApplicationModule: ClientApplicationModule,
    override val sendChannel: SendChannel<Frame>,
    override val owner: Owner,
    override val bmap: Bmap,
    override val receiveChannel: ReceiveChannel<Frame>,
    override val bmapCode: BmapCode,
) : GameModule {
    override val coroutineScope = clientApplicationModule.coroutineScope

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
    lateinit var game: Game

    override val rtcPeerConnectionAdapter = RTCPeerConnectionAdapterImpl(
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

    override fun tank(hasBuilder: Boolean): Tank {
        return TankImpl(
            scope = coroutineScope,
            game = game,
            hasBuilder = hasBuilder,
        )
    }

    override fun shell(
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

    override fun builder(
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

    override fun start() {
        game = GameImpl(
            module = this,
            scope = coroutineScope,
            control = control,
            canvas = htmlCanvasElementAdapter,
            tileProgram = tileProgram,
            spriteProgram = spriteProgram,
            sendChannel = sendChannel,
            owner = owner,
            bmap = bmap,
            receiveChannel = receiveChannel,
            bmapCode = bmapCode,
            tileArray = tileArray,
        )
    }
}
