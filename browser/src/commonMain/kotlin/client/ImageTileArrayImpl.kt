package client

import adapters.Uint8ArrayAdapter
import assert.never
import bmap.Bmap
import bmap.TypeTile
import bmap.BORDER
import bmap.ind
import bmap.isCraterLikeTile
import bmap.isRoadLikeTile
import bmap.isSeaLikeTile
import bmap.isTreeLikeTile
import bmap.isWallLikeTile
import bmap.isWaterLikeToLandTile
import bmap.isWaterLikeToWaterTile
import bmap.toTypeTile
import bmap.WORLD_HEIGHT
import bmap.WORLD_WIDTH
import frame.Owner

class ImageTileArrayImpl(
    private val bmap: Bmap,
    private val owner: Owner,
    private val imageTiles: Uint8ArrayAdapter,
) : ImageTileArray {
    override fun getTypeTile(col: Int, row: Int): TypeTile =
        if (col < 0 || col >= WORLD_WIDTH || row < 0 || row >= WORLD_HEIGHT) TypeTile.SeaMined
        else TypeTile.entries[tiles[ind(col, row)].toInt()]

    override fun update(col: Int, row: Int) {
        run {
            for (pill in bmap.pills) {
                if (pill.isPlaced && pill.col == col && pill.row == row) {
                    tiles[ind(pill.col, pill.row)] = run {
                        if (pill.owner == owner.int) TypeTile.PillFriendly0 else TypeTile.PillHostile0
                    }
                        .ordinal
                        .let { it + pill.armor }
                        .toUByte()
                    return@run
                }
            }

            for (base in bmap.bases) {
                if (base.col == col && base.row == row) {
                    tiles[ind(base.col, base.row)] = when (base.owner) {
                        0xff -> TypeTile.BaseNeutral
                        owner.int -> TypeTile.BaseFriendly
                        else -> TypeTile.BaseHostile
                    }.ordinal.toUByte()
                    return@run
                }
            }

            if (col >= BORDER && col < WORLD_WIDTH - BORDER && row >= BORDER && row < WORLD_HEIGHT - BORDER) {
                tiles[ind(col, row)] = bmap[col, row].toTypeTile().ordinal.toUByte()
            }
        }

        for (yi in row - 1..row + 1) {
            for (xi in col - 1..col + 1) {
                imageTiles[ind(xi, yi)] = mapImage(xi, yi).index.toUByte()
            }
        }
    }

    override val uint8Array: Any get() = imageTiles.uint8Array

    private val tiles: UByteArray = UByteArray(WORLD_WIDTH * WORLD_HEIGHT)
        .also { tiles ->
            for (row in 0..<WORLD_HEIGHT) {
                for (col in 0..<WORLD_WIDTH) {
                    tiles[ind(col, row)] = bmap[col, row].toTypeTile().ordinal.toUByte()
                }
            }

            for (base in bmap.bases) {
                tiles[ind(base.col, base.row)] =
                    when (base.owner) {
                        0xff -> TypeTile.BaseNeutral
                        owner.int -> TypeTile.BaseFriendly
                        else -> TypeTile.BaseHostile
                    }.ordinal.toUByte()
            }

            for (pill in bmap.pills) {
                if (pill.isPlaced) {
                    tiles[ind(pill.col, pill.row)] = (TypeTile.PillHostile0.ordinal + pill.armor).toUByte()
                }
            }
        }

    private fun mapImage(col: Int, row: Int): ImageTile =
        when (getTypeTile(col, row)) {
            TypeTile.Sea -> when (isLikeBits(col, row) { isSeaLikeTile() }) {
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
            TypeTile.River -> when (isLikeBits(col, row) { isWaterLikeToWaterTile() }) {
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
            TypeTile.Tree -> when (isLikeBits(col, row) { isTreeLikeTile() }) {
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

            TypeTile.TreeMined -> when (isLikeBits(col, row) { isTreeLikeTile() }) {
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

            TypeTile.Crater -> when (isLikeBits(col, row) { isCraterLikeTile() }) {
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

            TypeTile.CraterMined -> when (isLikeBits(col, row) { isCraterLikeTile() }) {
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
                when (isLikeBits(col, row) { isRoadLikeTile() }) {
                    0 -> when (isLikeBits(col, row) { isWaterLikeToLandTile() }) {
                        5 -> ImageTile.Road10
                        15 -> ImageTile.Road28
                        10 -> ImageTile.Road29
                        else -> ImageTile.Road7
                    }

                    1, 4, 5 -> when (
                        getTypeTile(col, row - 1).isWaterLikeToLandTile()
                            .or(getTypeTile(col, row + 1).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.Road29
                        else -> ImageTile.Road23
                    }

                    2, 8, 10 -> when (
                        getTypeTile(col - 1, row).isWaterLikeToLandTile()
                            .or(getTypeTile(col + 1, row).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.Road10
                        else -> ImageTile.Road22
                    }

                    6 -> when (
                        getTypeTile(col - 1, row).isWaterLikeToLandTile()
                            .or(getTypeTile(col, row + 1).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.Road15
                        else -> when (getTypeTile(col + 1, row - 1).isRoadLikeTile()) {
                            1 -> ImageTile.Road12
                            else -> ImageTile.Road24
                        }
                    }

                    3 -> when (
                        getTypeTile(col + 1, row).isWaterLikeToLandTile()
                            .or(getTypeTile(col, row + 1).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.Road17
                        else -> when (getTypeTile(col - 1, row - 1).isRoadLikeTile()) {
                            1 -> ImageTile.Road14
                            else -> ImageTile.Road25
                        }
                    }

                    7 -> when (getTypeTile(col, row + 1).isWaterLikeToLandTile()) {
                        1 -> ImageTile.Road16
                        else -> when (
                            getTypeTile(col - 1, row - 1).isRoadLikeTile()
                                .or(getTypeTile(col + 1, row - 1).isRoadLikeTile().shl(1))
                        ) {
                            0 -> ImageTile.Road27
                            else -> ImageTile.Road13
                        }
                    }

                    12 -> when (
                        getTypeTile(col - 1, row).isWaterLikeToLandTile()
                            .or(getTypeTile(col, row - 1).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.Road3
                        else -> when (getTypeTile(col + 1, row + 1).isRoadLikeTile()) {
                            1 -> ImageTile.Road0
                            else -> ImageTile.Road18
                        }
                    }

                    14 -> when (getTypeTile(col - 1, row).isWaterLikeToLandTile()) {
                        1 -> ImageTile.Road9
                        else -> when (
                            getTypeTile(col + 1, row - 1).isRoadLikeTile()
                                .or(getTypeTile(col + 1, row + 1).isRoadLikeTile().shl(1))
                        ) {
                            0 -> ImageTile.Road26
                            else -> ImageTile.Road6
                        }
                    }

                    9 -> when (
                        getTypeTile(col, row - 1).isWaterLikeToLandTile()
                            .or(getTypeTile(col + 1, row).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.Road5
                        else -> when (getTypeTile(col - 1, row + 1).isRoadLikeTile()) {
                            1 -> ImageTile.Road2
                            else -> ImageTile.Road19
                        }
                    }

                    13 -> when (getTypeTile(col, row - 1).isWaterLikeToLandTile()) {
                        1 -> ImageTile.Road4
                        else -> when (
                            getTypeTile(col - 1, row + 1).isRoadLikeTile()
                                .or(getTypeTile(col + 1, row + 1).isRoadLikeTile().shl(1))
                        ) {
                            0 -> ImageTile.Road20
                            else -> ImageTile.Road1
                        }
                    }

                    11 -> when (getTypeTile(col + 1, row).isWaterLikeToLandTile()) {
                        1 -> ImageTile.Road11
                        else -> when (
                            getTypeTile(col - 1, row - 1).isRoadLikeTile()
                                .or(getTypeTile(col - 1, row + 1).isRoadLikeTile().shl(1))
                        ) {
                            0 -> ImageTile.Road21
                            else -> ImageTile.Road8
                        }
                    }

                    15 -> when (
                        getTypeTile(col - 1, row - 1).isRoadLikeTile()
                            .or(getTypeTile(col + 1, row - 1).isRoadLikeTile().shl(1))
                            .or(getTypeTile(col - 1, row + 1).isRoadLikeTile().shl(2))
                            .or(getTypeTile(col + 1, row + 1).isRoadLikeTile().shl(3))
                    ) {
                        0 -> ImageTile.Road30
                        else -> ImageTile.Road7
                    }

                    else -> never()
                }
            }

            TypeTile.RoadMined -> {
                when (isLikeBits(col, row) { isRoadLikeTile() }) {
                    0 -> when (isLikeBits(col, row) { isWaterLikeToLandTile() }) {
                        5 -> ImageTile.RoadMined10
                        15 -> ImageTile.RoadMined28
                        10 -> ImageTile.RoadMined29
                        else -> ImageTile.RoadMined7
                    }

                    1, 4, 5 -> when (
                        getTypeTile(col, row - 1).isWaterLikeToLandTile()
                            .or(getTypeTile(col, row + 1).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.RoadMined29
                        else -> ImageTile.RoadMined23
                    }

                    2, 8, 10 -> when (
                        getTypeTile(col - 1, row).isWaterLikeToLandTile()
                            .or(getTypeTile(col + 1, row).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.RoadMined10
                        else -> ImageTile.RoadMined22
                    }

                    6 -> when (
                        getTypeTile(col - 1, row).isWaterLikeToLandTile()
                            .or(getTypeTile(col, row + 1).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.RoadMined15
                        else -> when (getTypeTile(col + 1, row - 1).isRoadLikeTile()) {
                            1 -> ImageTile.RoadMined12
                            else -> ImageTile.RoadMined24
                        }
                    }

                    3 -> when (
                        getTypeTile(col + 1, row).isWaterLikeToLandTile()
                            .or(getTypeTile(col, row + 1).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.RoadMined17
                        else -> when (getTypeTile(col - 1, row - 1).isRoadLikeTile()) {
                            1 -> ImageTile.RoadMined14
                            else -> ImageTile.RoadMined25
                        }
                    }

                    7 -> when (getTypeTile(col, row + 1).isWaterLikeToLandTile()) {
                        1 -> ImageTile.RoadMined16
                        else -> when (
                            getTypeTile(col - 1, row - 1).isRoadLikeTile()
                                .or(getTypeTile(col + 1, row - 1).isRoadLikeTile().shl(1))
                        ) {
                            0 -> ImageTile.RoadMined27
                            else -> ImageTile.RoadMined13
                        }
                    }

                    12 -> when (
                        getTypeTile(col - 1, row).isWaterLikeToLandTile()
                            .or(getTypeTile(col, row - 1).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.RoadMined3
                        else -> when (getTypeTile(col + 1, row + 1).isRoadLikeTile()) {
                            1 -> ImageTile.RoadMined0
                            else -> ImageTile.RoadMined18
                        }
                    }

                    14 -> when (getTypeTile(col - 1, row).isWaterLikeToLandTile()) {
                        1 -> ImageTile.RoadMined9
                        else -> when (
                            getTypeTile(col + 1, row - 1).isRoadLikeTile()
                                .or(getTypeTile(col + 1, row + 1).isRoadLikeTile().shl(1))
                        ) {
                            0 -> ImageTile.RoadMined26
                            else -> ImageTile.RoadMined6
                        }
                    }

                    9 -> when (
                        getTypeTile(col, row - 1).isWaterLikeToLandTile()
                            .or(getTypeTile(col + 1, row).isWaterLikeToLandTile().shl(1))
                    ) {
                        3 -> ImageTile.RoadMined5
                        else -> when (getTypeTile(col - 1, row + 1).isRoadLikeTile()) {
                            1 -> ImageTile.RoadMined2
                            else -> ImageTile.RoadMined19
                        }
                    }

                    13 -> when (getTypeTile(col, row - 1).isWaterLikeToLandTile()) {
                        1 -> ImageTile.RoadMined4
                        else -> when (
                            getTypeTile(col - 1, row + 1).isRoadLikeTile()
                                .or(getTypeTile(col + 1, row + 1).isRoadLikeTile().shl(1))
                        ) {
                            0 -> ImageTile.RoadMined20
                            else -> ImageTile.RoadMined1
                        }
                    }

                    11 -> when (getTypeTile(col + 1, row).isWaterLikeToLandTile()) {
                        1 -> ImageTile.RoadMined11
                        else -> when (
                            getTypeTile(col - 1, row - 1).isRoadLikeTile()
                                .or(getTypeTile(col - 1, row + 1).isRoadLikeTile().shl(1))
                        ) {
                            0 -> ImageTile.RoadMined21
                            else -> ImageTile.RoadMined8
                        }
                    }

                    15 -> when (
                        getTypeTile(col - 1, row - 1).isRoadLikeTile()
                            .or(getTypeTile(col + 1, row - 1).isRoadLikeTile().shl(1))
                            .or(getTypeTile(col - 1, row + 1).isRoadLikeTile().shl(2))
                            .or(getTypeTile(col + 1, row + 1).isRoadLikeTile().shl(3))
                    ) {
                        0 -> ImageTile.RoadMined30
                        else -> ImageTile.RoadMined7
                    }

                    else -> never()
                }
            }

            TypeTile.Rubble -> ImageTile.Rubble
            TypeTile.RubbleMined -> ImageTile.RubbleMined
            TypeTile.DamagedWall -> ImageTile.DamagedWall
            TypeTile.Wall -> when (isLikeBits(col, row) { isWallLikeTile() }) {
                0 -> ImageTile.Wall0
                4 -> ImageTile.Wall1
                2 -> ImageTile.Wall12
                6 ->
                    when (getTypeTile(col + 1, row - 1).isWallLikeTile()) {
                        1 -> ImageTile.Wall13
                        else -> ImageTile.Wall28
                    }

                5 -> ImageTile.Wall2
                1 -> ImageTile.Wall3
                3 ->
                    when (getTypeTile(col - 1, row - 1).isWallLikeTile()) {
                        1 -> ImageTile.Wall15
                        else -> ImageTile.Wall29
                    }

                7 -> when (
                    getTypeTile(col - 1, row - 1).isWallLikeTile()
                        .or(getTypeTile(col + 1, row - 1).isWallLikeTile().shl(1))
                ) {
                    3 -> ImageTile.Wall14
                    0 -> ImageTile.Wall23
                    2 -> ImageTile.Wall33
                    1 -> ImageTile.Wall40
                    else -> never()
                }

                8 -> ImageTile.Wall4
                12 -> when (getTypeTile(col + 1, row + 1).isWallLikeTile()) {
                    1 -> ImageTile.Wall5
                    else -> ImageTile.Wall24
                }

                10 -> ImageTile.Wall8
                14 -> when (
                    getTypeTile(col + 1, row - 1).isWallLikeTile()
                        .or(getTypeTile(col + 1, row + 1).isWallLikeTile().shl(1))
                ) {
                    3 -> ImageTile.Wall9
                    0 -> ImageTile.Wall22
                    2 -> ImageTile.Wall32
                    1 -> ImageTile.Wall36
                    else -> never()
                }

                9 -> when (getTypeTile(col - 1, row + 1).isWallLikeTile()) {
                    1 -> ImageTile.Wall7
                    else -> ImageTile.Wall25
                }

                13 -> when (
                    getTypeTile(col - 1, row + 1).isWallLikeTile()
                        .or(getTypeTile(col + 1, row + 1).isWallLikeTile().shl(1))
                ) {
                    3 -> ImageTile.Wall6
                    0 -> ImageTile.Wall18
                    2 -> ImageTile.Wall37
                    1 -> ImageTile.Wall44
                    else -> never()
                }

                11 -> when (
                    getTypeTile(col - 1, row - 1).isWallLikeTile()
                        .or(getTypeTile(col - 1, row + 1).isWallLikeTile().shl(1))
                ) {
                    3 -> ImageTile.Wall11
                    0 -> ImageTile.Wall19
                    2 -> ImageTile.Wall41
                    1 -> ImageTile.Wall45
                    else -> never()
                }

                15 -> when (
                    getTypeTile(col - 1, row - 1).isWallLikeTile()
                        .or(getTypeTile(col + 1, row - 1).isWallLikeTile().shl(1))
                        .or(getTypeTile(col - 1, row + 1).isWallLikeTile().shl(2))
                        .or(getTypeTile(col + 1, row + 1).isWallLikeTile().shl(3))
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

            TypeTile.Boat -> when (isLikeBits(col, row) { isWaterLikeToLandTile() }) {
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

    private inline fun ImageTileArray.isLikeBits(col: Int, row: Int, f: TypeTile.() -> Int): Int {
        return getTypeTile(col - 1, row).f()
            .or(getTypeTile(col, row - 1).f().shl(1))
            .or(getTypeTile(col + 1, row).f().shl(2))
            .or(getTypeTile(col, row + 1).f().shl(3))
    }

    init {
        for (rowIndex in 0..<WORLD_HEIGHT) {
            for (columnIndex in 0..<WORLD_WIDTH) {
                imageTiles[ind(columnIndex, rowIndex)] = mapImage(columnIndex, rowIndex).index.toUByte()
            }
        }
    }
}
