@file:OptIn(ExperimentalUnsignedTypes::class)

package bmap

import assert.assertEqual
import assert.assertLessThan
import assert.assertLessThanOrEqual
import util.baseArmorMax
import util.baseMinesMax
import util.baseShellsMax
import util.basesMax
import math.clampCycle
import util.pillArmorMax
import util.pillSpeedMax
import util.pillsMax
import util.startsMax

const val worldWidth: Int = 256
const val worldHeight: Int = 256
const val border: Int = 16

enum class Terrain {
    Sea,
    Boat,
    Wall,
    River,
    Swamp0,
    Swamp1,
    Swamp2,
    Swamp3,
    Crater,
    Road,
    Tree,
    Rubble0,
    Rubble1,
    Rubble2,
    Rubble3,
    Grass0,
    Grass1,
    Grass2,
    Grass3,
    WallDamaged0,
    WallDamaged1,
    WallDamaged2,
    WallDamaged3,
    SeaMined,
    SwampMined,
    CraterMined,
    RoadMined,
    ForestMined,
    RubbleMined,
    GrassMined,
}

fun ind(x: Int, y: Int): Int = (worldWidth * y) + x

class Bmap(
    val pills: Array<Pill>,
    val bases: Array<Base>,
    val starts: Array<StartInfo>,
) {
    private val terrain = UByteArray(worldWidth * worldHeight)
        .apply {
            for (y in 0.until(worldHeight)) {
                for (x in 0.until(worldWidth)) {
                    this[ind(x, y)] = defaultTerrain(x, y).ordinal.toUByte()
                }
            }
        }

    operator fun get(x: Int, y: Int): Terrain =
        if (x < border || x >= worldWidth || y < 0 || y >= worldHeight) Terrain.SeaMined
        else Terrain.entries[terrain[ind(x, y)].toInt()]

    operator fun set(x: Int, y: Int, t: Terrain) {
        if (x >= border && x < worldWidth - border && y >= border && y < worldHeight - border) {
            terrain[ind(x, y)] = t.ordinal.toUByte()
        }
    }

    fun damage(x: Int, y: Int) {
        this[x, y] = terrainDamage(this[x, y])
    }

    fun findPill(x: Int, y: Int): Pill? {
        for (pill in pills) {
            if (pill.isPlaced && pill.x == x && pill.y == y) {
                return pill
            }
        }

        return null
    }

    fun findBase(x: Int, y: Int): Base? {
        for (base in bases) {
            if (base.x == x && base.y == y) {
                return base
            }
        }

        return null
    }

    fun getEntity(x: Int, y: Int): Entity {
        for (index in pills.indices) {
            val pill = pills[index]
            if (pill.x == x &&
                pill.y == y &&
                pill.isPlaced
            ) {
                return Entity.Pill(pill)
            }
        }

        for (index in bases.indices) {
            val base = bases[index]
            if (base.x == x &&
                base.y == y
            ) {
                return Entity.Base(bases[index])
            }
        }

        return Entity.Terrain(this[x, y])
    }
}

fun Entity.Pill.isSolid(): Boolean =
    ref.armor > 0

fun Entity.Base.isSolid(owner: Int): Boolean =
    ref.armor > 0 && (ref.owner == 0xff || ref.owner == owner).not()

fun Entity.isSolid(owner: Int): Boolean =
    when (this) {
        is Entity.Pill -> isSolid()
        is Entity.Base -> isSolid(owner)
        is Entity.Terrain ->
            when (terrain) {
                Terrain.Wall,
                Terrain.WallDamaged0,
                Terrain.WallDamaged1,
                Terrain.WallDamaged2,
                Terrain.WallDamaged3,
                -> true

                Terrain.Sea,
                Terrain.River,
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
                Terrain.Boat,
                Terrain.SeaMined,
                Terrain.SwampMined,
                Terrain.CraterMined,
                Terrain.RoadMined,
                Terrain.ForestMined,
                Terrain.RubbleMined,
                Terrain.GrassMined,
                -> false
            }
    }

sealed interface Entity {
    data class Pill(val ref: bmap.Pill) : Entity
    data class Base(val ref: bmap.Base) : Entity
    data class Terrain(val terrain: bmap.Terrain) : Entity
}

data class Pill(
    var x: Int,
    var y: Int,
    var owner: Int,
    var armor: Int,
    var speed: Int,
    // not stored in bmap
    var code: Int,
    var isPlaced: Boolean,
)

