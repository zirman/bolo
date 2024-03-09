package bmap

fun getDamageLevel(terrain: TerrainTile): Int =
    when (terrain) {
        TerrainTile.Swamp0,
        TerrainTile.Rubble0,
        TerrainTile.Grass0,
        TerrainTile.WallDamaged0,
        -> 3

        TerrainTile.Swamp1,
        TerrainTile.Rubble1,
        TerrainTile.Grass1,
        TerrainTile.WallDamaged1,
        -> 2

        TerrainTile.Swamp2,
        TerrainTile.Rubble2,
        TerrainTile.Grass2,
        TerrainTile.WallDamaged2,
        -> 1

        else -> 0
    }

fun terrainToNibble(t: TerrainTile): Int =
    when (t) {
        TerrainTile.Wall -> 0
        TerrainTile.River -> 1
        TerrainTile.Swamp0,
        TerrainTile.Swamp1,
        TerrainTile.Swamp2,
        TerrainTile.Swamp3,
        -> 2

        TerrainTile.Crater -> 3
        TerrainTile.Road -> 4
        TerrainTile.Tree -> 5
        TerrainTile.Rubble0,
        TerrainTile.Rubble1,
        TerrainTile.Rubble2,
        TerrainTile.Rubble3,
        -> 6

        TerrainTile.Grass0,
        TerrainTile.Grass1,
        TerrainTile.Grass2,
        TerrainTile.Grass3,
        -> 7

        TerrainTile.WallDamaged0,
        TerrainTile.WallDamaged1,
        TerrainTile.WallDamaged2,
        TerrainTile.WallDamaged3,
        -> 8

        TerrainTile.Boat -> 9
        TerrainTile.SwampMined -> 10
        TerrainTile.CraterMined -> 11
        TerrainTile.RoadMined -> 12
        TerrainTile.TreeMined -> 13
        TerrainTile.RubbleMined -> 14
        TerrainTile.GrassMined -> 15
        else -> throw IllegalStateException()
    }

fun MutableList<UByte>.writeBmap(bmap: Bmap): MutableList<UByte> {
    fun writeString(string: String) {
        for (uByte in string.encodeToByteArray().toUByteArray()) {
            add(uByte)
        }
    }

    fun writeUByte(uByte: UByte) {
        add(uByte)
    }

    fun writeBuffer(buffer: List<UByte>) {
        for (uByte in buffer) {
            add(uByte)
        }
    }

    fun <T> writeMulti(xs: Iterable<T>, write: (x: T) -> Unit) {
        for (x in xs) {
            write(x)
        }
    }

    fun writePill(pill: Pill) {
        writeUByte(pill.col.toUByte())
        writeUByte(pill.row.toUByte())
        writeUByte(pill.owner.toUByte())
        writeUByte(pill.armor.toUByte())
        writeUByte(pill.speed.toUByte())
    }

    fun writeBase(base: Base) {
        writeUByte(base.col.toUByte())
        writeUByte(base.row.toUByte())
        writeUByte(base.owner.toUByte())
        writeUByte(base.armor.toUByte())
        writeUByte(base.shells.toUByte())
        writeUByte(base.mines.toUByte())
    }

    fun writeStart(start: StartInfo) {
        writeUByte(start.col.toUByte())
        writeUByte(start.row.toUByte())
        writeUByte(start.direction.toUByte())
    }

    writeString("BMAPBOLO")
    writeUByte(1.toUByte())
    writeUByte(bmap.pills.count().toUByte())
    writeUByte(bmap.bases.count().toUByte())
    writeUByte(bmap.starts.count().toUByte())
    writeMulti(bmap.pills.asIterable()) { writePill(it) }
    writeMulti(bmap.bases.asIterable()) { writeBase(it) }
    writeMulti(bmap.starts.asIterable()) { writeStart(it) }

    for (row in BORDER_WIDTH..<WORLD_HEIGHT - BORDER_WIDTH) {
        var col: Int = BORDER_WIDTH

        while (col < WORLD_WIDTH - BORDER_WIDTH) {
            // find run
            while (col < WORLD_WIDTH - BORDER_WIDTH) {
                if (bmap[col, row] != defaultTerrain(col, row)) {
                    val startCol = col
                    val nibbleWriter = NibbleWriter()

                    do {
                        val beginX = col

                        if (bmap[col, row] == bmap[col + 1, row]) { // sequence of same terrain
                            val t = bmap[col, row]

                            col += 2
                            while (col - beginX < 9 && col < WORLD_WIDTH - BORDER_WIDTH && bmap[col, row] == t) {
                                col++
                            }

                            nibbleWriter.writeNibble((col - beginX) + 6)
                            nibbleWriter.writeNibble(terrainToNibble(t))
                        } else { // sequence of different terrain
                            col++
                            while (col - beginX < 8 &&
                                col < WORLD_WIDTH - BORDER_WIDTH &&
                                bmap[col, row] != bmap[col - 1, row] &&
                                bmap[col, row] != defaultTerrain(col, row)
                            ) {
                                col++
                            }

                            nibbleWriter.writeNibble((col - beginX) - 1)

                            for (i in beginX..<col) {
                                nibbleWriter.writeNibble(terrainToNibble(bmap[i, row]))
                            }
                        }
                    } while (col < WORLD_WIDTH - BORDER_WIDTH && bmap[col, row] != defaultTerrain(col, row))

                    val buf = nibbleWriter.finish()
                    writeUByte((buf.size + 4).toUByte())
                    writeUByte(row.toUByte())
                    writeUByte(startCol.toUByte())
                    writeUByte(col.toUByte())
                    writeBuffer(buf)
                }

                col++
            }
        }
    }

    writeUByte(4.toUByte())
    writeUByte(0xff.toUByte())
    writeUByte(0xff.toUByte())
    writeUByte(0xff.toUByte())
    return this
}

