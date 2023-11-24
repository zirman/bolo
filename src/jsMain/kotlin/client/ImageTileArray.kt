package client

import bmap.Bmap
import bmap.TerrainTile
import bmap.border
import bmap.ind
import bmap.worldHeight
import bmap.worldWidth
import frame.Owner
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

const val tileSheetWidth: Int = 16
const val tileSheetHeight: Int = 16
const val tilesCount: Int = tileSheetWidth * tileSheetHeight

fun tileInd(x: Int, y: Int): Int = (tileSheetWidth * y) + x

enum class TypeTile {
    Sea,
    Boat,
    Wall,
    River,
    Swamp,
    Crater,
    Road,
    Forest,
    Rubble,
    Grass,
    DamagedWall,
    SeaMined,
    SwampMined,
    CraterMined,
    RoadMined,
    ForestMined,
    RubbleMined,
    GrassMined,
    BaseNeutral,
    BaseFriendly,
    BaseHostile,
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
}

fun TerrainTile.toTypeTile(): TypeTile =
    when (this) {
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
        TerrainTile.Tree -> TypeTile.Forest
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
        TerrainTile.ForestMined -> TypeTile.ForestMined
        TerrainTile.RubbleMined -> TypeTile.RubbleMined
        TerrainTile.GrassMined -> TypeTile.GrassMined
    }

class ImageTileArray(private val bmap: Bmap, private val owner: Owner) {
    val tiles: Uint8Array = Uint8Array(worldWidth * worldHeight)
        .also { tiles ->
            for (y in 0..<worldHeight) {
                for (x in 0..<worldWidth) {
                    tiles[ind(x, y)] = bmap[x, y].toTypeTile().ordinal.toByte()
                }
            }

            for (base in bmap.bases) {
                tiles[ind(base.x, base.y)] =
                    when (base.owner) {
                        0xff -> TypeTile.BaseNeutral
                        owner.int -> TypeTile.BaseFriendly
                        else -> TypeTile.BaseHostile
                    }.ordinal.toByte()
            }

            for (pill in bmap.pills) {
                if (pill.isPlaced) {
                    tiles[ind(pill.x, pill.y)] = (TypeTile.PillHostile0.ordinal + pill.armor).toByte()
                }
            }
        }

    val imageTiles: Uint8Array = Uint8Array(worldWidth * worldHeight)
        .also { tiles ->
            for (y in 0..<worldHeight) {
                for (x in 0..<worldWidth) {
                    tiles[ind(x, y)] = mapImage(x, y).index.toByte()
                }
            }
        }

    fun getTypeTile(x: Int, y: Int): TypeTile =
        if (x < 0 || x >= worldWidth || y < 0 || y >= worldHeight) TypeTile.SeaMined
        else TypeTile.entries[tiles[ind(x, y)].toInt()]

    fun getImageTile(x: Int, y: Int): ImageTile =
        if (x < 0 || x >= worldWidth || y < 0 || y >= worldHeight) ImageTile.SeaMined
        else ImageTile.entries[imageTiles[ind(x, y)].toInt()]

    fun update(x: Int, y: Int) {
        run {
            for (base in bmap.bases) {
                if (base.x == x && base.y == y) {
                    tiles[ind(base.x, base.y)] = when (base.owner) {
                        0xff -> TypeTile.BaseNeutral
                        owner.int -> TypeTile.BaseFriendly
                        else -> TypeTile.BaseHostile
                    }.ordinal.toByte()
                    return@run
                }
            }

            for (pill in bmap.pills) {
                if (pill.isPlaced && pill.x == x && pill.y == y) {
                    tiles[ind(pill.x, pill.y)] = run {
                        if (pill.owner == owner.int) TypeTile.PillFriendly0 else TypeTile.PillHostile0
                    }
                        .ordinal
                        .let { it + pill.armor }
                        .toByte()
                    return@run
                }
            }

            if (x >= border && x < worldWidth - border && y >= border && y < worldHeight - border) {
                tiles[ind(x, y)] = bmap[x, y].toTypeTile().ordinal.toByte()
            }
        }

        for (yy in y - 1..y + 1) {
            for (xx in x - 1..x + 1) {
                imageTiles[ind(xx, yy)] = mapImage(xx, yy).index.toByte()
            }
        }
    }


