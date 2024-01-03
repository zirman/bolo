package adapters

import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

class RTCPeerConnectionAdapterImpl : RTCPeerConnectionAdapter {
    private val rtcPeerConnection: dynamic = createRTCPeerConnection()

    override suspend fun setRemoteDescription(description: String) {
        println("setRemoteDescription $description")
        rtcPeerConnection.setRemoteDescription(JSON.parse(description)).unsafeCast<Promise<Any?>>().await()
    }

    override suspend fun setLocalDescription(description: String) {
        rtcPeerConnection.setLocalDescription(JSON.parse(description)).unsafeCast<Promise<Any?>>().await()
    }

    override suspend fun addIceCandidate(candidate: String) {
        rtcPeerConnection.addIceCandidate(JSON.parse(candidate)).unsafeCast<Promise<Any?>>().await()
    }

    override val localDescription: String?
        get() = rtcPeerConnection.localDescription?.unsafeCast<Json?>()?.let { JSON.stringify(it) }

    override suspend fun createOffer(): String {
        return JSON.stringify(rtcPeerConnection.createOffer().unsafeCast<Promise<Json>>().await())
    }

    override suspend fun createAnswer(): String {
        return JSON.stringify(rtcPeerConnection.createAnswer().unsafeCast<Promise<Json>>().await())
    }

    override fun setOnnegotiationneeded(callback: (String) -> Unit) {
        rtcPeerConnection.onnegotiationneeded = { event: dynamic ->
            callback(JSON.stringify(event))
        }
    }

    override fun setOnconnectionstatechange(callback: (connectionState: String) -> Unit) {
        rtcPeerConnection.onconnectionstatechange = {
            callback(rtcPeerConnection.connectionState.unsafeCast<String>())
        }
    }

    override fun setOndatachannel(callback: (DataChannelAdapter) -> Unit) {
        rtcPeerConnection.ondatachannel = { event: dynamic ->
            callback(DataChannelAdapterImpl(event.channel))
        }
    }

    override fun setOnicecandidate(callback: (candidate: String?) -> Unit) {
        rtcPeerConnection.onicecandidate = { event: dynamic ->
            callback(event?.candidate?.unsafeCast<Json?>()?.let { JSON.stringify(it) })
        }
    }

    override fun createDataChannel(label: String, options: Map<String, Any>): DataChannelAdapter {
        return DataChannelAdapterImpl(rtcPeerConnection.createDataChannel(label, json(*options.toList().toTypedArray())))
    }

    companion object {
        private fun createRTCPeerConnection(): dynamic = js(
            """
    new RTCPeerConnection({
        iceServers: [
            {
                urls: ["stun:robch.dev", "turn:robch.dev"],
                username: "prouser",
                credential: "BE3pJ@",
            },
        ],
    })
    """,
        )
    }
}
