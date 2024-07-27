package bmap

import kotlinx.serialization.protobuf.ProtoBuf

private val bmapExtraSerializer = BmapExtra.serializer()

fun BmapExtra.toByteArray(): ByteArray {
    return ProtoBuf.encodeToByteArray(bmapExtraSerializer, this)
}

fun ByteArray.toBmapExtra(): BmapExtra {
    return ProtoBuf.decodeFromByteArray(
        deserializer = bmapExtraSerializer,
        bytes = this,
    )
}
