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
        writeUByte(pill.x.toUByte())
        writeUByte(pill.y.toUByte())
        writeUByte(pill.owner.toUByte())
        writeUByte(pill.armor.toUByte())
        writeUByte(pill.speed.toUByte())
    }

    fun writeBase(base: Base) {
        writeUByte(base.x.toUByte())
        writeUByte(base.y.toUByte())
        writeUByte(base.owner.toUByte())
        writeUByte(base.armor.toUByte())
        writeUByte(base.shells.toUByte())
        writeUByte(base.mines.toUByte())
    }

    fun writeStart(start: StartInfo) {
        writeUByte(start.x.toUByte())
        writeUByte(start.y.toUByte())
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

    for (y in border..<worldHeight - border) {
        var x: Int = border

        while (x < worldWidth - border) {
            // find run
            while (x < worldWidth - border) {
                if (bmap[x, y] != defaultTerrain(x, y)) {
                    val startX = x
                    val nibbleWriter = NibbleWriter()

                    do {
                        val beginX = x

                        if (bmap[x, y] == bmap[x + 1, y]) { // sequence of same terrain
                            val t = bmap[x, y]

                            x += 2
                            while (x - beginX < 9 && x < worldWidth - border && bmap[x, y] == t) {
                                x++
                            }

                            nibbleWriter.writeNibble((x - beginX) + 6)
                            nibbleWriter.writeNibble(terrainToNibble(t))
                        } else { // sequence of different terrain
                            x++
                            while (x - beginX < 8 &&
                                x < worldWidth - border &&
                                bmap[x, y] != bmap[x - 1, y] &&
                                bmap[x, y] != defaultTerrain(x, y)
                            ) {
                                x++
                            }

                            nibbleWriter.writeNibble((x - beginX) - 1)

                            for (i in beginX..<x) {
                                nibbleWriter.writeNibble(terrainToNibble(bmap[i, y]))
                            }
                        }
                    } while (x < worldWidth - border && bmap[x, y] != defaultTerrain(x, y))

                    val buf = nibbleWriter.finish()
                    writeUByte((buf.size + 4).toUByte())
                    writeUByte(y.toUByte())
                    writeUByte(startX.toUByte())
                    writeUByte(x.toUByte())
                    writeBuffer(buf)
                }

                x++
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

    for (y in border..<worldHeight - border) {
        var x: Int = border

        while (x < worldWidth - border) {
            // find run
            while (x < worldWidth - border) {
                if (getDamageLevel(bmap[x, y]) != 0) {
                    val startX = x
                    val nibbleWriter = NibbleWriter()

                    do {
                        val beginX = x

                        // sequence of same damage
                        if (getDamageLevel(bmap[x, y]) == getDamageLevel(bmap[x + 1, y])) {
                            val t = getDamageLevel(bmap[x, y])

                            x += 2
                            while (x - beginX < 9 && x < worldWidth - border && getDamageLevel(bmap[x, y]) == t) {
                                x++
                            }

                            nibbleWriter.writeNibble((x - beginX) + 6)
                            nibbleWriter.writeNibble(t)
                        } else { // sequence of different terrain
                            x++
                            while (
                                x - beginX < 8 &&
                                x < worldWidth - border &&
                                getDamageLevel(bmap[x, y]) != getDamageLevel(bmap[x - 1, y]) &&
                                getDamageLevel(bmap[x, y]) != 0
                            ) {
                                x++
                            }

                            nibbleWriter.writeNibble((x - beginX) - 1)

                            for (i in beginX..<x) {
                                nibbleWriter.writeNibble(getDamageLevel(bmap[i, y]))
                            }
                        }
                    } while (x < worldWidth - border && getDamageLevel(bmap[x, y]) != 0)

                    val buf = nibbleWriter.finish()
                    writeUByte((buf.size + 4).toUByte())
                    writeUByte(y.toUByte())
                    writeUByte(startX.toUByte())
                    writeUByte(x.toUByte())
                    writeBuffer(buf)
                }

                x++
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

    for (y in border..<worldHeight - border) {
        var x: Int = border

        while (x < worldWidth - border) {
            // find run
            while (x < worldWidth - border) {
                if (bmapCode[x, y] != 0) {
                    val startX = x
                    val nibbleWriter = NibbleWriter()

                    do {
                        val beginX = x

                        if (bmapCode[x, y] == bmapCode[x + 1, y]) { // sequence of same code
                            val t = bmapCode[x, y]

                            x += 2
                            while (x - beginX < 9 && x < worldWidth - border && bmapCode[x, y] == t) {
                                x++
                            }

                            nibbleWriter.writeNibble((x - beginX) + 6)
                            nibbleWriter.writeNibble(t)
                        } else { // sequence of different terrain
                            x++
                            while (x - beginX < 8 &&
                                x < worldWidth - border &&
                                bmapCode[x, y] != bmapCode[x - 1, y] &&
                                bmapCode[x, y] != 0
                            ) {
                                x++
                            }

                            nibbleWriter.writeNibble((x - beginX) - 1)

                            for (i in beginX..<x) {
                                nibbleWriter.writeNibble(bmapCode[i, y])
                            }
                        }
                    } while (x < worldWidth - border && bmapCode[x, y] != 0)

                    val buf = nibbleWriter.finish()
                    writeUByte((buf.size + 4).toUByte())
                    writeUByte(y.toUByte())
                    writeUByte(startX.toUByte())
                    writeUByte(x.toUByte())
                    writeBuffer(buf)
                }

                x++
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