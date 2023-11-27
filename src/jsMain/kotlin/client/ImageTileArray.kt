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
    Tree,
    Rubble,
    Grass,
    DamagedWall,
    SeaMined,
    SwampMined,
    CraterMined,
    RoadMined,
    TreeMined,
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

class ImageTileArray(private val bmap: Bmap, private val owner: Owner) {
    private val tiles: Uint8Array = Uint8Array(worldWidth * worldHeight)
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

    fun update(x: Int, y: Int) {
        run {
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

            if (x >= border && x < worldWidth - border && y >= border && y < worldHeight - border) {
                tiles[ind(x, y)] = bmap[x, y].toTypeTile().ordinal.toByte()
            }
        }

        for (yi in y - 1..y + 1) {
            for (xi in x - 1..x + 1) {
                imageTiles[ind(xi, yi)] = mapImage(xi, yi).index.toByte()
            }
        }
    }


    private fun mapImage(x: Int, y: Int): ImageTile {
        return when (getTypeTile(x, y)) {
            TypeTile.Sea -> when (isLikeBits(x, y) { isSeaLikeTile() }) {
                0, 5, 10, 15 -> ImageTile.Sea0
                4, 14 -> ImageTile.Sea1
                1, 11 -> ImageTile.Sea2
                8, 13 -> ImageTile.Sea3
                12 -> ImageTile.Sea4
                9 -> ImageTile.Sea5
                2, 7 -> ImageTile.Sea6
                6 -> ImageTile.Sea7
                3 -> ImageTile.Sea8
                else -> never()
            }

            TypeTile.SeaMined -> ImageTile.SeaMined
            TypeTile.Swamp -> ImageTile.Swamp
            TypeTile.SwampMined -> ImageTile.SwampMined
            TypeTile.River -> when (isLikeBits(x, y) { isWaterLikeToWaterTile() }) {
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
                else -> never()
            }

            TypeTile.Grass -> ImageTile.Grass
            TypeTile.GrassMined -> ImageTile.GrassMined
            TypeTile.Tree -> when (isLikeBits(x, y) { isTreeLikeTile() }) {
                0 -> ImageTile.Tree0
                4 -> ImageTile.Tree1
                1 -> ImageTile.Tree2
                8 -> ImageTile.Tree3
                12 -> ImageTile.Tree4
                9 -> ImageTile.Tree5
                2 -> ImageTile.Tree6
                6 -> ImageTile.Tree7
                3 -> ImageTile.Tree8
                5, 7, 10, 11, 13, 14, 15 -> ImageTile.Tree9
                else -> never()
            }

            TypeTile.TreeMined -> when (isLikeBits(x, y) { isTreeLikeTile() }) {
                0 -> ImageTile.TreeMined0
                4 -> ImageTile.TreeMined1
                1 -> ImageTile.TreeMined2
                8 -> ImageTile.TreeMined3
                12 -> ImageTile.TreeMined4
                9 -> ImageTile.TreeMined5
                2 -> ImageTile.TreeMined6
                6 -> ImageTile.TreeMined7
                3 -> ImageTile.TreeMined8
                5, 7, 10, 11, 13, 14, 15 -> ImageTile.TreeMined9
                else -> never()
            }

            TypeTile.Crater -> when (isLikeBits(x, y) { isCraterLikeTile() }) {
                0 -> ImageTile.Crater0
                4 -> ImageTile.Crater1
                5 -> ImageTile.Crater2
                1 -> ImageTile.Crater3
                8 -> ImageTile.Crater4
                12 -> ImageTile.Crater5
                13 -> ImageTile.Crater6
                9 -> ImageTile.Crater7
                10 -> ImageTile.Crater8
                14 -> ImageTile.Crater9
                15 -> ImageTile.Crater10
                11 -> ImageTile.Crater11
                2 -> ImageTile.Crater12
                6 -> ImageTile.Crater13
                7 -> ImageTile.Crater14
                3 -> ImageTile.Crater15
                else -> never()
            }

            TypeTile.CraterMined -> when (isLikeBits(x, y) { isCraterLikeTile() }) {
                0 -> ImageTile.CraterMined0
                4 -> ImageTile.CraterMined1
                5 -> ImageTile.CraterMined2
                1 -> ImageTile.CraterMined3
                8 -> ImageTile.CraterMined4
                12 -> ImageTile.CraterMined5
                13 -> ImageTile.CraterMined6
                9 -> ImageTile.CraterMined7
                10 -> ImageTile.CraterMined8
                14 -> ImageTile.CraterMined9
                15 -> ImageTile.CraterMined10
                11 -> ImageTile.CraterMined11
                2 -> ImageTile.CraterMined12
                6 -> ImageTile.CraterMined13
                7 -> ImageTile.CraterMined14
                3 -> ImageTile.CraterMined15
                else -> never()
            }

            TypeTile.Road -> {
                when (isLikeBits(x, y) { isRoadLikeTile() }) {
                    0 -> when (isLikeBits(x, y) { isWaterLikeToLandTile() }) {
                        15 -> ImageTile.Road30
                        5 -> ImageTile.Road23
                        10 -> ImageTile.Road21
                        else -> ImageTile.Road10
                    }

                    1, 4, 5 -> when (
                        getTypeTile(x, y - 1).isWaterLikeToLandTile()
                            .or(getTypeTile(x, y + 1).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.Road21
                        else -> ImageTile.Road1
                    }

                    2, 8, 10 -> when (
                        getTypeTile(x - 1, y).isWaterLikeToLandTile()
                            .or(getTypeTile(x + 1, y).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.Road23
                        else -> ImageTile.Road3
                    }

                    6 -> when (
                        getTypeTile(x - 1, y).isWaterLikeToLandTile()
                            .or(getTypeTile(x, y + 1).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.Road24
                        else -> when (getTypeTile(x + 1, y - 1).isRoadLikeTile()) {
                            1 -> ImageTile.Road12
                            else -> ImageTile.Road4
                        }
                    }

                    3 -> when (
                        getTypeTile(x + 1, y).isWaterLikeToLandTile()
                            .or(getTypeTile(x, y + 1).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.Road25
                        else -> when (getTypeTile(x - 1, y - 1).isRoadLikeTile()) {
                            1 -> ImageTile.Road14
                            else -> ImageTile.Road5
                        }
                    }

                    7 -> when (getTypeTile(x, y + 1).isWaterLikeToLandTile()) {
                        1 -> ImageTile.Road29
                        else -> when (
                            getTypeTile(x - 1, y - 1).isRoadLikeTile()
                                .or(getTypeTile(x + 1, y - 1).isRoadLikeTile().shl(1))
                        ) {
                            0 -> ImageTile.Road18
                            else -> ImageTile.Road13
                        }
                    }

                    12 -> when (
                        getTypeTile(x - 1, y).isWaterLikeToLandTile()
                            .or(getTypeTile(x, y - 1).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.Road20
                        else -> when (getTypeTile(x + 1, y + 1).isRoadLikeTile()) {
                            1 -> ImageTile.Road6
                            else -> ImageTile.Road0
                        }
                    }

                    14 -> when (getTypeTile(x - 1, y).isWaterLikeToLandTile()) {
                        1 -> ImageTile.Road27
                        else -> when (
                            getTypeTile(x + 1, y - 1).isRoadLikeTile()
                                .or(getTypeTile(x + 1, y + 1).isRoadLikeTile().shl(1))
                        ) {
                            0 -> ImageTile.Road15
                            else -> ImageTile.Road9
                        }
                    }

                    9 -> when (
                        getTypeTile(x, y - 1).isWaterLikeToLandTile()
                            .or(getTypeTile(x + 1, y).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.Road22
                        else -> when (getTypeTile(x - 1, y + 1).isRoadLikeTile()) {
                            1 -> ImageTile.Road8
                            else -> ImageTile.Road2
                        }
                    }

                    13 -> when (getTypeTile(x, y - 1).isWaterLikeToLandTile()) {
                        1 -> ImageTile.Road26
                        else -> when (
                            getTypeTile(x - 1, y + 1).isRoadLikeTile()
                                .or(getTypeTile(x + 1, y + 1).isRoadLikeTile().shl(1))
                        ) {
                            0 -> ImageTile.Road17
                            else -> ImageTile.Road7
                        }
                    }

                    11 -> when (getTypeTile(x + 1, y).isWaterLikeToLandTile()) {
                        1 -> ImageTile.Road28
                        else -> when (
                            getTypeTile(x - 1, y - 1).isRoadLikeTile()
                                .or(getTypeTile(x - 1, y + 1).isRoadLikeTile().shl(1))
                        ) {
                            0 -> ImageTile.Road16
                            else -> ImageTile.Road11
                        }
                    }

                    15 -> when (isLikeBits(x, y) { isRoadLikeTile() }) {
                        0 -> ImageTile.Road19
                        else -> ImageTile.Road10
                    }

                    else -> never()
                }
            }

            TypeTile.RoadMined -> ImageTile.RoadMined
            TypeTile.Rubble -> ImageTile.Rubble
            TypeTile.RubbleMined -> ImageTile.RubbleMined
            TypeTile.DamagedWall -> ImageTile.DamagedWall
            TypeTile.Wall -> when (isLikeBits(x, y) { isWallLikeTile() }) {
                0 -> ImageTile.Wall0
                4 -> ImageTile.Wall1
                2 -> ImageTile.Wall12
                6 ->
                    when (getTypeTile(x + 1, y - 1).isWallLikeTile()) {
                        1 -> ImageTile.Wall13
                        else -> ImageTile.Wall28
                    }

                5 -> ImageTile.Wall2
                1 -> ImageTile.Wall3
                3 ->
                    when (getTypeTile(x - 1, y - 1).isWallLikeTile()) {
                        1 -> ImageTile.Wall15
                        else -> ImageTile.Wall29
                    }

                7 -> when (
                    getTypeTile(x - 1, y - 1).isWallLikeTile()
                        .or(getTypeTile(x + 1, y - 1).isWallLikeTile().shl(1))
                ) {
                    3 -> ImageTile.Wall14
                    0 -> ImageTile.Wall23
                    2 -> ImageTile.Wall33
                    1 -> ImageTile.Wall40
                    else -> never()
                }

                8 -> ImageTile.Wall4
                12 -> when (getTypeTile(x + 1, y + 1).isWallLikeTile()) {
                    1 -> ImageTile.Wall5
                    else -> ImageTile.Wall24
                }

                10 -> ImageTile.Wall8
                14 -> when (
                    getTypeTile(x + 1, y - 1).isWallLikeTile()
                        .or(getTypeTile(x + 1, y + 1).isWallLikeTile().shl(1))
                ) {
                    3 -> ImageTile.Wall9
                    0 -> ImageTile.Wall22
                    2 -> ImageTile.Wall32
                    1 -> ImageTile.Wall36
                    else -> never()
                }

                9 -> when (getTypeTile(x - 1, y + 1).isWallLikeTile()) {
                    1 -> ImageTile.Wall7
                    else -> ImageTile.Wall25
                }

                13 -> when (
                    getTypeTile(x - 1, y + 1).isWallLikeTile()
                        .or(getTypeTile(x + 1, y + 1).isWallLikeTile().shl(1))
                ) {
                    3 -> ImageTile.Wall6
                    0 -> ImageTile.Wall18
                    2 -> ImageTile.Wall37
                    1 -> ImageTile.Wall44
                    else -> never()
                }

                11 -> when (
                    getTypeTile(x - 1, y - 1).isWallLikeTile()
                        .or(getTypeTile(x - 1, y + 1).isWallLikeTile().shl(1))
                ) {
                    3 -> ImageTile.Wall11
                    0 -> ImageTile.Wall19
                    2 -> ImageTile.Wall41
                    1 -> ImageTile.Wall45
                    else -> never()
                }

                15 -> when (
                    getTypeTile(x - 1, y - 1).isWallLikeTile()
                        .or(getTypeTile(x + 1, y - 1).isWallLikeTile().shl(1))
                        .or(getTypeTile(x - 1, y + 1).isWallLikeTile().shl(2))
                        .or(getTypeTile(x + 1, y + 1).isWallLikeTile().shl(3))
                ) {
                    15 -> ImageTile.Wall10
                    7 -> ImageTile.Wall16
                    11 -> ImageTile.Wall17
                    13 -> ImageTile.Wall20
                    14 -> ImageTile.Wall21
                    1 -> ImageTile.Wall26
                    2 -> ImageTile.Wall27
                    4 -> ImageTile.Wall30
                    8 -> ImageTile.Wall31
                    6 -> ImageTile.Wall34
                    9 -> ImageTile.Wall35
                    10 -> ImageTile.Wall38
                    5 -> ImageTile.Wall39
                    12 -> ImageTile.Wall42
                    3 -> ImageTile.Wall43
                    0 -> ImageTile.Wall46
                    else -> never()
                }

                else -> never()
            }

            TypeTile.Boat -> when (isLikeBits(x, y) { isWaterLikeToLandTile() }) {
                0, 6, 15 -> ImageTile.Boat0
                2, 7, 10 -> ImageTile.Boat1
                3 -> ImageTile.Boat2
                1, 11 -> ImageTile.Boat3
                9 -> ImageTile.Boat4
                8, 13 -> ImageTile.Boat5
                12 -> ImageTile.Boat6
                4, 5, 14 -> ImageTile.Boat7
                else -> never()
            }

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

    private inline fun isLikeBits(x: Int, y: Int, f: TypeTile.() -> Int): Int {
        return getTypeTile(x - 1, y).f()
            .or(getTypeTile(x, y - 1).f().shl(1))
            .or(getTypeTile(x + 1, y).f().shl(2))
            .or(getTypeTile(x, y + 1).f().shl(3))
    }
}

fun never(): Nothing {
    throw IllegalStateException("Impossible")
}
