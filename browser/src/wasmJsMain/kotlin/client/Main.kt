package client

@JsFun(
    """
function fib(i) {
    if (i === 0 || i === 1) return i
    return fib(i - 1) + fib(i - 2)
}
"""
)
external fun fibJs(i: Double): Double

//const start = Date.now()
//fib(38)
//const end = Date.now()
//console.log(`js time: ${end - start} ms`)

fun main() {
    ClientApplicationModuleImpl()

//    val i = 38
//
//    measureTime {
//        println("kotlin/wasm fib($i) ${fib(i)}")
//    }.also { println("kotlin/wasm time: ${it.inWholeMilliseconds} ms") }
//
//    measureTime {
//        println("js fib($i) ${fibJs(i.toDouble())}")
//    }.also { println("js time: ${it.inWholeMilliseconds} ms") }
}