data class Base(
    val x: Int,
    val y: Int,
    var owner: Int,
    var armor: Int,
    var shells: Int,
    var mines: Int,
    // not stored in bmap
    var code: Int,
)

data class StartInfo(
    val x: Int,
    val y: Int,
    val dir: Int,
)

data class Run(
    val dataLen: Int,  // length of the data for this run
    // INCLUDING this 4 byte header
    val y: Int,        // y co-ordinate of this run.
    val startX: Int,   // first square of the run
    val endX: Int,     // last square of run + 1
    // (ie first deep sea square after run)
    val data: NibbleReader,
)

class NibbleReader(private val buffer: UByteArray) {
    private var offset: Int = 0
    private var nibbled = false

    fun readNibble(): Int =
        if (nibbled) {
            val nibble: Int = buffer[offset].toInt().and(0b00001111)
            nibbled = false
            offset++
            nibble
        } else {
            offset.assertLessThan(buffer.size)
            nibbled = true
            (buffer[offset].toInt().and(0b11110000)).ushr(4)
        }

    fun finish() {
        if (nibbled) {
            readNibble().assertEqual(0)
        }

        offset.assertEqual(buffer.size)
    }
}

class BmapReader(
    offset: Int,
    private val buffer: UByteArray,
) {
    var offset: Int = offset
        private set

    val bmap: Bmap

    init {
        matchString("BMAPBOLO")
        matchUByte(1.toUByte())
        val nPills = readMaxUByte(pillsMax.toUByte()).toInt()
        val nBases = readMaxUByte(basesMax.toUByte()).toInt()
        val nStarts = readMaxUByte(startsMax.toUByte()).toInt()
        val pills = readMulti(nPills) { readPill() }
        val bases = readMulti(nBases) { readBase() }
        val starts = readMulti(nStarts) { readStart() }
        bmap = Bmap(pills.toTypedArray(), bases.toTypedArray(), starts.toTypedArray())

        while (true) {
            val run = readRun()

            if (run.dataLen == 4 && run.y == 0xff && run.startX == 0xff && run.endX == 0xff) {
                break
            }

            var x: Int = run.startX

            while (x < run.endX) {
                val nib: Int = run.data.readNibble()

                if (nib in 0..7) { // sequence of different terrain
                    val endX: Int = x + nib + 1
                    endX.assertLessThanOrEqual(run.endX)

                    while (x < endX) {
                        bmap[x, run.y] = nibbleToTerrain(run.data.readNibble())
                        x++
                    }
                } else if (nib in 8..15) { // sequence of the same terrain
                    val endX: Int = x + nib - 6
                    endX.assertLessThanOrEqual(run.endX)
                    val t: Terrain = nibbleToTerrain(run.data.readNibble())

                    while (x < endX) {
                        bmap[x, run.y] = t
                        x++
                    }
                }
            }

            run.data.finish()
        }
    }

    private fun matchString(str: String) {
        val bytes = str.encodeToByteArray()

        for (byte in bytes) {
            offset.assertLessThan(buffer.size)
            byte.assertEqual(buffer[offset].toByte())
            offset++
        }
    }

    private fun matchUByte(c: UByte) {
        offset.assertLessThan(buffer.size)
        buffer[offset].assertEqual(c)
        offset++
    }

    private fun readUByte(): UByte {
        offset.assertLessThan(buffer.size)
        val x: UByte = buffer[offset]
        offset++
        return x
    }

    private fun readMaxUByte(max: UByte): UByte {
        offset.assertLessThan(buffer.size)
        val x: UByte = buffer[offset]
        offset++
        x.assertLessThanOrEqual(max)
        return x
    }

    private fun <T> readMulti(n: Int, read: () -> T): List<T> {
        val a: MutableList<T> = mutableListOf()

        for (i in 1..n) {
            a.add(read())
        }

        return a
    }

    private fun getNibbleReader(dataLen: Int): NibbleReader {
        (offset + dataLen).assertLessThanOrEqual(buffer.size)
        val nibbleReader = NibbleReader(buffer.sliceArray(offset.until(offset + dataLen)))
        offset += dataLen
        return nibbleReader
    }

    private fun readPill(): Pill {
        val x = readUByte().toInt()
        val y = readUByte().toInt()
        val owner = readUByte().toInt()
        val armor = readMaxUByte(pillArmorMax.toUByte()).toInt() // range 0-15 (dead pillbox = 0, full strength = 15)
        val speed = readMaxUByte(pillSpeedMax.toUByte()).toInt() // typically 50. Time between shots, in 20ms units
        // Lower values makes the pillbox start off 'angry'
        return Pill(x = x, y = y, owner = owner, armor = armor, speed = speed, code = 0, isPlaced = true)
    }

    private fun readBase(): Base {
        val x = readUByte().toInt()
        val y = readUByte().toInt()
        val owner = readUByte().toInt()
        val armor = readMaxUByte(baseArmorMax.toUByte()).toInt() // initial stocks of base. Maximum value 90
        val shells = readMaxUByte(baseShellsMax.toUByte()).toInt() // initial stocks of base. Maximum value 90
        val mines = readMaxUByte(baseMinesMax.toUByte()).toInt() // initial stocks of base. Maximum value 90
        return Base(x = x, y = y, owner = owner, armor = armor, shells = shells, mines, code = 0)
    }

    private fun readStart(): StartInfo {
        val x = readUByte().toInt()
        val y = readUByte().toInt()
        val dir = readMaxUByte(15.toUByte()).toInt()
        return StartInfo(x = x, y = y, dir = dir)
    }

    private fun readRun(): Run {
        val dataLen = readUByte().toInt()
        val y = readUByte().toInt()
        val startX = readUByte().toInt()
        val endX = readUByte().toInt()
        val data = getNibbleReader(dataLen - 4)
        return Run(dataLen = dataLen, y = y, startX = startX, endX = endX, data = data)
    }
}

