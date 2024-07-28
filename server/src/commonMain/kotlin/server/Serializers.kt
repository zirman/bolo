@file:OptIn(ExperimentalSerializationApi::class)

package server

import common.bmap.BmapExtra
import common.bmap.bmapExtraSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

fun BmapExtra.toByteArray(): ByteArray {
    return ProtoBuf.encodeToByteArray(bmapExtraSerializer, this)
}
