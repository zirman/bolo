package client

import bmap.Bmap
import bmap.Terrain
import bmap.border
import bmap.ind
import bmap.worldHeight
import bmap.worldWidth
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

const val tileSheetWidth: Int = 16
const val tileSheetHeight: Int = 16
const val tilesCount: Int = tileSheetWidth * tileSheetHeight

fun tileInd(x: Int, y: Int): Int = (tileSheetWidth * y) + x

enum class Tile(val index: Int) {
    Sea(tileInd(x = 9, y = 4)),
    Boat(tileInd(x = 14, y = 10)),
    Wall(tileInd(x = 14, y = 2)),
    River(tileInd(x = 10, y = 2)),
    Swamp(tileInd(x = 2, y = 12)),
    Crater(tileInd(x = 2, y = 2)),
    Road(tileInd(x = 4, y = 5)),
    Forest(tileInd(x = 1, y = 12)),
    Rubble(tileInd(x = 3, y = 12)),
    Grass(tileInd(x = 0, y = 12)),
    DamagedWall(tileInd(x = 8, y = 12)),
    SeaMined(tileInd(x = 9, y = 7)),
    SwampMined(tileInd(x = 6, y = 12)),
    CraterMined(tileInd(x = 6, y = 2)),
    RoadMined(tileInd(x = 4, y = 8)),
    ForestMined(tileInd(x = 5, y = 12)),
    RubbleMined(tileInd(x = 7, y = 12)),
    GrassMined(tileInd(x = 4, y = 12)),
    BaseNeutral(tileInd(x = 0, y = 13)),
    BaseFriendly(tileInd(x = 1, y = 13)),
    BaseHostile(tileInd(x = 2, y = 13)),
    PillHostile0(tileInd(x = 0, y = 14)),
    PillHostile1(tileInd(x = 1, y = 14)),
    PillHostile2(tileInd(x = 2, y = 14)),
    PillHostile3(tileInd(x = 3, y = 14)),
    PillHostile4(tileInd(x = 4, y = 14)),
    PillHostile5(tileInd(x = 5, y = 14)),
    PillHostile6(tileInd(x = 6, y = 14)),
    PillHostile7(tileInd(x = 7, y = 14)),
    PillHostile8(tileInd(x = 8, y = 14)),
    PillHostile9(tileInd(x = 9, y = 14)),
    PillHostile10(tileInd(x = 10, y = 14)),
    PillHostile11(tileInd(x = 11, y = 14)),
    PillHostile12(tileInd(x = 12, y = 14)),
    PillHostile13(tileInd(x = 13, y = 14)),
    PillHostile14(tileInd(x = 14, y = 14)),
    PillHostile15(tileInd(x = 15, y = 14)),
    PillFriendly0(tileInd(x = 0, y = 15)),
    PillFriendly1(tileInd(x = 1, y = 15)),
    PillFriendly2(tileInd(x = 2, y = 15)),
    PillFriendly3(tileInd(x = 3, y = 15)),
    PillFriendly4(tileInd(x = 4, y = 15)),
    PillFriendly5(tileInd(x = 5, y = 15)),
    PillFriendly6(tileInd(x = 6, y = 15)),
    PillFriendly7(tileInd(x = 7, y = 15)),
    PillFriendly8(tileInd(x = 8, y = 15)),
    PillFriendly9(tileInd(x = 9, y = 15)),
    PillFriendly10(tileInd(x = 10, y = 15)),
    PillFriendly11(tileInd(x = 11, y = 15)),
    PillFriendly12(tileInd(x = 12, y = 15)),
    PillFriendly13(tileInd(x = 13, y = 15)),
    PillFriendly14(tileInd(x = 14, y = 15)),
    PillFriendly15(tileInd(x = 15, y = 15)),
}

fun Terrain.toTile(): Tile =
    when (this) {
        Terrain.Sea -> Tile.Sea
        Terrain.Boat -> Tile.Boat
        Terrain.Wall -> Tile.Wall
        Terrain.River -> Tile.River
        Terrain.Swamp0,
        Terrain.Swamp1,
        Terrain.Swamp2,
        Terrain.Swamp3,
        -> Tile.Swamp

        Terrain.Crater -> Tile.Crater
        Terrain.Road -> Tile.Road
        Terrain.Tree -> Tile.Forest
        Terrain.Rubble0,
        Terrain.Rubble1,
        Terrain.Rubble2,
        Terrain.Rubble3,
        -> Tile.Rubble

        Terrain.Grass0,
        Terrain.Grass1,
        Terrain.Grass2,
        Terrain.Grass3,
        -> Tile.Grass

        Terrain.WallDamaged0,
        Terrain.WallDamaged1,
        Terrain.WallDamaged2,
        Terrain.WallDamaged3,
        -> Tile.DamagedWall

        Terrain.SeaMined -> Tile.SeaMined
        Terrain.SwampMined -> Tile.SwampMined
        Terrain.CraterMined -> Tile.CraterMined
        Terrain.RoadMined -> Tile.RoadMined
        Terrain.ForestMined -> Tile.ForestMined
        Terrain.RubbleMined -> Tile.RubbleMined
        Terrain.GrassMined -> Tile.GrassMined
    }

class TileArray(private val bmap: Bmap, private val owner: Int) {
    val tiles: Uint8Array = Uint8Array(worldWidth * worldHeight)
        .also { tiles ->
            for (y in 0.until(worldHeight)) {
                for (x in 0.until(worldWidth)) {
                    tiles[ind(x, y)] = bmap[x, y].toTile().index.toByte()
                }
            }

            for (base in bmap.bases) {
                tiles[ind(base.x, base.y)] =
                    when (base.owner) {
                        0xff -> Tile.BaseNeutral
                        owner -> Tile.BaseFriendly
                        else -> Tile.BaseHostile
                    }
                        .index.toByte()
            }

            for (pill in bmap.pills) {
                if (pill.isPlaced) {
                    tiles[ind(pill.x, pill.y)] = (Tile.PillHostile0.index + pill.armor).toByte()
                }
            }
        }

    operator fun get(x: Int, y: Int): Tile =
        if (x < 0 || x >= worldWidth || y < 0 || y >= worldHeight) Tile.SeaMined
        else Tile.entries[tiles[ind(x, y)].toInt()]

    fun update(x: Int, y: Int) {
        for (pill in bmap.pills) {
            if (pill.isPlaced && pill.x == x && pill.y == y) {
                tiles[ind(pill.x, pill.y)] =
                    ((if (pill.owner == owner) Tile.PillFriendly0 else Tile.PillHostile0).index + pill.armor).toByte()
                return
            }
        }

        for (base in bmap.bases) {
            if (base.x == x && base.y == y) {
                tiles[ind(base.x, base.y)] =
                    when (base.owner) {
                        0xff -> Tile.BaseNeutral
                        owner -> Tile.BaseFriendly
                        else -> Tile.BaseHostile
                    }
                        .index.toByte()
                return
            }
        }

        if (x >= border && x < worldWidth - border && y >= border && y < worldHeight - border) {
            tiles[ind(x, y)] = bmap[x, y].toTile().index.toByte()
        }
    }
}
