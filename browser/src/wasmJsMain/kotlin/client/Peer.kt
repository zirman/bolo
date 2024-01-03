package client

data class Peer(
    val peerConnection: JsAny,
    val dataChannel: JsAny,
    var tank: PeerTank? = null,
    var shells: List<PeerShell> = emptyList(),
    var builder: PeerBuilder? = null,
)
