@file:Suppress("unused", "UnusedReceiverParameter")

package math

import kotlin.math.sqrt

fun v2(x: Float, y: Float): V2 =
    V2(floatArrayOf(x, y))

fun v3(x: Float, y: Float, z: Float): V3 =
    V3(floatArrayOf(x, y, z))

inline var V2.x: Float
    get() = array[0]
    set(x) {
        array[0] = x
    }

inline var V2.y: Float
    get() = array[1]
    set(y) {
        array[1] = y
    }

val v2Origin: V2 = v2(0f, 0f)
val v2x: V2 = v2(1f, 0f)
val v2y: V2 = v2(0f, 1f)

fun V2.dot(v: V2): Float =
    x * v.x + y * v.y

fun V2.norm(): V2 =
    scale(1f / mag())

fun V2.mag(): Float =
    sqrt(dot(this))

fun V2.scale(s: Float): V2 =
    v2(
        x * s,
        y * s,
    )

fun V2.div(d: Float): V2 =
    v2(
        x / d,
        y / d,
    )

fun V2.add(v: V2): V2 =
    v2(
        x + v.x,
        y + v.y,
    )

fun V2.sub(v: V2): V2 =
    v2(
        x - v.x,
        y - v.y,
    )

fun V2.eq(v: V2): Boolean = x == v.x && y == v.y

fun V2.prj(v2: V2): V2 = scale(dot(v2) / dot(this))

inline var V3.x: Float
    get() = array[0]
    set(x) {
        array[0] = x
    }

inline var V3.y: Float
    get() = array[1]
    set(y) {
        array[1] = y
    }

inline var V3.z: Float
    get() = array[2]
    set(z) {
        array[2] = z
    }

const val DIMENSION_V2A: Int = 2
inline val V2A.dimension: Int get() = DIMENSION_V2A
//fun math.V2A.count(): Int = floatArray.size / math.getDimension
//inline val math.V2A.indices: IntRange get() = IntRange(0, count() - 1)

fun V2A.set(i: Int, x: Float, y: Float) {
    array[(i * dimension) + 0] = x
    array[(i * dimension) + 1] = y
}

const val DIMENSION_V3A: Int = 3
inline val V3A.dimension: Int get() = DIMENSION_V3A

fun V3A.set(i: Int, x: Float, y: Float, z: Float) {
    array[(i * dimension) + 0] = x
    array[(i * dimension) + 1] = y
    array[(i * dimension) + 2] = z
}

const val DIMENSION_V4A: Int = 4
inline val V4A.dimension: Int get() = DIMENSION_V4A
//fun math.V4A.count(): Int = floatArray.size / math.getDimension

fun V4A.getX(i: Int): Float = array[(i * dimension) + 0]
fun V4A.getY(i: Int): Float = array[(i * dimension) + 1]
fun V4A.getZ(i: Int): Float = array[(i * dimension) + 2]
fun V4A.getW(i: Int): Float = array[(i * dimension) + 3]

fun V4A.set(i: Int, x: Float, y: Float, z: Float, w: Float) {
    array[(i * dimension) + 0] = x
    array[(i * dimension) + 1] = y
    array[(i * dimension) + 2] = z
    array[(i * dimension) + 3] = w
}

fun V3.dot(v: V3): Float =
    x * v.x + y * v.y + z * v.z

fun V3.cross(v: V3): V3 =
    v3(
        y * v.z - z * v.y,
        -(x * v.z - z * v.x),
        x * v.y - y * v.x,
    )

fun V3.neg(): V3 =
    v3(
        -x,
        -y,
        -z,
    )

val v3Origin: V3 = v3(0f, 0f, 0f)
val v3x: V3 = v3(1f, 0f, 0f)
val v3y: V3 = v3(0f, 1f, 0f)
val v3z: V3 = v3(0f, 0f, 1f)

fun V3.norm(): V3 =
    scale(1f / mag())

fun V3.mag(): Float =
    sqrt(dot(this))

fun V3.scale(s: Float): V3 =
    v3(
        x * s,
        y * s,
        z * s,
    )

fun V3.div(d: Float): V3 =
    v3(
        x / d,
        y / d,
        z / d,
    )

fun V3.add(v: V3): V3 =
    v3(
        x + v.x,
        y + v.y,
        z + v.z,
    )

fun V3.sub(v: V3): V3 =
    v3(
        x - v.x,
        y - v.y,
        z - v.z,
    )

fun V3.eq(v: V3): Boolean = x == v.x &&
        y == v.y &&
        z == v.z
