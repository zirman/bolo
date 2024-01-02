@file:Suppress("unused")

package assert

inline fun <reified B : Any> Any?.assert(message: String): B = (this as? B) ?: throw IllegalStateException(message)

inline fun <reified A : Any> A?.assertNull(message: String? = null) {
    if (this != null) throw IllegalStateException(message ?: "Expected null")
}

inline fun <reified A : Any> A?.assertNotNull(message: String): A = this ?: throw IllegalStateException(message)

fun Int.assertLessThan(max: Int): Int = if (this < max) this else throw IllegalStateException("assertion failed $this < $max")
fun UByte.assertLessThan(max: UByte): UByte = if (this < max) this else throw IllegalStateException("assertion failed $this < $max")
fun Byte.assertLessThan(max: Byte): Byte = if (this < max) this else throw IllegalStateException("assertion failed $this < $max")
fun Float.assertLessThan(max: Float): Float = if (this < max) this else throw IllegalStateException("assertion failed $this < $max")
fun Double.assertLessThan(max: Double): Double =
    if (this < max) this else throw IllegalStateException("assertion failed $this < $max")

fun Int.assertLessThanOrEqual(max: Int): Int =
    if (this <= max) this else throw IllegalStateException("assertion failed $this <= $max")

fun Byte.assertLessThanOrEqual(max: Byte): Byte =
    if (this <= max) this else throw IllegalStateException("assertion failed $this <= $max")

fun UByte.assertLessThanOrEqual(max: UByte): UByte =
    if (this <= max) this else throw IllegalStateException("assertion failed $this <= $max")

fun Float.assertLessThanOrEqual(max: Float): Float =
    if (this <= max) this else throw IllegalStateException("assertion failed $this <= $max")

fun Double.assertLessThanOrEqual(max: Double): Double =
    if (this <= max) this else throw IllegalStateException("assertion failed $this <= $max")

fun UByte.assertEqual(x: UByte): UByte = if (this == x) this else throw IllegalStateException("assertion failed $this == $x")
fun Byte.assertEqual(x: Byte): Byte = if (this == x) this else throw IllegalStateException("assertion failed $this == $x")
fun Int.assertEqual(x: Int): Int = if (this == x) this else throw IllegalStateException("assertion failed $this == $x")
fun Float.assertEqual(x: Float): Float = if (this == x) this else throw IllegalStateException("assertion failed $this == $x")
fun Double.assertEqual(x: Double): Double = if (this == x) this else throw IllegalStateException("assertion failed $this == $x")

fun <A> A.assertEqualObject(x: A): A = if (this == x) this else throw IllegalStateException("assertion failed $this == $x")

fun never(): Nothing {
    throw IllegalStateException("Impossible")
}
