package client

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.RestrictsSuspension
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.startCoroutine

@RestrictsSuspension
sealed interface ConsumerScope<T> {
    suspend fun next(): T
}

interface Consumer<T> {
    val done: Boolean
    fun yield(input: T)
}

class ConsumerImpl<T> internal constructor() : ConsumerScope<T>, Consumer<T>, Continuation<Unit> {
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
    val consumer = ConsumerImpl<T>()
    block.startCoroutine(receiver = consumer, completion = consumer)
    return consumer
}
