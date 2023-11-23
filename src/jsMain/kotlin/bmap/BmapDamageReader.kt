@file:OptIn(ExperimentalUnsignedTypes::class)

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

            if (run.dataLen == 4 && run.y == 0xff && run.startX == 0xff && run.endX == 0xff) {
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

    private fun matchString(str: String) {
        val bytes = str.encodeToByteArray().toUByteArray()

        for (byte in bytes) {
            offset.assertLessThan(buffer.size)
            byte.assertEqual(buffer[offset])
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
        val x = buffer[offset]
        offset++
        return x
    }

    private fun getNibbleReader(dataLen: Int): NibbleReader {
        (offset + dataLen).assertLessThanOrEqual(buffer.size)
        val nibbleReader = NibbleReader(buffer.sliceArray(offset..<offset + dataLen))
        offset += dataLen
        return nibbleReader
    }

    private fun readRun(): Run {
        val dataLen: Int = readUByte().toInt()
        val y: Int = readUByte().toInt()
        val startX: Int = readUByte().toInt()
        val endX: Int = readUByte().toInt()
        val data = getNibbleReader(dataLen - 4)
        return Run(dataLen, y, startX, endX, data)
    }
}
