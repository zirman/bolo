package common.bmap

import kotlinx.serialization.Serializable

@Serializable
data class BmapExtra(
    val owner: Int,
    val pillCodes: List<Int>,
    val baseCodes: List<Int>,
)
