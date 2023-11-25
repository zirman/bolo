@file:Suppress("unused")

package math

import org.khronos.webgl.Float32Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

value class Q4(val array: Float32Array)

fun q4(x: Float, y: Float, z: Float, w: Float): Q4 = Q4(Float32Array(arrayOf(x, y, z, w)))

inline var Q4.x: Float
    get() = array[0]
    set(x) {
        array[0] = x
    }

inline var Q4.y: Float
    get() = array[1]
    set(y) {
        array[1] = y
    }

inline var Q4.z: Float
    get() = array[2]
    set(z) {
        array[2] = z
    }

inline var Q4.w: Float
    get() = array[3]
    set(w) {
        array[3] = w
    }

fun Q4.multiply(s: Q4): Q4 =
    q4(
        w * s.x + x * s.w - y * s.z + z * s.y,
        w * s.y + x * s.z + y * s.w - z * s.x,
        w * s.z - x * s.y + y * s.x + z * s.w,
        w * s.w - x * s.x - y * s.y - z * s.z,
    )

fun Q4.inverse(): Q4 =
    q4(-x, -y, -z, w)

fun q4V3(p: V3): Q4 =
    q4(p.x, p.y, p.z, 0f)

fun q4AxisAngle(axis: V3, theta: Float): Q4 {
    val theta2: Float = theta / 2f
    val sinTheta2: Float = sin(theta2)
    val cosTheta2: Float = cos(theta2)
    return q4(axis.x * sinTheta2, axis.y * sinTheta2, axis.z * sinTheta2, cosTheta2)
}

fun Q4.q4Rotate(p: V3): V3 =
    with(inverse().multiply(q4V3(p)).multiply(this)) { v3(x, y, z) }

fun q4FromTo(to: V3, from: V3): Q4 {
    val x: V3 = from.cross(to)
    val xLen: Float = x.mag()
    return if (xLen > 0.00001) q4AxisAngle(x.norm(), asin(xLen))
    else q4AxisAngle(v3x, acos(from.dot(to)))
}
