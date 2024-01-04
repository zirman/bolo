package adapters

import kotlinx.serialization.json.JsonObject

interface RTCPeerConnectionAdapter {
    suspend fun setRemoteDescription(description: String)
    suspend fun setLocalDescription(description: String)
    suspend fun addIceCandidate(candidate: String)
    val localDescription: String?
    suspend fun createOffer(): String
    suspend fun createAnswer(): String
    fun setOnnegotiationneeded(callback: (String) -> Unit)
    fun setOnconnectionstatechange(callback: (connectionState: String) -> Unit)
    fun setOndatachannel(callback: (DataChannelAdapter) -> Unit)
    fun setOnicecandidate(callback: (candidate: String?) -> Unit)
    fun createDataChannel(label: String, options: JsonObject): DataChannelAdapter
}
