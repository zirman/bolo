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

fun Int.clampRange(minVal: Int, maxVal: Int): Int =
    min(max(this, minVal), maxVal)

fun Int.clampCycle(maxVal: Int): Int =
    if (this < 0) maxVal + (this % maxVal) else this % maxVal

fun Float.clampRange(minVal: Float, maxVal: Float): Float =
    min(max(this, minVal), maxVal)

fun Float.clampCycle(maxVal: Float): Float =
    if (this < 0) maxVal + (this % maxVal) else this % maxVal

fun Double.clampRange(minVal: Double, maxVal: Double): Double =
    min(max(this, minVal), maxVal)

fun Double.clampCycle(maxVal: Double): Double =
    if (this < 0) maxVal + (this % maxVal) else this % maxVal
