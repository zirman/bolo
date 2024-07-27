package common.bmap

import common.assert.assertEqual
import common.assert.assertLessThan
import common.assert.assertLessThanOrEqual

class BmapCodeReader(
    offset: Int,
    private val buffer: UByteArray,
) {
    var offset: Int = offset
        private set

    val bmapCode: BmapCode = BmapCode()

    init {
        matchString("BMAPCODE")
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
                    endCol.assertLessThanOrEqual(run.endCol)

                    while (col < endCol) {
                        bmapCode[col, run.row] = run.data.readNibble()
                        col++
                    }
                } else if (nib in 8..15) { // sequence of the same terrain
                    val endCol = col + nib - 6
                    endCol.assertLessThanOrEqual(run.endCol)
                    val t = run.data.readNibble()

                    while (col < endCol) {
                        bmapCode[col, run.row] = t
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
        val x = buffer[offset]
        offset++
        return x
    }

    private fun getNibbleReader(dataLength: Int): NibbleReader {
        (offset + dataLength).assertLessThanOrEqual(buffer.size)
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
