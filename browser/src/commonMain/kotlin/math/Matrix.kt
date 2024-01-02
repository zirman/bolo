@file:Suppress("unused")

package math

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

inline var M4.m00: Float
    get() = array[0]
    set(x) {
        array[0] = x
    }

inline var M4.m01: Float
    get() = array[1]
    set(x) {
        array[1] = x
    }

inline var M4.m02: Float
    get() = array[2]
    set(x) {
        array[2] = x
    }

inline var M4.m03: Float
    get() = array[3]
    set(x) {
        array[3] = x
    }

inline var M4.m10: Float
    get() = array[4]
    set(x) {
        array[4] = x
    }

inline var M4.m11: Float
    get() = array[5]
    set(x) {
        array[5] = x
    }

inline var M4.m12: Float
    get() = array[6]
    set(x) {
        array[6] = x
    }

inline var M4.m13: Float
    get() = array[7]
    set(x) {
        array[7] = x
    }

inline var M4.m20: Float
    get() = array[8]
    set(x) {
        array[8] = x
    }

inline var M4.m21: Float
    get() = array[9]
    set(x) {
        array[9] = x
    }

inline var M4.m22: Float
    get() = array[10]
    set(x) {
        array[10] = x
    }

inline var M4.m23: Float
    get() = array[11]
    set(x) {
        array[11] = x
    }

inline var M4.m30: Float
    get() = array[12]
    set(x) {
        array[12] = x
    }

inline var M4.m31: Float
    get() = array[13]
    set(x) {
        array[13] = x
    }

inline var M4.m32: Float
    get() = array[14]
    set(x) {
        array[14] = x
    }

inline var M4.m33: Float
    get() = array[15]
    set(x) {
        array[15] = x
    }

fun m4Identity() = M4(
    floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f,
    ),
)

fun m4Scale(x: Float, y: Float, z: Float) = M4(
    floatArrayOf(
        x, 0f, 0f, 0f,
        0f, y, 0f, 0f,
        0f, 0f, z, 0f,
        0f, 0f, 0f, 1f,
    ),
)

fun m4Scale(v: V3) = M4(
    floatArrayOf(
        v.x, 0f, 0f, 0f,
        0f, v.y, 0f, 0f,
        0f, 0f, v.z, 0f,
        0f, 0f, 0f, 1f,
    ),
)

fun m4Translate(x: Float, y: Float, z: Float) = M4(
    floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        x, y, z, 1f,
    ),
)

fun m4Translate(v: V3) = M4(
    floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        v.x, v.y, v.z, 1f,
    ),
)

fun M4.multiply(m: M4) = M4(
    floatArrayOf(
        m.m00 * m00 + m.m01 * m10 + m.m02 * m20 + m.m03 * m30,
        m.m00 * m01 + m.m01 * m11 + m.m02 * m21 + m.m03 * m31,
        m.m00 * m02 + m.m01 * m12 + m.m02 * m22 + m.m03 * m32,
        m.m00 * m03 + m.m01 * m13 + m.m02 * m23 + m.m03 * m33,
        m.m10 * m00 + m.m11 * m10 + m.m12 * m20 + m.m13 * m30,
        m.m10 * m01 + m.m11 * m11 + m.m12 * m21 + m.m13 * m31,
        m.m10 * m02 + m.m11 * m12 + m.m12 * m22 + m.m13 * m32,
        m.m10 * m03 + m.m11 * m13 + m.m12 * m23 + m.m13 * m33,
        m.m20 * m00 + m.m21 * m10 + m.m22 * m20 + m.m23 * m30,
        m.m20 * m01 + m.m21 * m11 + m.m22 * m21 + m.m23 * m31,
        m.m20 * m02 + m.m21 * m12 + m.m22 * m22 + m.m23 * m32,
        m.m20 * m03 + m.m21 * m13 + m.m22 * m23 + m.m23 * m33,
        m.m30 * m00 + m.m31 * m10 + m.m32 * m20 + m.m33 * m30,
        m.m30 * m01 + m.m31 * m11 + m.m32 * m21 + m.m33 * m31,
        m.m30 * m02 + m.m31 * m12 + m.m32 * m22 + m.m33 * m32,
        m.m30 * m03 + m.m31 * m13 + m.m32 * m23 + m.m33 * m33,
    ),
)

fun M4.multiply(x: Float, y: Float, z: Float) = v3(
    m00 * x + m10 * y + m20 * z + m30,
    m01 * x + m11 * y + m21 * z + m31,
    m02 * x + m12 * y + m22 * z + m32,
)

fun M4.multiply(v: V3) = v3(
    m00 * v.x + m10 * v.y + m20 * v.z + m30,
    m01 * v.x + m11 * v.y + m21 * v.z + m31,
    m02 * v.x + m12 * v.y + m22 * v.z + m32,
)

