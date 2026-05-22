package test

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.resume

internal class TestMonotonicFrameClock : MonotonicFrameClock {

    private val pending = ConcurrentLinkedQueue<(Long) -> Unit>()
    private var closed = false

    override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
        return suspendCancellableCoroutine { cont ->
            pending.add { nanos ->
                if (!closed) cont.resume(onFrame(nanos))
            }
        }
    }

    fun advanceClock(times: Int = 1) {
        repeat(times) {
            while (true) {
                val cb = pending.poll() ?: break
                cb(System.nanoTime())
            }
        }
    }

    fun close() {
        closed = true
        pending.clear()
    }

}