    private fun mapImage(x: Int, y: Int): ImageTile {
        return when (getTypeTile(x, y)) {
            TypeTile.Sea -> when (
                getTypeTile(x - 1, y).isSeaLikeTile()
                    .or(getTypeTile(x, y - 1).isSeaLikeTile().shl(1))
                    .or(getTypeTile(x + 1, y).isSeaLikeTile().shl(2))
                    .or(getTypeTile(x, y + 1).isSeaLikeTile().shl(3))
            ) {
                0, 5, 10, 15 -> ImageTile.Sea0
                4, 14 -> ImageTile.Sea1
                1, 11 -> ImageTile.Sea2
                8, 13 -> ImageTile.Sea3
                12 -> ImageTile.Sea4
                9 -> ImageTile.Sea5
                2, 7 -> ImageTile.Sea6
                6 -> ImageTile.Sea7
                3 -> ImageTile.Sea8
                else -> throw IllegalStateException("Impossible")
            }

            TypeTile.SeaMined -> ImageTile.SeaMined
            TypeTile.Swamp -> ImageTile.Swamp
            TypeTile.SwampMined -> ImageTile.SwampMined
            TypeTile.River -> {
                when (
                    getTypeTile(x - 1, y).isWaterLikeToWaterTile()
                        .or(getTypeTile(x, y - 1).isWaterLikeToWaterTile().shl(1))
                        .or(getTypeTile(x + 1, y).isWaterLikeToWaterTile().shl(2))
                        .or(getTypeTile(x, y + 1).isWaterLikeToWaterTile().shl(3))
                ) {
                    0 -> ImageTile.River0
                    4 -> ImageTile.River1
                    5 -> ImageTile.River2
                    1 -> ImageTile.River3
                    8 -> ImageTile.River4
                    12 -> ImageTile.River5
                    13 -> ImageTile.River6
                    9 -> ImageTile.River7
                    10 -> ImageTile.River8
                    14 -> ImageTile.River9
                    15 -> ImageTile.River10
                    11 -> ImageTile.River11
                    2 -> ImageTile.River12
                    6 -> ImageTile.River13
                    7 -> ImageTile.River14
                    3 -> ImageTile.River15
                    else -> throw IllegalStateException("Impossible")
                }
            }

            TypeTile.Grass -> ImageTile.Grass
            TypeTile.GrassMined -> ImageTile.GrassMined
            TypeTile.Forest -> ImageTile.Forest
            TypeTile.ForestMined -> ImageTile.ForestMined
            TypeTile.Crater -> ImageTile.Crater
            TypeTile.CraterMined -> ImageTile.CraterMined
            TypeTile.Road -> ImageTile.Road
            TypeTile.RoadMined -> ImageTile.RoadMined
            TypeTile.Rubble -> ImageTile.Rubble
            TypeTile.RubbleMined -> ImageTile.RubbleMined
            TypeTile.DamagedWall -> ImageTile.DamagedWall
            TypeTile.Wall -> ImageTile.Wall
            TypeTile.Boat -> ImageTile.Boat
            TypeTile.BaseFriendly -> ImageTile.BaseFriendly
            TypeTile.PillFriendly0 -> ImageTile.PillFriendly0
            TypeTile.PillFriendly1 -> ImageTile.PillFriendly1
            TypeTile.PillFriendly2 -> ImageTile.PillFriendly2
            TypeTile.PillFriendly3 -> ImageTile.PillFriendly3
            TypeTile.PillFriendly4 -> ImageTile.PillFriendly4
            TypeTile.PillFriendly5 -> ImageTile.PillFriendly5
            TypeTile.PillFriendly6 -> ImageTile.PillFriendly6
            TypeTile.PillFriendly7 -> ImageTile.PillFriendly7
            TypeTile.PillFriendly8 -> ImageTile.PillFriendly8
            TypeTile.PillFriendly9 -> ImageTile.PillFriendly9
            TypeTile.PillFriendly10 -> ImageTile.PillFriendly10
            TypeTile.PillFriendly11 -> ImageTile.PillFriendly11
            TypeTile.PillFriendly12 -> ImageTile.PillFriendly12
            TypeTile.PillFriendly13 -> ImageTile.PillFriendly13
            TypeTile.PillFriendly14 -> ImageTile.PillFriendly14
            TypeTile.PillFriendly15 -> ImageTile.PillFriendly15
            TypeTile.BaseHostile -> ImageTile.BaseHostile
            TypeTile.PillHostile0 -> ImageTile.PillHostile0
            TypeTile.PillHostile1 -> ImageTile.PillHostile1
            TypeTile.PillHostile2 -> ImageTile.PillHostile2
            TypeTile.PillHostile3 -> ImageTile.PillHostile3
            TypeTile.PillHostile4 -> ImageTile.PillHostile4
            TypeTile.PillHostile5 -> ImageTile.PillHostile5
            TypeTile.PillHostile6 -> ImageTile.PillHostile6
            TypeTile.PillHostile7 -> ImageTile.PillHostile7
            TypeTile.PillHostile8 -> ImageTile.PillHostile8
            TypeTile.PillHostile9 -> ImageTile.PillHostile9
            TypeTile.PillHostile10 -> ImageTile.PillHostile10
            TypeTile.PillHostile11 -> ImageTile.PillHostile11
            TypeTile.PillHostile12 -> ImageTile.PillHostile12
            TypeTile.PillHostile13 -> ImageTile.PillHostile13
            TypeTile.PillHostile14 -> ImageTile.PillHostile14
            TypeTile.PillHostile15 -> ImageTile.PillHostile15
            TypeTile.BaseNeutral -> ImageTile.BaseNeutral
        }
    }
}
