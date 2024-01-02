package bmap

import assert.assertEqual
import assert.assertLessThan
import assert.assertLessThanOrEqual

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

            if (run.dataLength == 4 && run.y == 0xff && run.startX == 0xff && run.endX == 0xff) {
                break
            }

            var x = run.startX

            while (x < run.endX) {
                val nib = run.data.readNibble()

                if (nib in 0..7) { // sequence of different terrain
                    val endX = x + nib + 1
                    endX.assertLessThanOrEqual(run.endX)

                    while (x < endX) {
                        val dLevel: Int = run.data.readNibble()

                        for (i in 0..<dLevel) {
                            bmap.damage(x, run.y)
                        }
                        x++
                    }
                } else if (nib in 8..15) { // sequence of the same terrain
                    val endX = x + nib - 6
                    endX.assertLessThanOrEqual(run.endX)
                    val dLevel: Int = run.data.readNibble()

                    while (x < endX) {
                        for (i in 0..<dLevel) {
                            bmap.damage(x, run.y)
                        }

                        x++
                    }
                }
            }

            run.data.finish()
        }
    }

    private fun matchString(string: String) {
        val bytes = string.encodeToByteArray().toUByteArray()

        for (byte in bytes) {
            offset.assertLessThan(buffer.size)
            byte.assertEqual(buffer[offset])
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
        val y: Int = readUByte().toInt()
        val startX: Int = readUByte().toInt()
        val endX: Int = readUByte().toInt()
        val data = getNibbleReader(dataLength - 4)
        return Run(dataLength, y, startX, endX, data)
    }
}