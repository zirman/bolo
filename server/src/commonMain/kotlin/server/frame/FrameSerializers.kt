@file:OptIn(ExperimentalSerializationApi::class)

package server.frame

import common.frame.FrameClient
import common.frame.FrameServer
import common.frame.frameClientSerializer
import common.frame.frameServerSerializer
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

internal fun FrameServer.toByteArray(): ByteArray {
    return ProtoBuf.encodeToByteArray(frameServerSerializer, this)
}

internal fun Frame.Binary.toFrameClient(): FrameClient {
    return ProtoBuf.decodeFromByteArray(
        deserializer = frameClientSerializer,
        bytes = this.readBytes(),
    )
}
