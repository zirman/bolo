package client

fun fib(i: Int): Int = when (i) {
    0, 1 -> i
    else -> fib(i - 1) + fib(i - 2)
}
