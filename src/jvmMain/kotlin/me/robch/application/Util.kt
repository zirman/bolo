@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package me.robch.application

import frame.FrameClient
import frame.FrameServer
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
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