fun MutableList<UByte>.writeDamage(bmap: Bmap): MutableList<UByte> {
    fun writeString(string: String) {
        for (uByte in string.encodeToByteArray().toUByteArray()) {
            add(uByte)
        }
    }

    fun writeUByte(uByte: UByte) {
        add(uByte)
    }

    fun writeBuffer(buffer: List<UByte>) {
        for (uByte in buffer) {
            add(uByte)
        }
    }

    writeString("BMAPDAMG")
    writeUByte(1.toUByte())

    for (row in BORDER_WIDTH..<WORLD_HEIGHT - BORDER_WIDTH) {
        var col: Int = BORDER_WIDTH

        while (col < WORLD_WIDTH - BORDER_WIDTH) {
            // find run
            while (col < WORLD_WIDTH - BORDER_WIDTH) {
                if (getDamageLevel(bmap[col, row]) != 0) {
                    val startCol = col
                    val nibbleWriter = NibbleWriter()

                    do {
                        val beginX = col

                        // sequence of same damage
                        if (getDamageLevel(bmap[col, row]) == getDamageLevel(bmap[col + 1, row])) {
                            val t = getDamageLevel(bmap[col, row])

                            col += 2
                            while (col - beginX < 9 && col < WORLD_WIDTH - BORDER_WIDTH && getDamageLevel(bmap[col, row]) == t) {
                                col++
                            }

                            nibbleWriter.writeNibble((col - beginX) + 6)
                            nibbleWriter.writeNibble(t)
                        } else { // sequence of different terrain
                            col++
                            while (
                                col - beginX < 8 &&
                                col < WORLD_WIDTH - BORDER_WIDTH &&
                                getDamageLevel(bmap[col, row]) != getDamageLevel(bmap[col - 1, row]) &&
                                getDamageLevel(bmap[col, row]) != 0
                            ) {
                                col++
                            }

                            nibbleWriter.writeNibble((col - beginX) - 1)

                            for (i in beginX..<col) {
                                nibbleWriter.writeNibble(getDamageLevel(bmap[i, row]))
                            }
                        }
                    } while (col < WORLD_WIDTH - BORDER_WIDTH && getDamageLevel(bmap[col, row]) != 0)

                    val buf = nibbleWriter.finish()
                    writeUByte((buf.size + 4).toUByte())
                    writeUByte(row.toUByte())
                    writeUByte(startCol.toUByte())
                    writeUByte(col.toUByte())
                    writeBuffer(buf)
                }

                col++
            }
        }
    }

    writeUByte(4.toUByte())
    writeUByte(0xff.toUByte())
    writeUByte(0xff.toUByte())
    writeUByte(0xff.toUByte())
    return this
}

