package client

import common.bmap.BmapExtra
import common.bmap.bmapExtraSerializer
import kotlinx.serialization.protobuf.ProtoBuf

fun ByteArray.toBmapExtra(): BmapExtra {
    return ProtoBuf.decodeFromByteArray(
        deserializer = bmapExtraSerializer,
        bytes = this,
    )
}
