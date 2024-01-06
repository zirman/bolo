package client

import adapters.HTMLCanvasElementAdapter
import adapters.RTCPeerConnectionAdapter
import adapters.Uint8ArrayAdapter
import adapters.WindowAdapter
import bmap.Bmap
import bmap.BmapCode
import bmap.worldHeight
import bmap.worldWidth
import frame.Owner
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import math.V2

expect val canvas: Any
expect val windowAdapter: WindowAdapter
expect fun htmlCanvasElementAdapterFactory(canvas: Any): HTMLCanvasElementAdapter
expect fun uint8ArrayAdapterFactory(length: Int): Uint8ArrayAdapter
expect fun rtcPeerConnectionAdapterFactory(configuration: JsonObject): RTCPeerConnectionAdapter

class GameModuleImpl(
    val coroutineScope: CoroutineScope,
    val sendChannel: SendChannel<Frame>,
    val owner: Owner,
    val bmap: Bmap,
    val receiveChannel: ReceiveChannel<Frame>,
    val bmapCode: BmapCode,
) : GameModule {
    private val htmlCanvasElementAdapter = htmlCanvasElementAdapterFactory(canvas)

    private val webGLRenderingContext = htmlCanvasElementAdapter
        .getWebGlContext(
            arguments = buildJsonObject {
                put("alpha", JsonPrimitive(false))
            },
        )

    private val tileProgram = webGLRenderingContext.tileProgramFactory(coroutineScope)
    private val spriteProgram = webGLRenderingContext.spriteProgramFactory(coroutineScope)
    private val windowAdapter = client.windowAdapter
    private val control = Control(windowAdapter)

    private fun rtcPeerConnectionAdapterFactory() = rtcPeerConnectionAdapterFactory(
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
        },
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
        imageTiles = uint8ArrayAdapterFactory(worldWidth * worldHeight),
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
