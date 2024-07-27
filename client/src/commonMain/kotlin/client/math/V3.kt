@file:Suppress("unused")

package client.math

import kotlin.jvm.JvmInline
import kotlin.math.sqrt

@JvmInline
value class V3(val array: FloatArray) {
    companion object {
        fun create(x: Float, y: Float, z: Float): V3 = V3(floatArrayOf(x, y, z))
        val ORIGIN: V3 = create(0f, 0f, 0f)
        val UNIT_X: V3 = create(1f, 0f, 0f)
        val UNIT_Y: V3 = create(0f, 1f, 0f)
        val UNIT_Z: V3 = create(0f, 0f, 1f)
    }

    inline var x: Float
        get() = array[0]
        set(x) {
            array[0] = x
        }

    inline var y: Float
        get() = array[1]
        set(y) {
            array[1] = y
        }

    inline var z: Float
        get() = array[2]
        set(z) {
            array[2] = z
        }

    inline val negative: V3 get() = create(-x, -y, -z)
    inline val normalize: V3 get() = scale(1f / magnitude)
    inline val magnitude: Float get() = sqrt(dot(this))

    fun dot(v: V3): Float = x * v.x + y * v.y + z * v.z
    fun cross(v: V3): V3 = create(y * v.z - z * v.y, -(x * v.z - z * v.x), x * v.y - y * v.x)
    fun scale(s: Float): V3 = create(x * s, y * s, z * s)
    fun div(d: Float): V3 = create(x / d, y / d, z / d)
    fun add(v: V3): V3 = create(x + v.x, y + v.y, z + v.z)
    fun sub(v: V3): V3 = create(x - v.x, y - v.y, z - v.z)
    fun eq(v: V3): Boolean = x == v.x && y == v.y && z == v.z
}
