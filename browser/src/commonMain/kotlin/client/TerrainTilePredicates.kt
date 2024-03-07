package client

import bmap.TerrainTile
import bmap.TerrainTile.*

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

fun TerrainTile.isTreeBuildable(): Boolean = when (this) {
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
