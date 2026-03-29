package client

import common.bmap.Bmap
import common.bmap.BmapCode
import common.frame.Owner
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

@GraphExtension(GameScope::class)
interface GameGraph {
    val game: Game

    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    interface Factory {
        fun createGameGraph(
            @Provides outgoing: SendChannel<Frame>,
            @Provides incoming: ReceiveChannel<Frame>,
            @Provides bmap: Bmap,
            @Provides bmapCode: BmapCode,
            @Provides owner: Owner,
        ): GameGraph
    }
}
