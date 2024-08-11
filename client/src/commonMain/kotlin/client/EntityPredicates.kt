package client

import common.bmap.Entity
import common.bmap.Entity.Base
import common.bmap.Entity.Pill
import common.bmap.Entity.Terrain
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
import common.bmap.isSolid

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
