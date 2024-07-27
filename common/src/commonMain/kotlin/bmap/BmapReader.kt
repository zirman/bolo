package bmap

import assert.assertEqual
import assert.assertLessThan
import assert.assertLessThanOrEqual
import common.BASE_ARMOR_MAX
import common.BASE_MINES_MAX
import common.BASE_SHELLS_MAX
import common.BASES_MAX
import common.PILL_ARMOR_MAX
import common.PILL_SPEED_MAX
import common.PILLS_MAX
import common.STARTS_MAX
import kotlin.math.min

const val WORLD_WIDTH: Int = 256
const val WORLD_HEIGHT: Int = 256
const val BORDER_WIDTH: Int = 16

enum class TerrainTile {
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
    TreeMined,
    RubbleMined,
    GrassMined,
}

fun ind(col: Int, row: Int): Int = (WORLD_WIDTH * row) + col

class Bmap(
    val pills: Array<Pill>,
    val bases: Array<Base>,
    val starts: Array<StartInfo>,
) {
    private val terrain = UByteArray(WORLD_WIDTH * WORLD_HEIGHT).apply {
        for (y in 0..<WORLD_HEIGHT) {
            for (x in 0..<WORLD_WIDTH) {
                this[ind(x, y)] = defaultTerrain(x, y).ordinal.toUByte()
            }
        }
    }

    operator fun get(col: Int, row: Int): TerrainTile =
        if (col < BORDER_WIDTH || col >= WORLD_WIDTH || row < 0 || row >= WORLD_HEIGHT) TerrainTile.SeaMined
        else TerrainTile.entries[terrain[ind(col, row)].toInt()]

    operator fun set(col: Int, row: Int, t: TerrainTile) {
        if (col >= BORDER_WIDTH && col < WORLD_WIDTH - BORDER_WIDTH && row >= BORDER_WIDTH && row < WORLD_HEIGHT - BORDER_WIDTH) {
            terrain[ind(col, row)] = t.ordinal.toUByte()
        }
    }

    fun damage(col: Int, row: Int) {
        this[col, row] = this[col, row].toTerrainDamage()
    }

    fun mine(col: Int, row: Int) {
        this[col, row].toMinedTerrain()?.let {
            this[col, row] = it
        }
    }

    fun findPill(col: Int, row: Int): Pill? {
        for (pill in pills) {
            if (pill.isPlaced && pill.col == col && pill.row == row) {
                return pill
            }
        }

        return null
    }

    fun findBase(col: Int, row: Int): Base? {
        for (base in bases) {
            if (base.col == col && base.row == row) {
                return base
            }
        }

        return null
    }

    fun getEntity(col: Int, row: Int): Entity {
        for (index in pills.indices) {
            val pill = pills[index]
            if (pill.col == col &&
                pill.row == row &&
                pill.isPlaced
            ) {
                return Entity.Pill(pill)
            }
        }

        for (index in bases.indices) {
            val base = bases[index]
            if (base.col == col &&
                base.row == row
            ) {
                return Entity.Base(bases[index])
            }
        }

        return Entity.Terrain(this[col, row])
    }
}

fun Entity.Pill.isSolid(): Boolean =
    ref.armor > 0

fun Entity.Base.isSolid(owner: Int): Boolean =
    ref.armor > 0 && (ref.owner == 0xff || ref.owner == owner).not()

fun Entity.isSolid(owner: Int): Boolean = when (this) {
    is Entity.Pill -> isSolid()
    is Entity.Base -> isSolid(owner)
    is Entity.Terrain ->
        when (terrain) {
            TerrainTile.Wall,
            TerrainTile.WallDamaged0,
            TerrainTile.WallDamaged1,
            TerrainTile.WallDamaged2,
            TerrainTile.WallDamaged3,
            -> true

            TerrainTile.Sea,
            TerrainTile.River,
            TerrainTile.Swamp0,
            TerrainTile.Swamp1,
            TerrainTile.Swamp2,
            TerrainTile.Swamp3,
            TerrainTile.Crater,
            TerrainTile.Road,
            TerrainTile.Tree,
            TerrainTile.Rubble0,
            TerrainTile.Rubble1,
            TerrainTile.Rubble2,
            TerrainTile.Rubble3,
            TerrainTile.Grass0,
            TerrainTile.Grass1,
            TerrainTile.Grass2,
            TerrainTile.Grass3,
            TerrainTile.Boat,
            TerrainTile.SeaMined,
            TerrainTile.SwampMined,
            TerrainTile.CraterMined,
            TerrainTile.RoadMined,
            TerrainTile.TreeMined,
            TerrainTile.RubbleMined,
            TerrainTile.GrassMined,
            -> false
        }
}

sealed interface Entity {
    data class Pill(val ref: bmap.Pill) : Entity
    data class Base(val ref: bmap.Base) : Entity
    data class Terrain(val terrain: TerrainTile) : Entity
}