fun m4RotationX(angle: Float) = M4(
    run {
        val c: Float = cos(angle)
        val s: Float = sin(angle)

        floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, c, s, 0f,
            0f, -s, c, 0f,
            0f, 0f, 0f, 1f,
        )
    },
)

fun m4RotationY(angle: Float) = M4(
    run {
        val c: Float = cos(angle)
        val s: Float = sin(angle)

        floatArrayOf(
            c, 0f, -s, 0f,
            0f, 1f, 0f, 0f,
            s, 0f, c, 0f,
            0f, 0f, 0f, 1f,
        )
    },
)

fun m4RotationZ(angle: Float) = M4(
    run {
        val c: Float = cos(angle)
        val s: Float = sin(angle)

        floatArrayOf(
            c, s, 0f, 0f,
            -s, c, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f,
        )
    },
)

fun mulV3(r: FloatArray, ri: Int, v: FloatArray, vi: Int, s: Float) {
    r[ri + 0] = v[vi + 0] * s
    r[ri + 1] = v[vi + 1] * s
    r[ri + 2] = v[vi + 2] * s
}

fun M4.inverse() = M4(
    run {
        val a2323 = m22 * m33 - m23 * m32
        val a1323 = m21 * m33 - m23 * m31
        val a1223 = m21 * m32 - m22 * m31
        val a0323 = m20 * m33 - m23 * m30
        val a0223 = m20 * m32 - m22 * m30
        val a0123 = m20 * m31 - m21 * m30
        val a2313 = m12 * m33 - m13 * m32
        val a1313 = m11 * m33 - m13 * m31
        val a1213 = m11 * m32 - m12 * m31
        val a2312 = m12 * m23 - m13 * m22
        val a1312 = m11 * m23 - m13 * m21
        val a1212 = m11 * m22 - m12 * m21
        val a0313 = m10 * m33 - m13 * m30
        val a0213 = m10 * m32 - m12 * m30
        val a0312 = m10 * m23 - m13 * m20
        val a0212 = m10 * m22 - m12 * m20
        val a0113 = m10 * m31 - m11 * m30
        val a0112 = m10 * m21 - m11 * m20

        val det = 1 / (m00 * (m11 * a2323 - m12 * a1323 + m13 * a1223)
                - m01 * (m10 * a2323 - m12 * a0323 + m13 * a0223)
                + m02 * (m10 * a1323 - m11 * a0323 + m13 * a0123)
                - m03 * (m10 * a1223 - m11 * a0223 + m12 * a0123))

        floatArrayOf(
            det * (m11 * a2323 - m12 * a1323 + m13 * a1223),
            det * -(m01 * a2323 - m02 * a1323 + m03 * a1223),
            det * (m01 * a2313 - m02 * a1313 + m03 * a1213),
            det * -(m01 * a2312 - m02 * a1312 + m03 * a1212),
            det * -(m10 * a2323 - m12 * a0323 + m13 * a0223),
            det * (m00 * a2323 - m02 * a0323 + m03 * a0223),
            det * -(m00 * a2313 - m02 * a0313 + m03 * a0213),
            det * (m00 * a2312 - m02 * a0312 + m03 * a0212),
            det * (m10 * a1323 - m11 * a0323 + m13 * a0123),
            det * -(m00 * a1323 - m01 * a0323 + m03 * a0123),
            det * (m00 * a1313 - m01 * a0313 + m03 * a0113),
            det * -(m00 * a1312 - m01 * a0312 + m03 * a0112),
            det * -(m10 * a1223 - m11 * a0223 + m12 * a0123),
            det * (m00 * a1223 - m01 * a0223 + m02 * a0123),
            det * -(m00 * a1213 - m01 * a0213 + m02 * a0113),
            det * (m00 * a1212 - m01 * a0212 + m02 * a0112),
        )
    },
)

fun perspective(fieldOfViewInRadians: Float, aspect: Float, near: Float, far: Float) = M4(
    run {
        val f: Float = tan((Float.tau / 4 - fieldOfViewInRadians / 2))
        val rangeInv: Float = 1f / (near - far)

        floatArrayOf(
            (f / aspect), 0f, 0f, 0f,
            0f, f, 0f, 0f,
            0f, 0f, ((near + far) * rangeInv), -1f,
            0f, 0f, (near * far * rangeInv * 2f), 0f,
        )
    },
)

fun orthographicProj2d(left: Float, right: Float, bottom: Float, top: Float) = M4(
    floatArrayOf(
        2f / (right - left), 0f, 0f, 0f,
        0f, 2f / (top - bottom), 0f, 0f,
        0f, 0f, 1f, 0f,
        -(right + left) / (right - left), -(top + bottom) / (top - bottom), 0f, 1f,
    ),
)
