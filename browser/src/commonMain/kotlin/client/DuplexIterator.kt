package client

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.RestrictsSuspension
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.startCoroutine

interface DuplexIterator<in I, out T> {
    fun hasNext(): Boolean
    fun next(input: I): T
}

@RestrictsSuspension
abstract class AbstractDuplexScope<T, I> internal constructor(): Continuation<Unit>, DuplexIterator<I, T> {
    abstract suspend fun yield(value: T): I
}

class DuplexScope<I, T> : AbstractDuplexScope<I, T>() {
    private var done = false
    private var output: I? = null
    private var nextStep: Continuation<T>? = null

    override suspend fun yield(value: I): T {
        output = value

        return suspendCoroutineUninterceptedOrReturn { continuation ->
            nextStep = continuation
            COROUTINE_SUSPENDED
        }
    }

    override val context: CoroutineContext = EmptyCoroutineContext

    override fun resumeWith(result: Result<Unit>) {
        done = true
        result.getOrThrow()
    }

    override fun hasNext(): Boolean {
        return !done
    }

    override fun next(input: T): I {
        val o = output!!
        nextStep?.resume(input)
        return o
    }
}

fun <I, T> duplexIterator(block: suspend AbstractDuplexScope<I, T>.() -> Unit): DuplexIterator<T, I> {
    val receiver = DuplexScope<I, T>()
    block.startCoroutine(receiver = receiver, completion = receiver)
    return receiver
}
