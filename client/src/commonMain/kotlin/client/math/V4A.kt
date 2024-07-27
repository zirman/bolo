@file:Suppress("NOTHING_TO_INLINE", "unused")

package client.math

import kotlin.jvm.JvmInline

@JvmInline
value class V4A(val array: FloatArray) {
    companion object {
        const val DIMENSION: Int = 4
    }

    inline val size: Int get() = array.size / DIMENSION
    inline val indices: IntRange get() = 0..<size

    inline fun getX(i: Int): Float = array[(i * DIMENSION) + 0]
    inline fun getY(i: Int): Float = array[(i * DIMENSION) + 1]
    inline fun getZ(i: Int): Float = array[(i * DIMENSION) + 2]
    inline fun getW(i: Int): Float = array[(i * DIMENSION) + 3]

    inline fun set(i: Int, x: Float, y: Float, z: Float, w: Float) {
        array[(i * DIMENSION) + 0] = x
        array[(i * DIMENSION) + 1] = y
        array[(i * DIMENSION) + 2] = z
        array[(i * DIMENSION) + 3] = w
    }
}
