package client

import bmap.TerrainTile

fun TypeTile.isTreeLikeTile(): Int = when (this) {
    TypeTile.Tree,
    TypeTile.TreeMined,
    -> 1

    else -> 0
}

fun TypeTile.isCraterLikeTile(): Int = when (this) {
    TypeTile.Crater,
    TypeTile.CraterMined,
    TypeTile.River,
    TypeTile.Sea,
    TypeTile.SeaMined,
    TypeTile.Boat,
    -> 1

    else -> 0
}

fun TypeTile.isRoadLikeTile(): Int = when (this) {
    TypeTile.Road,
    TypeTile.RoadMined,
    TypeTile.BaseFriendly,
    TypeTile.BaseHostile,
    TypeTile.BaseNeutral,
    TypeTile.PillFriendly0,
    TypeTile.PillFriendly1,
    TypeTile.PillFriendly2,
    TypeTile.PillFriendly3,
    TypeTile.PillFriendly4,
    TypeTile.PillFriendly5,
    TypeTile.PillFriendly6,
    TypeTile.PillFriendly7,
    TypeTile.PillFriendly8,
    TypeTile.PillFriendly9,
    TypeTile.PillFriendly10,
    TypeTile.PillFriendly11,
    TypeTile.PillFriendly12,
    TypeTile.PillFriendly13,
    TypeTile.PillFriendly14,
    TypeTile.PillFriendly15,
    TypeTile.PillHostile0,
    TypeTile.PillHostile1,
    TypeTile.PillHostile2,
    TypeTile.PillHostile3,
    TypeTile.PillHostile4,
    TypeTile.PillHostile5,
    TypeTile.PillHostile6,
    TypeTile.PillHostile7,
    TypeTile.PillHostile8,
    TypeTile.PillHostile9,
    TypeTile.PillHostile10,
    TypeTile.PillHostile11,
    TypeTile.PillHostile12,
    TypeTile.PillHostile13,
    TypeTile.PillHostile14,
    TypeTile.PillHostile15,
    -> 1

    else -> 0
}

fun TypeTile.isWaterLikeToLandTile(): Int = when (this) {
    TypeTile.River,
    TypeTile.Boat,
    TypeTile.Sea,
    TypeTile.SeaMined,
    -> 1

    else -> 0
}

fun TypeTile.isWaterLikeToWaterTile(): Int = when (this) {
    TypeTile.Road,
    TypeTile.RoadMined,
    TypeTile.River,
    TypeTile.Boat,
    TypeTile.Sea,
    TypeTile.SeaMined,
    TypeTile.Crater,
    TypeTile.CraterMined,
    TypeTile.BaseFriendly,
    TypeTile.BaseHostile,
    TypeTile.BaseNeutral,
    TypeTile.PillFriendly0,
    TypeTile.PillFriendly1,
    TypeTile.PillFriendly2,
    TypeTile.PillFriendly3,
    TypeTile.PillFriendly4,
    TypeTile.PillFriendly5,
    TypeTile.PillFriendly6,
    TypeTile.PillFriendly7,
    TypeTile.PillFriendly8,
    TypeTile.PillFriendly9,
    TypeTile.PillFriendly10,
    TypeTile.PillFriendly11,
    TypeTile.PillFriendly12,
    TypeTile.PillFriendly13,
    TypeTile.PillFriendly14,
    TypeTile.PillFriendly15,
    TypeTile.PillHostile0,
    TypeTile.PillHostile1,
    TypeTile.PillHostile2,
    TypeTile.PillHostile3,
    TypeTile.PillHostile4,
    TypeTile.PillHostile5,
    TypeTile.PillHostile6,
    TypeTile.PillHostile7,
    TypeTile.PillHostile8,
    TypeTile.PillHostile9,
    TypeTile.PillHostile10,
    TypeTile.PillHostile11,
    TypeTile.PillHostile12,
    TypeTile.PillHostile13,
    TypeTile.PillHostile14,
    TypeTile.PillHostile15,
    -> 1

    else -> 0
}

fun TypeTile.isWallLikeTile(): Int = when (this) {
    TypeTile.Rubble,
    TypeTile.RubbleMined,
    TypeTile.Wall,
    TypeTile.DamagedWall,
    -> 1

    else -> 0
}

fun TypeTile.isSeaLikeTile(): Int = when (this) {
    TypeTile.Sea,
    TypeTile.SeaMined,
    -> 1

    else -> 0
}

fun TerrainTile.isMinedTerrain(): Boolean = when (this) {
    TerrainTile.SwampMined,
    TerrainTile.CraterMined,
    TerrainTile.RoadMined,
    TerrainTile.TreeMined,
    TerrainTile.RubbleMined,
    TerrainTile.GrassMined,
    TerrainTile.SeaMined,
    -> true

    else -> false
}
