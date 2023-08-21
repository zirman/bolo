@file:OptIn(ExperimentalUnsignedTypes::class)

package bmap

fun getDamageLevel(terrain: Terrain): Int =
    when (terrain) {
        Terrain.Swamp0,
        Terrain.Rubble0,
        Terrain.Grass0,
        Terrain.WallDamaged0,
        -> 3

        Terrain.Swamp1,
        Terrain.Rubble1,
        Terrain.Grass1,
        Terrain.WallDamaged1,
        -> 2

        Terrain.Swamp2,
        Terrain.Rubble2,
        Terrain.Grass2,
        Terrain.WallDamaged2,
        -> 1

        else -> 0
    }

fun terrainToNibble(t: Terrain): Int =
    when (t) {
        Terrain.Wall -> 0
        Terrain.River -> 1
        Terrain.Swamp0,
        Terrain.Swamp1,
        Terrain.Swamp2,
        Terrain.Swamp3,
        -> 2

        Terrain.Crater -> 3
        Terrain.Road -> 4
        Terrain.Tree -> 5
        Terrain.Rubble0,
        Terrain.Rubble1,
        Terrain.Rubble2,
        Terrain.Rubble3,
        -> 6

        Terrain.Grass0,
        Terrain.Grass1,
        Terrain.Grass2,
        Terrain.Grass3,
        -> 7

        Terrain.WallDamaged0,
        Terrain.WallDamaged1,
        Terrain.WallDamaged2,
        Terrain.WallDamaged3,
        -> 8

        Terrain.Boat -> 9
        Terrain.SwampMined -> 10
        Terrain.CraterMined -> 11
        Terrain.RoadMined -> 12
        Terrain.ForestMined -> 13
        Terrain.RubbleMined -> 14
        Terrain.GrassMined -> 15
        else -> throw Exception()
    }

fun writeBmap(
    bmap: Bmap,
    buffer: MutableList<UByte>,
) {
    fun writeString(str: String) {
        for (c in str.encodeToByteArray().toUByteArray()) {
            buffer.add(c)
        }
    }

    fun writeUByte(n: UByte) {
        buffer.add(n)
    }

    fun writeBuffer(buf: List<UByte>) {
        for (byte in buf) {
            buffer.add(byte)
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
        writeUByte(start.dir.toUByte())
    }

    writeString("BMAPBOLO")
    writeUByte(1.toUByte())
    writeUByte(bmap.pills.count().toUByte())
    writeUByte(bmap.bases.count().toUByte())
    writeUByte(bmap.starts.count().toUByte())
    writeMulti(bmap.pills.asIterable()) { writePill(it) }
    writeMulti(bmap.bases.asIterable()) { writeBase(it) }
    writeMulti(bmap.starts.asIterable()) { writeStart(it) }

    for (y in border.until(worldHeight - border)) {
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

                            for (i in beginX.until(x)) {
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
}

fun writeDamage(
    bmap: Bmap,
    buffer: MutableList<UByte>,
) {
    fun writeString(str: String) {
        for (c in str.encodeToByteArray().toUByteArray()) {
            buffer.add(c)
        }
    }

    fun writeUByte(n: UByte) {
        buffer.add(n)
    }

    fun writeBuffer(buf: List<UByte>) {
        for (byte in buf) {
            buffer.add(byte)
        }
    }

    writeString("BMAPDAMG")
    writeUByte(1.toUByte())

    for (y in border.until(worldHeight - border)) {
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

                            for (i in beginX.until(x)) {
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
}

fun writeBmapCode(
    bmapCode: BmapCode,
    buffer: MutableList<UByte>,
) {
    fun writeString(str: String) {
        for (c in str.encodeToByteArray().toUByteArray()) {
            buffer.add(c)
        }
    }

    fun writeUByte(n: UByte) {
        buffer.add(n)
    }

    fun writeBuffer(buf: List<UByte>) {
        for (byte in buf) {
            buffer.add(byte)
        }
    }

    writeString("BMAPCODE")
    writeUByte(1.toUByte())

    for (y in border.until(worldHeight - border)) {
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

                            for (i in beginX.until(x)) {
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
