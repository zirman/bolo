package client

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.RestrictsSuspension
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.startCoroutine

interface Consumer<in T> {
    fun yield(input: T)
}

@RestrictsSuspension
abstract class ConsumerScope<T> internal constructor() : Continuation<Unit>, Consumer<T> {
    abstract val done: Boolean
    abstract suspend fun next(): T
}

class ConsumerScopeImpl<T> : ConsumerScope<T>() {
    override var done = false
        private set

    private lateinit var nextStep: Continuation<T>

    override suspend fun next(): T {
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

    override fun yield(input: T) {
        if (done) throw IllegalStateException("called yield() on a completed Consumer")
        nextStep.resume(input)
    }
}

fun <T> consumer(block: suspend ConsumerScope<T>.() -> Unit): Consumer<T> {
    val receiver = ConsumerScopeImpl<T>()
    block.startCoroutine(receiver = receiver, completion = receiver)
    return receiver
}
