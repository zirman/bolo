@file:Suppress("unused")

package client

import adapters.AudioAdapter
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

expect val windowAdapter: WindowAdapter
expect val htmlCanvasElementAdapter: HTMLCanvasElementAdapter
expect fun uint8ArrayAdapterFactory(length: Int): Uint8ArrayAdapter
expect fun rtcPeerConnectionAdapterFactory(configuration: JsonObject): RTCPeerConnectionAdapter
expect fun audioAdapterFactory(src: String): AudioAdapter

class GameModuleImpl(
    val coroutineScope: CoroutineScope,
    val sendChannel: SendChannel<Frame>,
    val owner: Owner,
    val bmap: Bmap,
    val receiveChannel: ReceiveChannel<Frame>,
    val bmapCode: BmapCode,
) : GameModule {
    private val htmlCanvasElementAdapter = client.htmlCanvasElementAdapter

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
            tankShotAudioManager = tankShotAudioManager,
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

    private val bubblesAudioManager: AudioManager = AudioManager("bubbles.mp3")
    private val buildAudioManager: AudioManager = AudioManager("build.mp3")
    private val builderDeathAudioManager: AudioManager = AudioManager("builder_death.mp3")
    private val builderDeathFarAudioManager: AudioManager = AudioManager("builder_death_far.mp3")
    private val buildFarAudioManager: AudioManager = AudioManager("build_far.mp3")
    private val explosionAudioManager: AudioManager = AudioManager("explosion.mp3")
    private val explosionFarAudioManager: AudioManager = AudioManager("explosion_far.mp3")
    private val hitTankAudioManager: AudioManager = AudioManager("hit_tank.mp3")
    private val hitTankFarAudioManager: AudioManager = AudioManager("hit_tank_far.mp3")
    private val hitTerrainAudioManager: AudioManager = AudioManager("hit_terrain.mp3")
    private val hitTerrainFarAudioManager: AudioManager = AudioManager("hit_terrain_far.mp3")
    private val hitTreeAudioManager: AudioManager = AudioManager("hit_tree.mp3")
    private val hitTreeFarAudioManager: AudioManager = AudioManager("hit_tree_far.mp3")
    private val messageReceivedAudioManager: AudioManager = AudioManager("message_received.mp3")
    private val mineAudioManager: AudioManager = AudioManager("mine.mp3")
    private val pillShotAudioManager: AudioManager = AudioManager("pill_shot.mp3")
    private val shotFarAudioManager: AudioManager = AudioManager("shot_far.mp3")
    private val sinkAudioManager: AudioManager = AudioManager("sink.mp3")
    private val sinkFarAudioManager: AudioManager = AudioManager("sink_far.mp3")
    private val superBoomAudioManager: AudioManager = AudioManager("super_boom.mp3")
    private val superBoomFarAudioManager: AudioManager = AudioManager("super_boom_far.mp3")
    private val tankShotAudioManager: AudioManager = AudioManager("tank_shot.mp3")
    private val treeAudioManager: AudioManager = AudioManager("tree.mp3")
    private val treeFarAudioManager: AudioManager = AudioManager("tree_far.mp3")

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