fun nibbleToTerrain(nibble: Int): Terrain =
    when (nibble) {
        0 -> Terrain.Wall
        1 -> Terrain.River
        2 -> Terrain.Swamp3
        3 -> Terrain.Crater
        4 -> Terrain.Road
        5 -> Terrain.Tree
        6 -> Terrain.Rubble3
        7 -> Terrain.Grass3
        8 -> Terrain.WallDamaged3
        9 -> Terrain.Boat
        10 -> Terrain.SwampMined
        11 -> Terrain.CraterMined
        12 -> Terrain.RoadMined
        13 -> Terrain.ForestMined
        14 -> Terrain.RubbleMined
        15 -> Terrain.GrassMined
        else -> throw Exception("invalid nibble")
    }

fun defaultTerrain(x: Int, y: Int): Terrain =
    if (x < border || y < border || x >= worldWidth - border || y >= worldHeight - border) Terrain.SeaMined
    else Terrain.Sea

class BmapCode {
    private val code = UByteArray(worldWidth * worldHeight)

    operator fun get(x: Int, y: Int): Int = code[ind(x, y)].toInt()

    operator fun set(x: Int, y: Int, t: Int) {
        code[ind(x, y)] = t.toUByte()
    }

    fun inc(x: Int, y: Int): Int {
        val i = ind(x, y)
        val c = code[i].toInt()
        code[i] = (c + 1).clampCycle(16).toUByte()
        return c
    }
}

private fun terrainDamage(terrain: Terrain): Terrain =
    when (terrain) {
        Terrain.Wall -> Terrain.WallDamaged3
        Terrain.Swamp0 -> Terrain.River
        Terrain.Swamp1 -> Terrain.Swamp0
        Terrain.Swamp2 -> Terrain.Swamp1
        Terrain.Swamp3 -> Terrain.Swamp2
        Terrain.Crater -> Terrain.Crater
        Terrain.Road -> Terrain.River
        Terrain.Tree -> Terrain.Grass3
        Terrain.Rubble0 -> Terrain.River
        Terrain.Rubble1 -> Terrain.Rubble0
        Terrain.Rubble2 -> Terrain.Rubble1
        Terrain.Rubble3 -> Terrain.Rubble2
        Terrain.Grass0 -> Terrain.Swamp3
        Terrain.Grass1 -> Terrain.Grass0
        Terrain.Grass2 -> Terrain.Grass1
        Terrain.Grass3 -> Terrain.Grass2
        Terrain.WallDamaged0 -> Terrain.Rubble3
        Terrain.WallDamaged1 -> Terrain.WallDamaged0
        Terrain.WallDamaged2 -> Terrain.WallDamaged1
        Terrain.WallDamaged3 -> Terrain.WallDamaged2
        Terrain.Boat -> Terrain.River
        Terrain.SwampMined -> Terrain.Crater
        Terrain.CraterMined -> Terrain.Crater
        Terrain.RoadMined -> Terrain.Crater
        Terrain.ForestMined -> Terrain.Crater
        Terrain.RubbleMined -> Terrain.Crater
        Terrain.GrassMined -> Terrain.Crater
        else -> throw Exception("damageTerrain(): invalid terrain")
    }
