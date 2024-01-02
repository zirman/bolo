package bmap

import kotlinx.serialization.Serializable

@Serializable
data class BmapExtra(
    val owner: Int,
    val pillCodes: List<Int>,
    val baseCodes: List<Int>,
)

fun Bmap.toExtra(owner: Int): BmapExtra =
    BmapExtra(
        owner = owner,
        pillCodes = pills.map { it.code },
        baseCodes = bases.map { it.code },
    )

fun BmapExtra.loadCodes(bmap: Bmap) {
    pillCodes.forEachIndexed { index, code ->
        bmap.pills[index].code = code
    }

    baseCodes.forEachIndexed { index, code ->
        bmap.bases[index].code = code
    }
}
