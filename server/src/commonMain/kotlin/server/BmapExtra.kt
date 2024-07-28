package server

import common.bmap.Bmap
import common.bmap.BmapExtra

fun Bmap.toExtra(owner: Int): BmapExtra = BmapExtra(
    owner = owner,
    pillCodes = pills.map { it.code },
    baseCodes = bases.map { it.code },
)
