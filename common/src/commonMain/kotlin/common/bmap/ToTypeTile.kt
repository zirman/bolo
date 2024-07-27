package common.bmap

fun TerrainTile.toTypeTile(): TypeTile = when (this) {
    TerrainTile.Sea -> TypeTile.Sea
    TerrainTile.Boat -> TypeTile.Boat
    TerrainTile.Wall -> TypeTile.Wall
    TerrainTile.River -> TypeTile.River
    TerrainTile.Swamp0,
    TerrainTile.Swamp1,
    TerrainTile.Swamp2,
    TerrainTile.Swamp3,
    -> TypeTile.Swamp

    TerrainTile.Crater -> TypeTile.Crater
    TerrainTile.Road -> TypeTile.Road
    TerrainTile.Tree -> TypeTile.Tree
    TerrainTile.Rubble0,
    TerrainTile.Rubble1,
    TerrainTile.Rubble2,
    TerrainTile.Rubble3,
    -> TypeTile.Rubble

    TerrainTile.Grass0,
    TerrainTile.Grass1,
    TerrainTile.Grass2,
    TerrainTile.Grass3,
    -> TypeTile.Grass

    TerrainTile.WallDamaged0,
    TerrainTile.WallDamaged1,
    TerrainTile.WallDamaged2,
    TerrainTile.WallDamaged3,
    -> TypeTile.DamagedWall

    TerrainTile.SeaMined -> TypeTile.SeaMined
    TerrainTile.SwampMined -> TypeTile.SwampMined
    TerrainTile.CraterMined -> TypeTile.CraterMined
    TerrainTile.RoadMined -> TypeTile.RoadMined
    TerrainTile.TreeMined -> TypeTile.TreeMined
    TerrainTile.RubbleMined -> TypeTile.RubbleMined
    TerrainTile.GrassMined -> TypeTile.GrassMined
}
