package client

import adapters.HTMLCanvasElementAdapter
import adapters.RTCPeerConnectionAdapter
import adapters.WindowAdapter
import bmap.Bmap
import bmap.BmapCode
import frame.Owner
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import math.V2

interface GameModule {
    val sendChannel: SendChannel<Frame>
    val owner: Owner
    val bmap: Bmap
    val receiveChannel: ReceiveChannel<Frame>
    val bmapCode: BmapCode
    val coroutineScope: CoroutineScope
    val htmlCanvasElementAdapter: HTMLCanvasElementAdapter
    val windowAdapter: WindowAdapter
    val control: Control
    val rtcPeerConnectionAdapter: RTCPeerConnectionAdapter
    val game: Game

    fun tankFactory(hasBuilder: Boolean): Tank

    fun shellFactory(
        startPosition: V2,
        bearing: Float,
        fromBoat: Boolean,
        sightRange: Float,
    ): Shell

    fun builderFactory(
        startPosition: V2,
        buildOp: BuilderMission,
    ): Builder
}
