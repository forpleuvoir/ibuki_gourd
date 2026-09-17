package moe.forpleuvoir.ibukigourd.ui.sokitsu.toast

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import kotlin.time.Duration

/**
 * 提示服务的唯一入口：持有队列与活动列表，按 [ToastStrategy] 处置冲突。
 *
 * **时间推进由外部驱动**（[tick]）——提示本身不依赖 Compose 的帧时钟，因此没有屏幕、
 * 没有 Compose 内容时也能正常计时。[ToastHost] 每帧调用一次 [tick]。
 *
 * 活动条目在 `remaining` 扣到 `-exitGrace` 之后才移出列表：退场动画期间条目必须存活，
 * 否则 [ToastContainer] 的 `AnimatedVisibility` 会随条目消失被直接撤组合，动画不可见。
 * `exitGrace` 取主题 meta 的出场动画时长，两者默认同源。
 *
 * 线程约束：状态修改集中在 `synchronized(lock)` 内；活动列表是 snapshot state list，
 * 渲染线程读取、任意线程写入（与 Compose 的 snapshot 机制配合）。
 */
object ToastHandler {

    private val lock = Any()

    private val queue = ArrayDeque<Toast>()

    private val _active = mutableStateListOf<ToastState>()

    /** 当前展示中的提示（按展示顺序）。 */
    val active: List<ToastState> get() = _active

    /** 同时可见的条数上限，取自 [IGConfig.Gui.Toast.maxVisible]。 */
    private val maxVisible: Int get() = IGConfig.Gui.Toast.maxVisible

    /** 退场宽限期 = 主题 meta 的出场动画时长。 */
    private val exitGrace: Duration get() = ToastDefaults.exitDuration

    /** 是否既无活动提示也无排队提示 —— 宿主据此跳过整帧的渲染开销。 */
    val isIdle: Boolean get() = synchronized(lock) { _active.isEmpty() && queue.isEmpty() }

    /**
     * 展示一段带标准外观的提示：面板容器（主题面板精灵 + 内容色）+ 倒计时条由
     * [ToastContent] 提供，[content] 只负责面板内部的内容。
     */
    fun showContent(
        duration: Duration = IGConfig.Gui.Toast.duration,
        strategy: ToastStrategy = ToastStrategy.ReplaceAll,
        animation: ToastAnimation? = null,
        modifier: Modifier = Modifier,
        contentAlignment: Alignment = Alignment.Center,
        propagateMinConstraints: Boolean = true,
        content: @Composable BoxScope.() -> Unit
    ) {
        show(duration = duration, strategy = strategy, animation = animation) {
            ToastContent(
                modifier = modifier,
                contentAlignment = contentAlignment,
                propagateMinConstraints = propagateMinConstraints,
                content = content,
            )
        }
    }

    /**
     * 展示一段完全自定义的提示（外观、内边距全由 [content] 决定，不加面板容器）。
     *
     * @param duration 展示时长；`<= Duration.ZERO` 表示不自动消失
     * @param strategy 与已有提示的冲突处置策略
     * @param animation 本条提示的进出场动画；null = 主题 meta 组装的默认动画
     */
    fun show(
        duration: Duration = IGConfig.Gui.Toast.duration,
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
                is ToastStrategy.ReplaceAll     -> replaceActive(toast)
                is ToastStrategy.Refresh        -> refreshActive(duration, toast)
                is ToastStrategy.Enqueue        -> {
                    queue.addLast(toast)
                    tryDequeue()
                }

                is ToastStrategy.Tagged.Refresh -> handleTagged(strategy.tag, strategy, duration, toast)
                is ToastStrategy.Tagged.Replace -> handleTagged(strategy.tag, strategy, duration, toast)
                is ToastStrategy.Tagged.Drop    -> handleTagged(strategy.tag, strategy, duration, toast)
            }
        }
    }

    /** 让所有活动提示立即过期（退场动画照常播放）。 */
    fun dismissAll() {
        synchronized(lock) {
            for (state in _active) state.remaining = Duration.ZERO
            queue.clear()
        }
    }

    /**
     * 推进时间：扣减各活动条目的剩余时长，移除已过宽限期的条目，并按需从队列补充。
     *
     * 由 [ToastHost] 每帧驱动，`delta` 为距上一帧的时长。
     */
    internal fun tick(delta: Duration) {
        synchronized(lock) {
            for (state in _active) {
                state.remaining -= delta
            }
            _active.removeAll { it.remaining <= -exitGrace }
            tryDequeue()
        }
    }

    /**
     * 展示时长 → 运行时剩余时长。
     *
     * `duration <= ZERO` 的提示不自动消失，剩余时长取 [Duration.INFINITE]（扣减后仍为
     * INFINITE），这样"可见性 = `remaining > ZERO`"这一条判据对常驻提示同样成立
     * —— 否则它的剩余时长恒为 0，会从一开始就被判定为"该退场"而不展示。
     * 常驻提示只能被策略替换或 [dismissAll]（把剩余时长置 0）终止。
     */
    private fun remainingFor(duration: Duration): Duration =
        if (duration > Duration.ZERO) duration else Duration.INFINITE

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
            state.remaining = remainingFor(duration)
            state.refreshCounter++
        }
    }

    private fun addToActive(toast: Toast) {
        _active.add(ToastState(toast, remainingFor(toast.duration)))
    }

    private fun tryDequeue() {
        while (_active.size < maxVisible && queue.isNotEmpty()) {
            addToActive(queue.removeFirst())
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
                        existing.remaining = remainingFor(duration)
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

            is ToastStrategy.Tagged.Drop    -> {
                if (_active.none { it.toast.tag == tag } && queue.none { it.tag == tag }) {
                    reShow(duration, strategy.fallback, toast)
                }
            }
        }
    }

    /** 标识策略未命中时按 [strategy] 回落处置（[ToastStrategy.Tagged] 会递归解析其 fallback）。 */
    private fun reShow(
        duration: Duration,
        strategy: ToastStrategy,
        toast: Toast
    ) {
        when (strategy) {
            is ToastStrategy.ReplaceAll -> replaceActive(toast)
            is ToastStrategy.Refresh    -> refreshActive(duration, toast)
            is ToastStrategy.Enqueue    -> {
                queue.addLast(toast)
                tryDequeue()
            }

            is ToastStrategy.Tagged     -> handleTagged(strategy.tag, strategy, duration, toast)
        }
    }

    /**
     * 一条活动提示的运行时状态。
     *
     * 三个字段都是 snapshot state（驱动 [ToastContainer] 重组）：
     * - [toast]：条目内容（`Tagged.Refresh` 原地替换时可变）
     * - [remaining]：剩余展示时长，由 [tick] 每帧扣减；`<= ZERO` 时进入退场。
     *   不自动消失的提示恒为 [Duration.INFINITE]，见 [remainingFor]
     * - [refreshCounter]：被原地刷新的次数，用作倒计时条 / 内容动画的重启键
     */
    class ToastState(
        toast: Toast,
        remaining: Duration
    ) {
        var toast by mutableStateOf(toast)
        var remaining by mutableStateOf(remaining)
        var refreshCounter by mutableIntStateOf(0)
    }
}
