package client.bmap

import common.bmap.TypeTile
import common.bmap.TypeTile.BaseFriendly
import common.bmap.TypeTile.BaseHostile
import common.bmap.TypeTile.BaseNeutral
import common.bmap.TypeTile.Boat
import common.bmap.TypeTile.Crater
import common.bmap.TypeTile.CraterMined
import common.bmap.TypeTile.DamagedWall
import common.bmap.TypeTile.PillFriendly0
import common.bmap.TypeTile.PillFriendly1
import common.bmap.TypeTile.PillFriendly10
import common.bmap.TypeTile.PillFriendly11
import common.bmap.TypeTile.PillFriendly12
import common.bmap.TypeTile.PillFriendly13
import common.bmap.TypeTile.PillFriendly14
import common.bmap.TypeTile.PillFriendly15
import common.bmap.TypeTile.PillFriendly2
import common.bmap.TypeTile.PillFriendly3
import common.bmap.TypeTile.PillFriendly4
import common.bmap.TypeTile.PillFriendly5
import common.bmap.TypeTile.PillFriendly6
import common.bmap.TypeTile.PillFriendly7
import common.bmap.TypeTile.PillFriendly8
import common.bmap.TypeTile.PillFriendly9
import common.bmap.TypeTile.PillHostile0
import common.bmap.TypeTile.PillHostile1
import common.bmap.TypeTile.PillHostile10
import common.bmap.TypeTile.PillHostile11
import common.bmap.TypeTile.PillHostile12
import common.bmap.TypeTile.PillHostile13
import common.bmap.TypeTile.PillHostile14
import common.bmap.TypeTile.PillHostile15
import common.bmap.TypeTile.PillHostile2
import common.bmap.TypeTile.PillHostile3
import common.bmap.TypeTile.PillHostile4
import common.bmap.TypeTile.PillHostile5
import common.bmap.TypeTile.PillHostile6
import common.bmap.TypeTile.PillHostile7
import common.bmap.TypeTile.PillHostile8
import common.bmap.TypeTile.PillHostile9
import common.bmap.TypeTile.River
import common.bmap.TypeTile.Road
import common.bmap.TypeTile.RoadMined
import common.bmap.TypeTile.Rubble
import common.bmap.TypeTile.RubbleMined
import common.bmap.TypeTile.Sea
import common.bmap.TypeTile.SeaMined
import common.bmap.TypeTile.Tree
import common.bmap.TypeTile.TreeMined
import common.bmap.TypeTile.Wall

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
