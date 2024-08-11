package common

import common.bmap.TerrainTile
import common.bmap.TerrainTile.Boat
import common.bmap.TerrainTile.Crater
import common.bmap.TerrainTile.CraterMined
import common.bmap.TerrainTile.Grass0
import common.bmap.TerrainTile.Grass1
import common.bmap.TerrainTile.Grass2
import common.bmap.TerrainTile.Grass3
import common.bmap.TerrainTile.GrassMined
import common.bmap.TerrainTile.River
import common.bmap.TerrainTile.Road
import common.bmap.TerrainTile.RoadMined
import common.bmap.TerrainTile.Rubble0
import common.bmap.TerrainTile.Rubble1
import common.bmap.TerrainTile.Rubble2
import common.bmap.TerrainTile.Rubble3
import common.bmap.TerrainTile.RubbleMined
import common.bmap.TerrainTile.Sea
import common.bmap.TerrainTile.SeaMined
import common.bmap.TerrainTile.Swamp0
import common.bmap.TerrainTile.Swamp1
import common.bmap.TerrainTile.Swamp2
import common.bmap.TerrainTile.Swamp3
import common.bmap.TerrainTile.SwampMined
import common.bmap.TerrainTile.Tree
import common.bmap.TerrainTile.TreeMined
import common.bmap.TerrainTile.Wall
import common.bmap.TerrainTile.WallDamaged0
import common.bmap.TerrainTile.WallDamaged1
import common.bmap.TerrainTile.WallDamaged2
import common.bmap.TerrainTile.WallDamaged3

fun isWater(terrain: TerrainTile): Boolean = when (terrain) {
    Sea,
    River,
    Boat,
    SeaMined,
        -> true

    Wall,
    Swamp0,
    Swamp1,
    Swamp2,
    Swamp3,
    Crater,
    Road,
    Tree,
    Rubble0,
    Rubble1,
    Rubble2,
    Rubble3,
    Grass0,
    Grass1,
    Grass2,
    Grass3,
    WallDamaged0,
    WallDamaged1,
    WallDamaged2,
    WallDamaged3,
    SwampMined,
    CraterMined,
    RoadMined,
    TreeMined,
    RubbleMined,
    GrassMined,
        -> false
}

fun TerrainTile.isShore(): Boolean = when (this) {
    Sea,
    River,
    SeaMined,
        -> false

    Boat,
    Wall,
    Swamp0,
    Swamp1,
    Swamp2,
    Swamp3,
    Crater,
    Road,
    Tree,
    Rubble0,
    Rubble1,
    Rubble2,
    Rubble3,
    Grass0,
    Grass1,
    Grass2,
    Grass3,
    WallDamaged0,
    WallDamaged1,
    WallDamaged2,
    WallDamaged3,
    SwampMined,
    CraterMined,
    RoadMined,
    TreeMined,
    RubbleMined,
    GrassMined,
        -> true
}

fun TerrainTile.isDrivable(): Boolean = when (this) {
    Sea,
    River,
    SeaMined,
    Boat,
    Wall,
    WallDamaged0,
    WallDamaged1,
    WallDamaged2,
    WallDamaged3,
        -> false

    Swamp0,
    Swamp1,
    Swamp2,
    Swamp3,
    Crater,
    Road,
    Tree,
    Rubble0,
    Rubble1,
    Rubble2,
    Rubble3,
    Grass0,
    Grass1,
    Grass2,
    Grass3,
    SwampMined,
    CraterMined,
    RoadMined,
    TreeMined,
    RubbleMined,
    GrassMined,
        -> true
}

fun TerrainTile.getSpeedMax(): Float = when (this) {
    River,
    Swamp0,
    Swamp1,
    Swamp2,
    Swamp3,
    Crater,
    Rubble0,
    Rubble1,
    Rubble2,
    Rubble3,
    SwampMined,
    CraterMined,
    RubbleMined,
        -> 75f / 128f

    Tree,
    TreeMined,
        -> 75f / 64f

    Grass0,
    Grass1,
    Grass2,
    Grass3,
    GrassMined,
        -> 75f / 32f

    Road,
    Boat,
    RoadMined,
        -> 25f / 8f

    Sea,
    Wall,
    WallDamaged0,
    WallDamaged1,
    WallDamaged2,
    WallDamaged3,
    SeaMined,
        -> 0f
}

fun TerrainTile.getMaxAngularVelocity(): Float = when (this) {
    River,
    Swamp0,
    Swamp1,
    Swamp2,
    Swamp3,
    Crater,
    Rubble0,
    Rubble1,
    Rubble2,
    Rubble3,
    SwampMined,
    CraterMined,
    RubbleMined,
        -> 5f / 8f

    Tree,
    TreeMined,
        -> 5f / 4f

    Grass0,
    Grass1,
    Grass2,
    Grass3,
    GrassMined,
    Road,
    Boat,
    RoadMined,
        -> 5f / 2f

    Sea,
    Wall,
    WallDamaged0,
    WallDamaged1,
    WallDamaged2,
    WallDamaged3,
    SeaMined,
        -> 0f
}

fun TerrainTile.isMined(): Boolean = when (this) {
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

fun TerrainTile.isRoadBuildable(): Boolean = when (this) {
    River,
    Swamp0,
    Swamp1,
    Swamp2,
    Swamp3,
    Crater,
    Rubble0,
    Rubble1,
    Rubble2,
    Rubble3,
    Grass0,
    Grass1,
    Grass2,
    Grass3,
        -> true

    else -> false
}

fun TerrainTile.isWallBuildable(): Boolean = when (this) {
    Swamp0,
    Swamp1,
    Swamp2,
    Swamp3,
    Crater,
    Road,
    Rubble0,
    Rubble1,
    Rubble2,
    Rubble3,
    Grass0,
    Grass1,
    Grass2,
    Grass3,
    WallDamaged0,
    WallDamaged1,
    WallDamaged2,
    WallDamaged3,
        -> true

    else -> false
}

fun TerrainTile.isTreeGrowable(): Boolean = when (this) {
    Swamp0,
    Swamp1,
    Swamp2,
    Swamp3,
    Crater,
    Rubble0,
    Rubble1,
    Rubble2,
    Rubble3,
    Grass0,
    Grass1,
    Grass2,
    Grass3,
        -> true

    else -> false
}
