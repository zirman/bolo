package client.bmap

import common.bmap.Bmap
import common.bmap.NibbleReader
import common.bmap.Run

class BmapDamageReader(
    offset: Int,
    bmap: Bmap,
    private val buffer: UByteArray,
) {
    var offset = offset
        private set

    init {
        matchString("BMAPDAMG")
        matchUByte(1.toUByte())

        while (true) {
            val run = readRun()

            if (run.dataLength == 4 && run.row == 0xff && run.startCol == 0xff && run.endCol == 0xff) {
                break
            }

            var col = run.startCol

            while (col < run.endCol) {
                val nib = run.data.readNibble()

                if (nib in 0..7) { // sequence of different terrain
                    val endCol = col + nib + 1
                    check(endCol <= run.endCol)

                    while (col < endCol) {
                        val dLevel: Int = run.data.readNibble()

                        for (i in 0..<dLevel) {
                            bmap.damage(col, run.row)
                        }
                        col++
                    }
                } else if (nib in 8..15) { // sequence of the same terrain
                    val endCol = col + nib - 6
                    check(endCol <= run.endCol)
                    val dLevel: Int = run.data.readNibble()

                    while (col < endCol) {
                        for (i in 0..<dLevel) {
                            bmap.damage(col, run.row)
                        }

                        col++
                    }
                }
            }

            run.data.finish()
        }
    }

    private fun matchString(string: String) {
        val bytes = string.encodeToByteArray().toUByteArray()

        for (byte in bytes) {
            check(offset < buffer.size)
            check(byte == buffer[offset])
            offset++
        }
    }

    private fun matchUByte(uByte: UByte) {
        check(offset < buffer.size)
        check(buffer[offset] == uByte)
        offset++
    }

    private fun readUByte(): UByte {
        check(offset < buffer.size)
        val x = buffer[offset]
        offset++
        return x
    }

    private fun getNibbleReader(dataLength: Int): NibbleReader {
        check((offset + dataLength) <= buffer.size)
        val nibbleReader = NibbleReader(buffer.sliceArray(offset..<offset + dataLength))
        offset += dataLength
        return nibbleReader
    }

    private fun readRun(): Run {
        val dataLength: Int = readUByte().toInt()
        val row: Int = readUByte().toInt()
        val startCol: Int = readUByte().toInt()
        val endCol: Int = readUByte().toInt()
        val data = getNibbleReader(dataLength - 4)
        return Run(dataLength, row, startCol, endCol, data)
    }
}
