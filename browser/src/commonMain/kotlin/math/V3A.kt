@file:Suppress("NOTHING_TO_INLINE", "unused")

package math

import kotlin.jvm.JvmInline

@JvmInline
value class V3A(val array: FloatArray) {
    companion object {
        const val DIMENSION: Int = 3
    }

    inline val size: Int get() = array.size / DIMENSION
    inline val indices: IntRange get() = 0..<size

    inline fun getX(i: Int): Float = array[(i * DIMENSION) + 0]
    inline fun getY(i: Int): Float = array[(i * DIMENSION) + 1]
    inline fun getZ(i: Int): Float = array[(i * DIMENSION) + 2]

    inline fun set(i: Int, x: Float, y: Float, z: Float) {
        array[(i * DIMENSION) + 0] = x
        array[(i * DIMENSION) + 1] = y
        array[(i * DIMENSION) + 2] = z
    }
}
