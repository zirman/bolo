package bmap

import bmap.TypeTile.*

fun TypeTile.isTreeLikeTile(): Int = when (this) {
    Tree,
    TreeMined,
    -> 1

    else -> 0
}

fun TypeTile.isCraterLikeTile(): Int = when (this) {
    Crater,
    CraterMined,
    River,
    Sea,
    SeaMined,
    Boat,
    -> 1

    else -> 0
}

fun TypeTile.isRoadLikeTile(): Int = when (this) {
    Road,
    RoadMined,
    BaseFriendly,
    BaseHostile,
    BaseNeutral,
    PillFriendly0,
    PillFriendly1,
    PillFriendly2,
    PillFriendly3,
    PillFriendly4,
    PillFriendly5,
    PillFriendly6,
    PillFriendly7,
    PillFriendly8,
    PillFriendly9,
    PillFriendly10,
    PillFriendly11,
    PillFriendly12,
    PillFriendly13,
    PillFriendly14,
    PillFriendly15,
    PillHostile0,
    PillHostile1,
    PillHostile2,
    PillHostile3,
    PillHostile4,
    PillHostile5,
    PillHostile6,
    PillHostile7,
    PillHostile8,
    PillHostile9,
    PillHostile10,
    PillHostile11,
    PillHostile12,
    PillHostile13,
    PillHostile14,
    PillHostile15,
    -> 1

    else -> 0
}

fun TypeTile.isWaterLikeToLandTile(): Int = when (this) {
    River,
    Boat,
    Sea,
    SeaMined,
    -> 1

    else -> 0
}

fun TypeTile.isWaterLikeToWaterTile(): Int = when (this) {
    Road,
    RoadMined,
    River,
    Boat,
    Sea,
    SeaMined,
    Crater,
    CraterMined,
    BaseFriendly,
    BaseHostile,
    BaseNeutral,
    PillFriendly0,
    PillFriendly1,
    PillFriendly2,
    PillFriendly3,
    PillFriendly4,
    PillFriendly5,
    PillFriendly6,
    PillFriendly7,
    PillFriendly8,
    PillFriendly9,
    PillFriendly10,
    PillFriendly11,
    PillFriendly12,
    PillFriendly13,
    PillFriendly14,
    PillFriendly15,
    PillHostile0,
    PillHostile1,
    PillHostile2,
    PillHostile3,
    PillHostile4,
    PillHostile5,
    PillHostile6,
    PillHostile7,
    PillHostile8,
    PillHostile9,
    PillHostile10,
    PillHostile11,
    PillHostile12,
    PillHostile13,
    PillHostile14,
    PillHostile15,
    -> 1

    else -> 0
}

fun TypeTile.isWallLikeTile(): Int = when (this) {
    Rubble,
    RubbleMined,
    Wall,
    DamagedWall,
    -> 1

    else -> 0
}

fun TypeTile.isSeaLikeTile(): Int = when (this) {
    Sea,
    SeaMined,
    -> 1

    else -> 0
}
