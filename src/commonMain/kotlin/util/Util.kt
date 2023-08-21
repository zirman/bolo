package util

import bmap.Terrain

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

fun isWater(terrain: Terrain): Boolean =
    when (terrain) {
        Terrain.Sea,
        Terrain.River,
        Terrain.Boat,
        Terrain.SeaMined,
        -> true

        Terrain.Wall,
        Terrain.Swamp0,
        Terrain.Swamp1,
        Terrain.Swamp2,
        Terrain.Swamp3,
        Terrain.Crater,
        Terrain.Road,
        Terrain.Tree,
        Terrain.Rubble0,
        Terrain.Rubble1,
        Terrain.Rubble2,
        Terrain.Rubble3,
        Terrain.Grass0,
        Terrain.Grass1,
        Terrain.Grass2,
        Terrain.Grass3,
        Terrain.WallDamaged0,
        Terrain.WallDamaged1,
        Terrain.WallDamaged2,
        Terrain.WallDamaged3,
        Terrain.SwampMined,
        Terrain.CraterMined,
        Terrain.RoadMined,
        Terrain.ForestMined,
        Terrain.RubbleMined,
        Terrain.GrassMined,
        -> false
    }

fun isShore(terrain: Terrain): Boolean =
    when (terrain) {
        Terrain.Sea,
        Terrain.River,
        Terrain.SeaMined,
        -> false

        Terrain.Boat,
        Terrain.Wall,
        Terrain.Swamp0,
        Terrain.Swamp1,
        Terrain.Swamp2,
        Terrain.Swamp3,
        Terrain.Crater,
        Terrain.Road,
        Terrain.Tree,
        Terrain.Rubble0,
        Terrain.Rubble1,
        Terrain.Rubble2,
        Terrain.Rubble3,
        Terrain.Grass0,
        Terrain.Grass1,
        Terrain.Grass2,
        Terrain.Grass3,
        Terrain.WallDamaged0,
        Terrain.WallDamaged1,
        Terrain.WallDamaged2,
        Terrain.WallDamaged3,
        Terrain.SwampMined,
        Terrain.CraterMined,
        Terrain.RoadMined,
        Terrain.ForestMined,
        Terrain.RubbleMined,
        Terrain.GrassMined,
        -> true
    }

fun isDrivable(terrain: Terrain): Boolean =
    when (terrain) {
        Terrain.Sea,
        Terrain.River,
        Terrain.SeaMined,
        Terrain.Boat,
        Terrain.Wall,
        Terrain.WallDamaged0,
        Terrain.WallDamaged1,
        Terrain.WallDamaged2,
        Terrain.WallDamaged3,
        -> false

        Terrain.Swamp0,
        Terrain.Swamp1,
        Terrain.Swamp2,
        Terrain.Swamp3,
        Terrain.Crater,
        Terrain.Road,
        Terrain.Tree,
        Terrain.Rubble0,
        Terrain.Rubble1,
        Terrain.Rubble2,
        Terrain.Rubble3,
        Terrain.Grass0,
        Terrain.Grass1,
        Terrain.Grass2,
        Terrain.Grass3,
        Terrain.SwampMined,
        Terrain.CraterMined,
        Terrain.RoadMined,
        Terrain.ForestMined,
        Terrain.RubbleMined,
        Terrain.GrassMined,
        -> true
    }

fun getSpeedMax(terrain: Terrain): Float =
    when (terrain) {
        Terrain.River,
        Terrain.Swamp0,
        Terrain.Swamp1,
        Terrain.Swamp2,
        Terrain.Swamp3,
        Terrain.Crater,
        Terrain.Rubble0,
        Terrain.Rubble1,
        Terrain.Rubble2,
        Terrain.Rubble3,
        Terrain.SwampMined,
        Terrain.CraterMined,
        Terrain.RubbleMined,
        -> 75f / 128f

        Terrain.Tree,
        Terrain.ForestMined,
        -> 75f / 64f

        Terrain.Grass0,
        Terrain.Grass1,
        Terrain.Grass2,
        Terrain.Grass3,
        Terrain.GrassMined,
        -> 75f / 32f

        Terrain.Road,
        Terrain.Boat,
        Terrain.RoadMined,
        -> 25f / 8f

        Terrain.Sea,
        Terrain.Wall,
        Terrain.WallDamaged0,
        Terrain.WallDamaged1,
        Terrain.WallDamaged2,
        Terrain.WallDamaged3,
        Terrain.SeaMined,
        -> 0f
    }

fun getMaxAngularVelocity(terrain: Terrain): Float =
    when (terrain) {
        Terrain.River,
        Terrain.Swamp0,
        Terrain.Swamp1,
        Terrain.Swamp2,
        Terrain.Swamp3,
        Terrain.Crater,
        Terrain.Rubble0,
        Terrain.Rubble1,
        Terrain.Rubble2,
        Terrain.Rubble3,
        Terrain.SwampMined,
        Terrain.CraterMined,
        Terrain.RubbleMined,
        -> 5f / 8f

        Terrain.Tree,
        Terrain.ForestMined,
        -> 5f / 4f

        Terrain.Grass0,
        Terrain.Grass1,
        Terrain.Grass2,
        Terrain.Grass3,
        Terrain.GrassMined,
        Terrain.Road,
        Terrain.Boat,
        Terrain.RoadMined,
        -> 5f / 2f

        Terrain.Sea,
        Terrain.Wall,
        Terrain.WallDamaged0,
        Terrain.WallDamaged1,
        Terrain.WallDamaged2,
        Terrain.WallDamaged3,
        Terrain.SeaMined,
        -> 0f
    }

fun isBuildable(terrain: Terrain): Boolean =
    when (terrain) {
        Terrain.Sea,
        Terrain.River,
        Terrain.SeaMined,
        Terrain.Boat,
        Terrain.Wall,
        Terrain.WallDamaged0,
        Terrain.WallDamaged1,
        Terrain.WallDamaged2,
        Terrain.WallDamaged3,
        Terrain.Tree,
        -> false

        Terrain.Swamp0,
        Terrain.Swamp1,
        Terrain.Swamp2,
        Terrain.Swamp3,
        Terrain.Crater,
        Terrain.Road,
        Terrain.Rubble0,
        Terrain.Rubble1,
        Terrain.Rubble2,
        Terrain.Rubble3,
        Terrain.Grass0,
        Terrain.Grass1,
        Terrain.Grass2,
        Terrain.Grass3,
        Terrain.SwampMined,
        Terrain.CraterMined,
        Terrain.RoadMined,
        Terrain.ForestMined,
        Terrain.RubbleMined,
        Terrain.GrassMined,
        -> true
    }
