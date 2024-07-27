package client

import common.bmap.TerrainTile

sealed interface BuildOp {
    data class Terrain(
        val terrain: TerrainTile,
        val col: Int,
        val row: Int,
        val result: (BuildResult) -> Unit,
    ) : BuildOp

    data class Mine(
        val col: Int,
        val row: Int,
        val result: (BuildResult) -> Unit,
    ) : BuildOp

    data object PillPlacement : BuildOp

    data class PillRepair(
        val index: Int,
        val material: Int,
    ) : BuildOp
}
