package math

import kotlin.math.cos
import kotlin.math.sin

fun dirToVec(bearing: Float): V2 = V2.create(cos(bearing), -sin(bearing))
