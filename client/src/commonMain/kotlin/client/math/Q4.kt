@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package client.math

import kotlin.jvm.JvmInline
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

@JvmInline
value class Q4(val array: FloatArray) {
    companion object {
        fun create(x: Float, y: Float, z: Float, w: Float): Q4 = Q4(floatArrayOf(x, y, z, w))

        fun q4V3(p: V3): Q4 =
            create(p.x, p.y, p.z, 0f)

        fun q4AxisAngle(axis: V3, theta: Float): Q4 {
            val theta2: Float = theta / 2f
            val sinTheta2: Float = sin(theta2)
            val cosTheta2: Float = cos(theta2)
            return create(axis.x * sinTheta2, axis.y * sinTheta2, axis.z * sinTheta2, cosTheta2)
        }

        fun q4FromTo(to: V3, from: V3): Q4 {
            val x: V3 = from.cross(to)
            val xLen: Float = x.magnitude
            return if (xLen > 0.00001) q4AxisAngle(x.normalize, asin(xLen))
            else q4AxisAngle(V3.UNIT_X, acos(from.dot(to)))
        }
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

    inline var w: Float
        get() = array[3]
        set(w) {
            array[3] = w
        }

    fun multiply(s: Q4): Q4 =
        create(
            w * s.x + x * s.w - y * s.z + z * s.y,
            w * s.y + x * s.z + y * s.w - z * s.x,
            w * s.z - x * s.y + y * s.x + z * s.w,
            w * s.w - x * s.x - y * s.y - z * s.z,
        )

    fun inverse(): Q4 =
        create(-x, -y, -z, w)

    fun q4Rotate(p: V3): V3 =
        with(inverse().multiply(q4V3(p)).multiply(this)) { V3.create(x, y, z) }
}
