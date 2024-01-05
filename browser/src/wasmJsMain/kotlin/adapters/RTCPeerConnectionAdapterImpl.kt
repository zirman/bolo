package adapters

import kotlinx.coroutines.await
import kotlinx.serialization.json.JsonObject

class RTCPeerConnectionAdapterImpl(configuration: JsAny) : RTCPeerConnectionAdapter {
    private val rtcPeerConnection: RTCPeerConnection = RTCPeerConnection(configuration)

    override suspend fun setRemoteDescription(description: String) {
        println("setRemoteDescription $description")
        return rtcPeerConnection.setRemoteDescription(JSON.parse(description)!!).await()
    }

    override suspend fun setLocalDescription(description: String) {
        return rtcPeerConnection.setLocalDescription(JSON.parse(description)!!).await()
    }

    override suspend fun addIceCandidate(candidate: String) {
        return rtcPeerConnection.addIceCandidate(JSON.parse(candidate)!!).await()
    }

    override val localDescription: String?
        get() = rtcPeerConnection.localDescription?.let { JSON.stringify(it) }

    override suspend fun createOffer(): String {
        return JSON.stringify(rtcPeerConnection.createOffer().await())
    }

    override suspend fun createAnswer(): String {
        return JSON.stringify(rtcPeerConnection.createAnswer().await())
    }

    override fun setOnnegotiationneeded(callback: (String) -> Unit) {
        rtcPeerConnection.onnegotiationneeded = { event ->
            callback(JSON.stringify(event))
        }
    }

    override fun setOnconnectionstatechange(callback: (connectionState: String) -> Unit) {
        rtcPeerConnection.onconnectionstatechange = {
            callback(rtcPeerConnection.connectionState)
        }
    }

    override fun setOndatachannel(callback: (DataChannelAdapter) -> Unit) {
        rtcPeerConnection.ondatachannel = { event ->
            callback(DataChannelAdapterImpl(event.channel))
        }
    }

    override fun setOnicecandidate(callback: (candidate: String?) -> Unit) {
        rtcPeerConnection.onicecandidate = { event ->
            callback(event.candidate?.let { JSON.stringify(it) })
        }
    }

    override fun createDataChannel(label: String, options: JsonObject): DataChannelAdapter {
        return DataChannelAdapterImpl(
            rtcPeerConnection.createDataChannel(
                label = label,
                options = JSON.parse(options.toString())!!,
            ),
        )
    }
}
