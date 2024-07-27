package adapters

import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

class Uint8ArrayAdapterImpl(override val uint8Array: Uint8Array): Uint8ArrayAdapter {
    override fun get(index: Int): UByte = uint8Array[index].toUByte()

    override fun set(index: Int, value: UByte) {
        uint8Array[index] = value.toByte()
    }
}