fun MutableList<UByte>.writeBmapCode(bmapCode: BmapCode): MutableList<UByte> {
    fun writeString(string: String) {
        for (uByte in string.encodeToByteArray().toUByteArray()) {
            add(uByte)
        }
    }

    fun writeUByte(uByte: UByte) {
        add(uByte)
    }

    fun writeBuffer(buffer: List<UByte>) {
        for (uByte in buffer) {
            add(uByte)
        }
    }

    writeString("BMAPCODE")
    writeUByte(1.toUByte())

    for (row in BORDER_WIDTH..<WORLD_HEIGHT - BORDER_WIDTH) {
        var col: Int = BORDER_WIDTH

        while (col < WORLD_WIDTH - BORDER_WIDTH) {
            // find run
            while (col < WORLD_WIDTH - BORDER_WIDTH) {
                if (bmapCode[col, row] != 0) {
                    val startCol = col
                    val nibbleWriter = NibbleWriter()

                    do {
                        val beginX = col

                        if (bmapCode[col, row] == bmapCode[col + 1, row]) { // sequence of same code
                            val t = bmapCode[col, row]

                            col += 2
                            while (col - beginX < 9 && col < WORLD_WIDTH - BORDER_WIDTH && bmapCode[col, row] == t) {
                                col++
                            }

                            nibbleWriter.writeNibble((col - beginX) + 6)
                            nibbleWriter.writeNibble(t)
                        } else { // sequence of different terrain
                            col++
                            while (col - beginX < 8 &&
                                col < WORLD_WIDTH - BORDER_WIDTH &&
                                bmapCode[col, row] != bmapCode[col - 1, row] &&
                                bmapCode[col, row] != 0
                            ) {
                                col++
                            }

                            nibbleWriter.writeNibble((col - beginX) - 1)

                            for (i in beginX..<col) {
                                nibbleWriter.writeNibble(bmapCode[i, row])
                            }
                        }
                    } while (col < WORLD_WIDTH - BORDER_WIDTH && bmapCode[col, row] != 0)

                    val buf = nibbleWriter.finish()
                    writeUByte((buf.size + 4).toUByte())
                    writeUByte(row.toUByte())
                    writeUByte(startCol.toUByte())
                    writeUByte(col.toUByte())
                    writeBuffer(buf)
                }

                col++
            }
        }
    }

    writeUByte(4.toUByte())
    writeUByte(0xff.toUByte())
    writeUByte(0xff.toUByte())
    writeUByte(0xff.toUByte())
    return this
}

class NibbleWriter {
    private val buffer: MutableList<UByte> = mutableListOf()
    private var nibbled: Boolean = false
    private var nibble: Int = 0

    fun writeNibble(nib: Int) {
        if (nibbled) {
            buffer.add(nibble.shl(4).or(nib).toUByte())
            nibbled = false
        } else {
            nibble = nib
            nibbled = true
        }
    }

    fun finish(): List<UByte> {
        if (nibbled) {
            writeNibble(0)
        }

        return buffer
    }
}

//fun writeBmapExtra(
//    owner: Int,
//    pills: Array<bmap.Pill>,
//    buffer: MutableList<UByte>,
//) {
//    fun writeString(str: String) {
//        for (c in str.encodeToByteArray().toUByteArray()) {
//            buffer.add(c)
//        }
//    }
//
//    fun writeUByte(n: UByte) {
//        buffer.add(n)
//    }
//
//    fun writeInt(n: Int) {
//        buffer.add(n)
//    }
//
//    fun writeBoolean(n: Boolean) {
//        buffer.add((if (n) 1 else 0).toUByte())
//    }
//
//    writeString("BMAPEXTR")
//    writeUByte(1.toUByte())
//    writeUByte(owner.toUByte())
//
////    for (pill in pills) {
////        writeInt(pill.code)
////        writeBoolean(pill.isPlaced)
////    }
////
////    for (bases in bases) {
////        writeInt(pill.code.toUByte())
////        writeBoolean(pill.isPlaced)
////    }
//}
