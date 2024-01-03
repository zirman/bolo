package client

data class Peer(
    val peerConnection: Any,
    val dataChannel: Any,
    var tank: PeerTank? = null,
    var shells: List<PeerShell> = emptyList(),
    var builder: PeerBuilder? = null,
)
