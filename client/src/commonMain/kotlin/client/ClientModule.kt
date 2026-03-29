package client

import client.adapters.HTMLCanvasElementAdapter
import client.adapters.RTCPeerConnectionAdapter
import client.adapters.WebGlRenderingContextAdapter
import client.adapters.WindowAdapter
import common.bmap.Bmap
import common.bmap.WORLD_HEIGHT
import common.bmap.WORLD_WIDTH
import common.frame.Owner
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

@ContributesTo(AppScope::class)
@BindingContainer
interface ClientModule {
    @Binds
    fun bindClientApplication(clientApplicationImpl: ClientApplicationImpl): ClientApplication

    @Suppress("TooManyFunctions")
    companion object {
        @SingleIn(AppScope::class)
        @Provides
        fun provideCoroutineScope(): CoroutineScope = CoroutineScope(
            object : AbstractCoroutineContextElement(CoroutineExceptionHandler),
                CoroutineExceptionHandler {
                private var once = false

                override fun handleException(context: CoroutineContext, exception: Throwable) {
                    if (once || exception is CancellationException) return
                    once = true
                    exception.printStackTrace()
                    alert(exception.message ?: "An error occurred")
                }
            },
        )

        @Provides
        fun provideHttpClient(): HttpClient = HttpClient { install(WebSockets) }

        @Provides
        @SingleIn(AppScope::class)
        fun provideHTMLCanvasElementAdapter(): HTMLCanvasElementAdapter = htmlCanvasElementAdapter

        @Provides
        @SingleIn(AppScope::class)
        fun provideWebGlRenderingContextAdapter(
            htmlCanvasElementAdapter: HTMLCanvasElementAdapter,
        ): WebGlRenderingContextAdapter = htmlCanvasElementAdapter.getWebGlContext(
            arguments = buildJsonObject {
                put("alpha", JsonPrimitive(false))
            },
        )

        @Provides
        @SingleIn(AppScope::class)
        fun provideDeferredTileProgram(
            webGlRenderingContextAdapter: WebGlRenderingContextAdapter,
            coroutineScope: CoroutineScope,
        ): Deferred<TileProgram> = webGlRenderingContextAdapter.tileProgramFactory(coroutineScope)

        @Provides
        @SingleIn(AppScope::class)
        fun provideDeferredSpriteProgram(
            webGlRenderingContextAdapter: WebGlRenderingContextAdapter,
            scope: CoroutineScope,
        ): Deferred<SpriteProgram> = webGlRenderingContextAdapter.spriteProgramFactory(scope)

        @Provides
        @SingleIn(AppScope::class)
        fun provideWindowAdapter(): WindowAdapter = windowAdapter

        @Provides
        @SingleIn(AppScope::class)
        fun provideControl(): Control = Control(windowAdapter, htmlCanvasElementAdapter)

        @Provides
        fun provideRTCPeerConnectionAdapter(): RTCPeerConnectionAdapter = rtcPeerConnectionAdapterFactory(
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

        @Provides
        fun provideImageTileArray(
            bmap: Bmap,
            owner: Owner,
        ): ImageTileArray = ImageTileArrayImpl(
            bmap = bmap,
            owner = owner,
            imageTiles = uint8ArrayAdapterFactory(WORLD_WIDTH * WORLD_HEIGHT),
        )

        @SingleIn(AppScope::class)
        @Named("Sound.Bubbles")
        @Provides
        fun provideSoundBubbles(): AudioManager = AudioManager("/files/bubbles.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.Build")
        @Provides
        fun provideSoundBuild(): AudioManager = AudioManager("/files/build.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.BuilderDeath")
        @Provides
        fun provideSoundBuilderDeath(): AudioManager = AudioManager("/files/builder_death.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.BuilderDeathFar")
        @Provides
        fun provideSoundBuilderDeathFar(): AudioManager = AudioManager("/files/builder_death_far.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.BuildFar")
        @Provides
        fun provideSoundBuildFar(): AudioManager = AudioManager("/files/build_far.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.Explosion")
        @Provides
        fun provideSoundSoundExplosion(): AudioManager = AudioManager("/files/explosion.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.ExplosionFar")
        @Provides
        fun provideSoundExplosionFar(): AudioManager = AudioManager("/files/explosion_far.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.HitTank")
        @Provides
        fun provideSoundHitTank(): AudioManager = AudioManager("/files/hit_tank.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.HitTankFar")
        @Provides
        fun provideSoundHitTankFar(): AudioManager = AudioManager("/files/hit_tank_far.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.HitTerrain")
        @Provides
        fun provideSoundHitTerrain(): AudioManager = AudioManager("/files/hit_terrain.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.HitTerrainFar")
        @Provides
        fun provideSoundHitTerrainFar(): AudioManager = AudioManager("/files/hit_terrain_far.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.HitTree")
        @Provides
        fun provideSoundHitTree(): AudioManager = AudioManager("/files/hit_tree.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.HitTreeFar")
        @Provides
        fun provideSoundHitTreeFar(): AudioManager = AudioManager("/files/hit_tree_far.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.MessageReceived")
        @Provides
        fun provideSoundMessageReceived(): AudioManager = AudioManager("/files/message_received.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.Mine")
        @Provides
        fun provideSoundMine(): AudioManager = AudioManager("/files/mine.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.PillShot")
        @Provides
        fun provideSoundPillShot(): AudioManager = AudioManager("/files/pill_shot.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.ShotFar")
        @Provides
        fun provideSoundShotFar(): AudioManager = AudioManager("/files/shot_far.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.Sink")
        @Provides
        fun provideSoundSink(): AudioManager = AudioManager("/files/sink.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.SinkFar")
        @Provides
        fun provideSoundSinkFar(): AudioManager = AudioManager("/files/sink_far.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.SuperBoom")
        @Provides
        fun provideSoundSuperBoom(): AudioManager = AudioManager("/files/super_boom.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.SuperBoomFar")
        @Provides
        fun provideSoundSuperBoomFar(): AudioManager = AudioManager("/files/super_boom_far.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.TankShot")
        @Provides
        fun provideSoundTankShot(): AudioManager = AudioManager("/files/tank_shot.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.Tree")
        @Provides
        fun provideSoundTree(): AudioManager = AudioManager("/files/tree.mp3")

        @SingleIn(AppScope::class)
        @Named("Sound.TreeFar")
        @Provides
        fun provideSoundTreeFar(): AudioManager = AudioManager("/files/tree_far.mp3")
    }
}
