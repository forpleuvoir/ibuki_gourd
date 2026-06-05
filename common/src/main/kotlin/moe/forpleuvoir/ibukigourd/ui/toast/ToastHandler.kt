package moe.forpleuvoir.ibukigourd.ui.toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

object ToastHandler {

    var maxVisible: Int = 1

    val EXIT_GRACE: Duration = 0.3.seconds

    private val lock = Any()

    private val queue = ArrayDeque<Toast>()

    private val _active = mutableStateListOf<ToastState>()

    val active: List<ToastState> get() = _active

    fun show(
        duration: Duration = 2.seconds,
        strategy: ToastStrategy = ToastStrategy.ReplaceAll,
        animation: ToastAnimation? = null,
        content: @Composable () -> Unit
    ) {
        synchronized(lock) {
            val toast = Toast(
                content = content,
                duration = duration,
                tag = (strategy as? ToastStrategy.Tagged)?.tag,
                animation = animation
            )

            when (strategy) {
                is ToastStrategy.ReplaceAll -> replaceActive(toast)
                is ToastStrategy.Refresh -> refreshActive(duration, toast)
                is ToastStrategy.Enqueue -> {
                    queue.addLast(toast)
                    tryDequeue()
                }
                is ToastStrategy.Tagged.Refresh -> handleTagged(strategy.tag, strategy, duration, toast)
                is ToastStrategy.Tagged.Replace -> handleTagged(strategy.tag, strategy, duration, toast)
                is ToastStrategy.Tagged.Drop -> handleTagged(strategy.tag, strategy, duration, toast)
            }
        }
    }

    fun dismissAll() {
        synchronized(lock) {
            for (state in _active) state.remaining = Duration.ZERO
        }
    }

    internal fun tick(delta: Duration) {
        synchronized(lock) {
            for (state in _active) {
                state.remaining -= delta
            }
            _active.removeAll { it.remaining <= -EXIT_GRACE }
            tryDequeue()
        }
    }

    private fun replaceActive(toast: Toast) {
        queue.clear()
        for (state in _active) {
            state.remaining = Duration.ZERO
        }
        addToActive(toast)
    }

    private fun refreshActive(duration: Duration, toast: Toast) {
        if (_active.isEmpty()) {
            queue.clear()
            addToActive(toast)
            return
        }
        val state = _active.first()
        if (state.remaining <= Duration.ZERO) {
            addToActive(toast)
        } else {
            state.remaining = duration
            state.refreshCounter++
        }
    }

    private fun addToActive(toast: Toast) {
        _active.add(ToastState(toast, toast.duration))
    }

    private fun tryDequeue() {
        while (_active.size < maxVisible && queue.isNotEmpty()) {
            val toast = queue.removeFirst()
            _active.add(ToastState(toast, toast.duration))
        }
    }

    private fun handleTagged(
        tag: String,
        strategy: ToastStrategy.Tagged,
        duration: Duration,
        toast: Toast
    ) {
        when (strategy) {
            is ToastStrategy.Tagged.Refresh -> {
                val existing = _active.firstOrNull { it.toast.tag == tag }
                if (existing != null) {
                    if (existing.remaining <= Duration.ZERO) {
                        addToActive(toast)
                    } else {
                        existing.remaining = duration
                        existing.refreshCounter++
                    }
                } else {
                    val queued = queue.indexOfFirst { it.tag == tag }.takeIf { it >= 0 }
                    if (queued != null) {
                        queue[queued] = toast
                    } else {
                        reShow(duration, strategy.fallback, toast)
                    }
                }
            }

            is ToastStrategy.Tagged.Replace -> {
                val tagged = _active.filter { it.toast.tag == tag }
                queue.removeAll { it.tag == tag }
                if (tagged.isNotEmpty()) {
                    for (state in tagged) {
                        state.remaining = Duration.ZERO
                    }
                    addToActive(toast)
                } else {
                    reShow(duration, strategy.fallback, toast)
                }
            }

            is ToastStrategy.Tagged.Drop -> {
                if (_active.none { it.toast.tag == tag } && queue.none { it.tag == tag }) {
                    reShow(duration, strategy.fallback, toast)
                }
            }
        }
    }

    private fun reShow(
        duration: Duration,
        strategy: ToastStrategy,
        toast: Toast
    ) {
        when (strategy) {
            is ToastStrategy.ReplaceAll -> replaceActive(toast)
            is ToastStrategy.Refresh -> refreshActive(duration, toast)
            is ToastStrategy.Enqueue -> {
                queue.addLast(toast)
                tryDequeue()
            }
            is ToastStrategy.Tagged -> handleTagged(strategy.tag, strategy, duration, toast)
        }
    }

    class ToastState(
        toast: Toast,
        remaining: Duration
    ) {
        var toast by mutableStateOf(toast)
        var remaining by mutableStateOf(remaining)
        var refreshCounter by mutableIntStateOf(0)
    }
}