data class Pill(
    var col: Int,
    var row: Int,
    var owner: Int,
    var armor: Int,
    var speed: Int,
    // not stored in bmap
    var code: Int,
    var isPlaced: Boolean,
)

data class Base(
    val col: Int,
    val row: Int,
    var owner: Int,
    var armor: Int,
    var shells: Int,
    var mines: Int,
    // not stored in bmap
    var code: Int,
)

data class StartInfo(
    val col: Int,
    val row: Int,
    val direction: Int,
)

data class Run(
    val dataLength: Int,  // length of the data for this run
    // INCLUDING this 4 byte header
    val row: Int,        // y co-ordinate of this run.
    val startCol: Int,   // first square of the run
    val endCol: Int,     // last square of run + 1
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
        val nPills = readMaxUByte(PILLS_MAX.toUByte()).toInt()
        val nBases = readMaxUByte(BASES_MAX.toUByte()).toInt()
        val nStarts = readMaxUByte(STARTS_MAX.toUByte()).toInt()
        val pills = readMulti(nPills) { readPill() }
        val bases = readMulti(nBases) { readBase() }
        val starts = readMulti(nStarts) { readStart() }
        bmap = Bmap(pills.toTypedArray(), bases.toTypedArray(), starts.toTypedArray())

        while (true) {
            val run = readRun()

            if (run.dataLength == 4 && run.row == 0xff && run.startCol == 0xff && run.endCol == 0xff) {
                break
            }

            var col: Int = run.startCol

            while (col < run.endCol) {
                val nib: Int = run.data.readNibble()

                if (nib in 0..7) { // sequence of different terrain
                    val endCol: Int = col + nib + 1
                    endCol.assertLessThanOrEqual(run.endCol)

                    while (col < endCol) {
                        bmap[col, run.row] = nibbleToTerrain(run.data.readNibble())
                        col++
                    }
                } else if (nib in 8..15) { // sequence of the same terrain
                    val endCol: Int = col + nib - 6
                    endCol.assertLessThanOrEqual(run.endCol)
                    val t: TerrainTile = nibbleToTerrain(run.data.readNibble())

                    while (col < endCol) {
                        bmap[col, run.row] = t
                        col++
                    }
                }
            }

            run.data.finish()
        }
    }

    private fun matchString(string: String) {
        val bytes = string.encodeToByteArray()

        for (byte in bytes) {
            offset.assertLessThan(buffer.size)
            byte.assertEqual(buffer[offset].toByte())
            offset++
        }
    }

    private fun matchUByte(uByte: UByte) {
        offset.assertLessThan(buffer.size)
        buffer[offset].assertEqual(uByte)
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
        // x.assertLessThanOrEqual(max)
        return min(x.toInt(), max.toInt()).toUByte()
    }

    private fun <T> readMulti(n: Int, read: () -> T): List<T> {
        val a: MutableList<T> = mutableListOf()

        for (i in 1..n) {
            a.add(read())
        }

        return a
    }

    private fun getNibbleReader(dataLength: Int): NibbleReader {
        (offset + dataLength).assertLessThanOrEqual(buffer.size)
        val nibbleReader = NibbleReader(buffer.sliceArray(offset..<offset + dataLength))
        offset += dataLength
        return nibbleReader
    }

    private fun readPill(): Pill {
        val x = readUByte().toInt()
        val y = readUByte().toInt()
        val owner = readUByte().toInt()
        val armor = readMaxUByte(PILL_ARMOR_MAX.toUByte()).toInt() // range 0-15 (dead pillbox = 0, full strength = 15)
        val speed = readMaxUByte(PILL_SPEED_MAX.toUByte()).toInt() // typically 50. Time between shots, in 20ms units
        // Lower values makes the pillbox start off 'angry'
        return Pill(col = x, row = y, owner = owner, armor = armor, speed = speed, code = 0, isPlaced = true)
    }

    private fun readBase(): Base {
        val x = readUByte().toInt()
        val y = readUByte().toInt()
        val owner = readUByte().toInt()
        val armor = readMaxUByte(BASE_ARMOR_MAX.toUByte()).toInt() // initial stocks of base. Maximum value 90
        val shells = readMaxUByte(BASE_SHELLS_MAX.toUByte()).toInt() // initial stocks of base. Maximum value 90
        val mines = readMaxUByte(BASE_MINES_MAX.toUByte()).toInt() // initial stocks of base. Maximum value 90
        return Base(col = x, row = y, owner = owner, armor = armor, shells = shells, mines, code = 0)
    }

    private fun readStart(): StartInfo {
        val x = readUByte().toInt()
        val y = readUByte().toInt()
        val dir = readMaxUByte(15.toUByte()).toInt()
        return StartInfo(col = x, row = y, direction = dir)
    }

    private fun readRun(): Run {
        val dataLength = readUByte().toInt()
        val y = readUByte().toInt()
        val startCol = readUByte().toInt()
        val endCol = readUByte().toInt()
        val data = getNibbleReader(dataLength - 4)
        return Run(dataLength = dataLength, row = y, startCol = startCol, endCol = endCol, data = data)
    }
}

