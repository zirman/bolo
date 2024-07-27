@file:OptIn(ExperimentalSerializationApi::class)

package server

import common.frame.FrameClient
import common.frame.FrameServer
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

internal val frameServerSerializer = FrameServer.serializer()

internal fun FrameServer.toByteArray(): ByteArray {
    return ProtoBuf.encodeToByteArray(frameServerSerializer, this)
}

internal val frameClientSerializer = FrameClient.serializer()

internal fun Frame.Binary.toFrameClient(): FrameClient {
    return ProtoBuf.decodeFromByteArray(
        deserializer = frameClientSerializer,
        bytes = this.readBytes()
    )
}
