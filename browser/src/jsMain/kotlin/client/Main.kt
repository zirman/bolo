package client

fun main() {
//    val i = 38
//
//    measureTime {
//        println("kotlin/js fib($i) ${fib(i)}")
//    }.also { println("kotlin/js time: ${it.inWholeMilliseconds}") }

    ClientApplicationModuleImpl().start()
}
