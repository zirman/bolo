package math

import kotlin.math.cos
import kotlin.math.sin

fun dirToVec(bearing: Float): V2 = v2(cos(bearing), -sin(bearing))
