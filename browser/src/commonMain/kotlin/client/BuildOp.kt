package client

import bmap.TerrainTile

sealed interface BuildOp {
    data class Terrain(
        val terrain: TerrainTile,
        val col: Int,
        val row: Int,
        val result: (Boolean) -> Unit,
    ) : BuildOp

    data object PillPlacement : BuildOp

    data class PillRepair(
        val index: Int,
        val material: Int,
    ) : BuildOp
}
