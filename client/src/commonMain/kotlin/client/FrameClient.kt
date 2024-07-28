package client

import common.frame.FrameClient
import common.frame.frameClientSerializer
import io.ktor.websocket.Frame
import kotlinx.serialization.protobuf.ProtoBuf

fun FrameClient.toFrame(): Frame {
    return ProtoBuf
        .encodeToByteArray(
            serializer = frameClientSerializer,
            value = this,
        )
        .let { Frame.Binary(fin = true, data = it) }
}
