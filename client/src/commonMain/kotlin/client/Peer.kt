package client

import adapters.DataChannelAdapter
import adapters.RTCPeerConnectionAdapter
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf

data class Peer(
    val peerConnection: RTCPeerConnectionAdapter,
    val dataChannel: DataChannelAdapter,
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
    val x: Float,
    val y: Float,
    val bearing: Float,
    val onBoat: Boolean,
)

@Serializable
data class PeerShell(
    val x: Float,
    val y: Float,
    val bearing: Float,
)

@Serializable
data class PeerBuilder(
    val x: Float,
    val y: Float,
)

fun PeerUpdate.toHexString(): String {
    return ProtoBuf.encodeToHexString(
        serializer = peerUpdateSerializer,
        value = this,
    )
}

private val peerUpdateSerializer = PeerUpdate.serializer()

fun String.toPeerUpdate(): PeerUpdate {
    return ProtoBuf.decodeFromHexString(
        deserializer = peerUpdateSerializer,
        hex = this,
    )
}
