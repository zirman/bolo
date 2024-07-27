package client

import bmap.Entity
import bmap.Entity.*
import bmap.TerrainTile.*
import bmap.isSolid

fun Entity.isShore(owner: Int): Boolean = when (this) {
    is Pill -> isSolid()
    is Base -> isSolid(owner)
    is Terrain -> when (terrain) {
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
}

fun Entity.isShellable(owner: Int): Boolean = when (this) {
    is Pill -> isSolid()
    is Base -> isSolid(owner)
    is Terrain -> when (terrain) {
        Sea,
        River,
        SeaMined,
        Boat,
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
        SwampMined,
        CraterMined,
        RoadMined,
        RubbleMined,
        GrassMined,
        -> false

        Wall,
        Tree,
        WallDamaged0,
        WallDamaged1,
        WallDamaged2,
        WallDamaged3,
        TreeMined,
        -> true
    }
}
