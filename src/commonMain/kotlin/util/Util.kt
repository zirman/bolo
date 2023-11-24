package util

import bmap.TerrainTile

const val canvasId = "bolo-canvas"

const val tankShellsMax = 40
const val tankMinesMax = 40
const val tankArmorMax = 40
const val tankMaterialMax = 40

const val pillsMax = 16
const val basesMax = 16
const val startsMax = 16

const val pillArmorMax = 15
const val pillSpeedMax = 50

const val materialPerTree = 4

const val roadPerMaterial = 2
const val wallPerMaterial = 2
const val boatPerMaterial = 20
const val pillPerMaterial = 4

const val baseArmorMax = 90
const val baseShellsMax = 90
const val baseMinesMax = 90

const val armorUnit = 5
const val shellsUnit = 1
const val minesUnit = 1

const val replenishBaseTime = 600f / 60f

const val refuelArmorTime = 46f / 60f
const val refuelShellTime = 7f / 60f
const val refuelMineTime = 7f / 60f

fun isWater(terrain: TerrainTile): Boolean =
    when (terrain) {
        TerrainTile.Sea,
        TerrainTile.River,
        TerrainTile.Boat,
        TerrainTile.SeaMined,
        -> true

        TerrainTile.Wall,
        TerrainTile.Swamp0,
        TerrainTile.Swamp1,
        TerrainTile.Swamp2,
        TerrainTile.Swamp3,
        TerrainTile.Crater,
        TerrainTile.Road,
        TerrainTile.Tree,
        TerrainTile.Rubble0,
        TerrainTile.Rubble1,
        TerrainTile.Rubble2,
        TerrainTile.Rubble3,
        TerrainTile.Grass0,
        TerrainTile.Grass1,
        TerrainTile.Grass2,
        TerrainTile.Grass3,
        TerrainTile.WallDamaged0,
        TerrainTile.WallDamaged1,
        TerrainTile.WallDamaged2,
        TerrainTile.WallDamaged3,
        TerrainTile.SwampMined,
        TerrainTile.CraterMined,
        TerrainTile.RoadMined,
        TerrainTile.TreeMined,
        TerrainTile.RubbleMined,
        TerrainTile.GrassMined,
        -> false
    }

fun isShore(terrain: TerrainTile): Boolean =
    when (terrain) {
        TerrainTile.Sea,
        TerrainTile.River,
        TerrainTile.SeaMined,
        -> false

        TerrainTile.Boat,
        TerrainTile.Wall,
        TerrainTile.Swamp0,
        TerrainTile.Swamp1,
        TerrainTile.Swamp2,
        TerrainTile.Swamp3,
        TerrainTile.Crater,
        TerrainTile.Road,
        TerrainTile.Tree,
        TerrainTile.Rubble0,
        TerrainTile.Rubble1,
        TerrainTile.Rubble2,
        TerrainTile.Rubble3,
        TerrainTile.Grass0,
        TerrainTile.Grass1,
        TerrainTile.Grass2,
        TerrainTile.Grass3,
        TerrainTile.WallDamaged0,
        TerrainTile.WallDamaged1,
        TerrainTile.WallDamaged2,
        TerrainTile.WallDamaged3,
        TerrainTile.SwampMined,
        TerrainTile.CraterMined,
        TerrainTile.RoadMined,
        TerrainTile.TreeMined,
        TerrainTile.RubbleMined,
        TerrainTile.GrassMined,
        -> true
    }

fun isDrivable(terrain: TerrainTile): Boolean =
    when (terrain) {
        TerrainTile.Sea,
        TerrainTile.River,
        TerrainTile.SeaMined,
        TerrainTile.Boat,
        TerrainTile.Wall,
        TerrainTile.WallDamaged0,
        TerrainTile.WallDamaged1,
        TerrainTile.WallDamaged2,
        TerrainTile.WallDamaged3,
        -> false

        TerrainTile.Swamp0,
        TerrainTile.Swamp1,
        TerrainTile.Swamp2,
        TerrainTile.Swamp3,
        TerrainTile.Crater,
        TerrainTile.Road,
        TerrainTile.Tree,
        TerrainTile.Rubble0,
        TerrainTile.Rubble1,
        TerrainTile.Rubble2,
        TerrainTile.Rubble3,
        TerrainTile.Grass0,
        TerrainTile.Grass1,
        TerrainTile.Grass2,
        TerrainTile.Grass3,
        TerrainTile.SwampMined,
        TerrainTile.CraterMined,
        TerrainTile.RoadMined,
        TerrainTile.TreeMined,
        TerrainTile.RubbleMined,
        TerrainTile.GrassMined,
        -> true
    }

