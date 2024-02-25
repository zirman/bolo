@file:Suppress("unused")

package math

import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

inline val Float.Companion.degreesInTau: Float get() = 360.0f
inline val Float.Companion.pi: Float get() = PI.toFloat()
inline val Float.Companion.tau: Float get() = pi * 2
inline val Float.toDegrees: Float get() = this * (Float.degreesInTau / Float.tau)
inline val Float.toRadians: Float get() = this * (Float.tau / Float.degreesInTau)

inline val Double.Companion.degreesInTau: Double get() = 360.0
inline val Double.Companion.pi: Double get() = PI
inline val Double.Companion.tau: Double get() = pi * 2
inline val Double.toDegrees: Double get() = this * (Double.degreesInTau / Double.tau)
inline val Double.toRadians: Double get() = this * (Double.tau / Double.degreesInTau)

fun Int.clamp(min: Int, max: Int): Int =
    min(max(this, min), max)

fun Float.clamp(min: Float, max: Float): Float =
    min(max(this, min), max)
