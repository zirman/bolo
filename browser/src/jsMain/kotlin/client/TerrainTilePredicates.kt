package client

import bmap.TerrainTile
import bmap.TerrainTile.*

fun TerrainTile.isMinedTerrain(): Boolean = when (this) {
    SwampMined,
    CraterMined,
    RoadMined,
    TreeMined,
    RubbleMined,
    GrassMined,
    SeaMined,
    -> true

    else -> false
}
