package client

import adapters.HTMLCanvasElementAdapter
import adapters.RTCPeerConnectionAdapter
import adapters.WebGlRenderingContextAdapter
import adapters.WindowAdapter
import bmap.Bmap
import bmap.BmapCode
import bmap.WORLD_HEIGHT
import bmap.WORLD_WIDTH
import frame.Owner
import io.ktor.websocket.Frame
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import math.V2
import org.koin.core.qualifier.named
import org.koin.dsl.module

val gameModule = module {
    single<HTMLCanvasElementAdapter> { htmlCanvasElementAdapter }

    single<WebGlRenderingContextAdapter> {
        get<HTMLCanvasElementAdapter>()
            .getWebGlContext(
                arguments = buildJsonObject {
                    put("alpha", JsonPrimitive(false))
                },
            )
    }

    single<Deferred<TileProgram>>(named(WebGlProgram.Tile)) {
        get<WebGlRenderingContextAdapter>().tileProgramFactory(get())
    }

    single<Deferred<SpriteProgram>>(named(WebGlProgram.Sprite)) {
        get<WebGlRenderingContextAdapter>().spriteProgramFactory(get())
    }

    single<WindowAdapter> { windowAdapter }

    single<Control> { Control(windowAdapter, htmlCanvasElementAdapter) }

    factory<RTCPeerConnectionAdapter> {
        rtcPeerConnectionAdapterFactory(
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
    }

    factory<Tank> { (hasBuilder: Boolean) ->
        TankImpl(
            game = get(),
            hasBuilder = hasBuilder,
            tankShotAudioManager = get(named(Sound.TankShot)),
        )
    }

    factory<Shell> {
            (
                startPosition: V2,
                bearing: Float,
                fromBoat: Boolean,
                sightRange: Float,
            ),
        ->
        ShellImpl(
            game = get(),
            startPosition = startPosition,
            bearing = bearing,
            fromBoat = fromBoat,
            sightRange = sightRange,
        )
    }

    factory<Builder> {
            (
                startPosition: V2,
                buildOp: BuilderMission,
                material: Int,
                mines: Int,
            ),
        ->
        BuilderImpl(
            game = get(),
            startPosition = startPosition,
            buildMission = buildOp,
            material = material,
            mines = mines,
        )
    }

    single<ImageTileArray> {
        ImageTileArrayImpl(
            bmap = get(),
            owner = get(),
            imageTiles = uint8ArrayAdapterFactory(WORLD_WIDTH * WORLD_HEIGHT),
        )
    }

    single<AudioManager>(named(Sound.Bubbles)) { AudioManager("bubbles.mp3") }
    single<AudioManager>(named(Sound.Build)) { AudioManager("build.mp3") }
    single<AudioManager>(named(Sound.BuilderDeath)) { AudioManager("builder_death.mp3") }
    single<AudioManager>(named(Sound.BuilderDeathFar)) { AudioManager("builder_death_far.mp3") }
    single<AudioManager>(named(Sound.BuildFar)) { AudioManager("build_far.mp3") }
    single<AudioManager>(named(Sound.Explosion)) { AudioManager("explosion.mp3") }
    single<AudioManager>(named(Sound.ExplosionFar)) { AudioManager("explosion_far.mp3") }
    single<AudioManager>(named(Sound.HitTank)) { AudioManager("hit_tank.mp3") }
    single<AudioManager>(named(Sound.HitTankFar)) { AudioManager("hit_tank_far.mp3") }
    single<AudioManager>(named(Sound.HitTerrain)) { AudioManager("hit_terrain.mp3") }
    single<AudioManager>(named(Sound.HitTerrainFar)) { AudioManager("hit_terrain_far.mp3") }
    single<AudioManager>(named(Sound.HitTree)) { AudioManager("hit_tree.mp3") }
    single<AudioManager>(named(Sound.HitTreeFar)) { AudioManager("hit_tree_far.mp3") }
    single<AudioManager>(named(Sound.MessageReceived)) { AudioManager("message_received.mp3") }
    single<AudioManager>(named(Sound.Mine)) { AudioManager("mine.mp3") }
    single<AudioManager>(named(Sound.PillShot)) { AudioManager("pill_shot.mp3") }
    single<AudioManager>(named(Sound.ShotFar)) { AudioManager("shot_far.mp3") }
    single<AudioManager>(named(Sound.Sink)) { AudioManager("sink.mp3") }
    single<AudioManager>(named(Sound.SinkFar)) { AudioManager("sink_far.mp3") }
    single<AudioManager>(named(Sound.SuperBoom)) { AudioManager("super_boom.mp3") }
    single<AudioManager>(named(Sound.SuperBoomFar)) { AudioManager("super_boom_far.mp3") }
    single<AudioManager>(named(Sound.TankShot)) { AudioManager("tank_shot.mp3") }
    single<AudioManager>(named(Sound.Tree)) { AudioManager("tree.mp3") }
    single<AudioManager>(named(Sound.TreeFar)) { AudioManager("tree_far.mp3") }

    single<Game> {
            (
                sendChannel: SendChannel<Frame>,
                owner: Owner,
                bmap: Bmap,
                receiveChannel: ReceiveChannel<Frame>,
                bmapCode: BmapCode,
            ),
        ->
        GameImpl(
            scope = get(),
            sendChannel = sendChannel,
            receiveChannel = receiveChannel,
            control = get(),
            canvas = get(),
            tileProgram = get(named(WebGlProgram.Tile)),
            spriteProgram = get(named(WebGlProgram.Sprite)),
            tileArray = get(),
            owner = owner,
            bmap = bmap,
            bmapCode = bmapCode,
        )
    }
}
