@file:Suppress("unused")

package client.math

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

inline val Float.Companion.degreesInTau: Float get() = 360f
inline val Float.Companion.pi: Float get() = PI.toFloat()
inline val Float.Companion.tau: Float get() = pi * 2
inline val Float.toDegrees: Float get() = this * (Float.degreesInTau / Float.tau)
inline val Float.toRadians: Float get() = this * (Float.tau / Float.degreesInTau)
inline val Float.squared: Float get() = this * this
fun Int.clamp(min: Int, max: Int): Int = min(max(this, min), max)
fun Float.clamp(min: Float, max: Float): Float = min(max(this, min), max)

fun dirToVec(bearing: Float): V2 = V2.create(cos(bearing), -sin(bearing))
fun mulV3(r: FloatArray, ri: Int, v: FloatArray, vi: Int, s: Float) {
    r[ri + 0] = v[vi + 0] * s
    r[ri + 1] = v[vi + 1] * s
    r[ri + 2] = v[vi + 2] * s
}

inline val Double.Companion.degreesInTau: Double get() = 360.0
inline val Double.Companion.pi: Double get() = PI
inline val Double.Companion.tau: Double get() = pi * 2
inline val Double.toDegrees: Double get() = this * (Double.degreesInTau / Double.tau)
inline val Double.toRadians: Double get() = this * (Double.tau / Double.degreesInTau)
