package client

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.RestrictsSuspension
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.startCoroutine

interface DuplexIterator<in I : Any, out T : Any> {
    fun next(input: I): T?
}

@RestrictsSuspension
abstract class DuplexScope<T : Any, I : Any> internal constructor() : Continuation<Unit>, DuplexIterator<I, T> {
    abstract suspend fun yieldGet(value: T): I
}

class DuplexScopeImpl<I : Any, T : Any> : DuplexScope<I, T>() {
    private var done = false
    private var output: I? = null
    private lateinit var nextStep: Continuation<T>

    override suspend fun yieldGet(value: I): T {
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

    override fun next(input: T): I? {
        if (done) return null
        nextStep.resume(input)
        val o = output
        output = null
        return o
    }
}

fun <I : Any, T : Any> duplexIterator(block: suspend DuplexScope<I, T>.() -> Unit): DuplexIterator<T, I> {
    val receiver = DuplexScopeImpl<I, T>()
    block.startCoroutine(receiver = receiver, completion = receiver)
    return receiver
}
