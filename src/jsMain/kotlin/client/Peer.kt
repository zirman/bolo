package client

import kotlinx.serialization.Serializable

data class Peer(
    val peerConnection: dynamic,
    val dataChannel: dynamic,
    var tank: PeerTank? = null,
    var shells: List<PeerShell> = emptyList(),
    var builder: PeerBuilder? = null,
)

@Serializable
data class PeerUpdate(
    val tank: PeerTank?,
    val shells: List<PeerShell>,
    val builder: PeerBuilder?,
)

@Serializable
data class PeerTank(
    val positionX: Float,
    val positionY: Float,
    val bearing: Float,
    val onBoat: Boolean,
)

@Serializable
data class PeerShell(
    val positionX: Float,
    val positionY: Float,
    val bearing: Float,
)

@Serializable
data class PeerBuilder(
    val positionX: Float,
    val positionY: Float,
)