fun nibbleToTerrain(nibble: Int): TerrainTile = when (nibble) {
    0 -> TerrainTile.Wall
    1 -> TerrainTile.River
    2 -> TerrainTile.Swamp3
    3 -> TerrainTile.Crater
    4 -> TerrainTile.Road
    5 -> TerrainTile.Tree
    6 -> TerrainTile.Rubble3
    7 -> TerrainTile.Grass3
    8 -> TerrainTile.WallDamaged3
    9 -> TerrainTile.Boat
    10 -> TerrainTile.SwampMined
    11 -> TerrainTile.CraterMined
    12 -> TerrainTile.RoadMined
    13 -> TerrainTile.TreeMined
    14 -> TerrainTile.RubbleMined
    15 -> TerrainTile.GrassMined
    else -> throw IllegalStateException("invalid nibble")
}

fun defaultTerrain(col: Int, row: Int): TerrainTile =
    if (col < BORDER_WIDTH || row < BORDER_WIDTH || col >= WORLD_WIDTH - BORDER_WIDTH || row >= WORLD_HEIGHT - BORDER_WIDTH) TerrainTile.SeaMined
    else TerrainTile.Sea

class BmapCode {
    private val code = UByteArray(WORLD_WIDTH * WORLD_HEIGHT)

    operator fun get(col: Int, row: Int): Int = code[ind(col, row)].toInt()

    operator fun set(col: Int, row: Int, t: Int) {
        code[ind(col, row)] = t.toUByte()
    }

    fun inc(col: Int, row: Int): Int {
        val i = ind(col, row)
        val c = code[i].toInt()
        code[i] = (c + 1).mod(16).toUByte()
        return c
    }
}

private fun TerrainTile.toTerrainDamage(): TerrainTile = when (this) {
    TerrainTile.Wall -> TerrainTile.WallDamaged3
    TerrainTile.Swamp0 -> TerrainTile.River
    TerrainTile.Swamp1 -> TerrainTile.Swamp0
    TerrainTile.Swamp2 -> TerrainTile.Swamp1
    TerrainTile.Swamp3 -> TerrainTile.Swamp2
    TerrainTile.Crater -> TerrainTile.Crater
    TerrainTile.Road -> TerrainTile.River
    TerrainTile.Tree -> TerrainTile.Grass3
    TerrainTile.Rubble0 -> TerrainTile.River
    TerrainTile.Rubble1 -> TerrainTile.Rubble0
    TerrainTile.Rubble2 -> TerrainTile.Rubble1
    TerrainTile.Rubble3 -> TerrainTile.Rubble2
    TerrainTile.Grass0 -> TerrainTile.Swamp3
    TerrainTile.Grass1 -> TerrainTile.Grass0
    TerrainTile.Grass2 -> TerrainTile.Grass1
    TerrainTile.Grass3 -> TerrainTile.Grass2
    TerrainTile.WallDamaged0 -> TerrainTile.Rubble3
    TerrainTile.WallDamaged1 -> TerrainTile.WallDamaged0
    TerrainTile.WallDamaged2 -> TerrainTile.WallDamaged1
    TerrainTile.WallDamaged3 -> TerrainTile.WallDamaged2
    TerrainTile.Boat -> TerrainTile.River
    TerrainTile.SwampMined -> TerrainTile.Crater
    TerrainTile.CraterMined -> TerrainTile.Crater
    TerrainTile.RoadMined -> TerrainTile.Crater
    TerrainTile.TreeMined -> TerrainTile.Crater
    TerrainTile.RubbleMined -> TerrainTile.Crater
    TerrainTile.GrassMined -> TerrainTile.Crater
    else -> throw IllegalStateException("toTerrainDamage(): Invalid terrain")
}

fun TerrainTile.toMinedTerrain(): TerrainTile? = when (this) {
    TerrainTile.Swamp0,
    TerrainTile.Swamp1,
    TerrainTile.Swamp2,
    TerrainTile.Swamp3,
    -> TerrainTile.SwampMined

    TerrainTile.Crater -> TerrainTile.CraterMined
    TerrainTile.Road -> TerrainTile.RoadMined
    TerrainTile.Tree -> TerrainTile.TreeMined

    TerrainTile.Rubble0,
    TerrainTile.Rubble1,
    TerrainTile.Rubble2,
    TerrainTile.Rubble3,
    -> TerrainTile.RubbleMined

    TerrainTile.Grass0,
    TerrainTile.Grass1,
    TerrainTile.Grass2,
    TerrainTile.Grass3,
    -> TerrainTile.GrassMined

    else -> null
}