fun getSpeedMax(terrain: TerrainTile): Float =
    when (terrain) {
        TerrainTile.River,
        TerrainTile.Swamp0,
        TerrainTile.Swamp1,
        TerrainTile.Swamp2,
        TerrainTile.Swamp3,
        TerrainTile.Crater,
        TerrainTile.Rubble0,
        TerrainTile.Rubble1,
        TerrainTile.Rubble2,
        TerrainTile.Rubble3,
        TerrainTile.SwampMined,
        TerrainTile.CraterMined,
        TerrainTile.RubbleMined,
        -> 75f / 128f

        TerrainTile.Tree,
        TerrainTile.TreeMined,
        -> 75f / 64f

        TerrainTile.Grass0,
        TerrainTile.Grass1,
        TerrainTile.Grass2,
        TerrainTile.Grass3,
        TerrainTile.GrassMined,
        -> 75f / 32f

        TerrainTile.Road,
        TerrainTile.Boat,
        TerrainTile.RoadMined,
        -> 25f / 8f

        TerrainTile.Sea,
        TerrainTile.Wall,
        TerrainTile.WallDamaged0,
        TerrainTile.WallDamaged1,
        TerrainTile.WallDamaged2,
        TerrainTile.WallDamaged3,
        TerrainTile.SeaMined,
        -> 0f
    }

fun getMaxAngularVelocity(terrain: TerrainTile): Float =
    when (terrain) {
        TerrainTile.River,
        TerrainTile.Swamp0,
        TerrainTile.Swamp1,
        TerrainTile.Swamp2,
        TerrainTile.Swamp3,
        TerrainTile.Crater,
        TerrainTile.Rubble0,
        TerrainTile.Rubble1,
        TerrainTile.Rubble2,
        TerrainTile.Rubble3,
        TerrainTile.SwampMined,
        TerrainTile.CraterMined,
        TerrainTile.RubbleMined,
        -> 5f / 8f

        TerrainTile.Tree,
        TerrainTile.TreeMined,
        -> 5f / 4f

        TerrainTile.Grass0,
        TerrainTile.Grass1,
        TerrainTile.Grass2,
        TerrainTile.Grass3,
        TerrainTile.GrassMined,
        TerrainTile.Road,
        TerrainTile.Boat,
        TerrainTile.RoadMined,
        -> 5f / 2f

        TerrainTile.Sea,
        TerrainTile.Wall,
        TerrainTile.WallDamaged0,
        TerrainTile.WallDamaged1,
        TerrainTile.WallDamaged2,
        TerrainTile.WallDamaged3,
        TerrainTile.SeaMined,
        -> 0f
    }

fun isBuildable(terrain: TerrainTile): Boolean =
    when (terrain) {
        TerrainTile.Sea,
        TerrainTile.River,
        TerrainTile.SeaMined,
        TerrainTile.Boat,
        TerrainTile.Wall,
        TerrainTile.WallDamaged0,
        TerrainTile.WallDamaged1,
        TerrainTile.WallDamaged2,
        TerrainTile.WallDamaged3,
        TerrainTile.Tree,
        -> false

        TerrainTile.Swamp0,
        TerrainTile.Swamp1,
        TerrainTile.Swamp2,
        TerrainTile.Swamp3,
        TerrainTile.Crater,
        TerrainTile.Road,
        TerrainTile.Rubble0,
        TerrainTile.Rubble1,
        TerrainTile.Rubble2,
        TerrainTile.Rubble3,
        TerrainTile.Grass0,
        TerrainTile.Grass1,
        TerrainTile.Grass2,
        TerrainTile.Grass3,
        TerrainTile.SwampMined,
        TerrainTile.CraterMined,
        TerrainTile.RoadMined,
        TerrainTile.TreeMined,
        TerrainTile.RubbleMined,
        TerrainTile.GrassMined,
        -> true
    }
