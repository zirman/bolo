@file:Suppress("unused")

package math

import kotlin.jvm.JvmInline
import kotlin.math.sqrt

@JvmInline
value class V2(val array: FloatArray) {
    companion object {
        fun create(x: Float, y: Float): V2 = V2(floatArrayOf(x, y))
        val ORIGIN: V2 = create(0f, 0f)
        val UNIT_X: V2 = create(1f, 0f)
        val UNIT_Y: V2 = create(0f, 1f)
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

    inline val normalize: V2 get() = scale(1f / magnitude)
    inline val magnitude: Float get() = sqrt(dot(this))
    inline val negative: V2 get() = create(-x, -y)

    fun dot(v: V2): Float = x * v.x + y * v.y
    fun scale(s: Float): V2 = create(x * s, y * s)
    fun div(d: Float): V2 = create(x / d, y / d)
    fun add(v: V2): V2 = create(x + v.x, y + v.y)
    fun sub(v: V2): V2 = create(x - v.x, y - v.y)
    fun eq(v: V2): Boolean = x == v.x && y == v.y
    fun prj(v2: V2): V2 = scale(dot(v2) / dot(this))
}
