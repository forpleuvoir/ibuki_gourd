package moe.forpleuvoir.ibukigourd.ui.overlay

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration

/** 一次注册的撤销句柄；重复调用无副作用。 */
fun interface OverlayHandle {
    fun unregister()
}

/**
 * 一条覆盖层内容。
 *
 * @param key 条目键，同一 key 重复注册会替换旧条目
 * @param present 本帧是否需要渲染；返回 `false` 时该条目连组合都不参与
 */
class OverlayEntry internal constructor(
    val key: Any,
    internal val token: Any,
    internal val present: () -> Boolean,
    internal val content: @Composable BoxScope.() -> Unit,
)

/**
 * 全局覆盖层注册表：内容是画在原版 GUI（HUD / 界面）与 Compose 屏幕**之上**的常驻内容，
 * 由 [OverlayHost] 的常驻场景渲染，不隶属于任何屏幕。
 *
 * 条目是 snapshot state：任意线程都可注册 / 注销，变化在下一次场景组合时生效。
 * 条目按注册顺序组合，先注册的在底层。
 */
object OverlayService {

    /** 当前注册的覆盖层内容（按注册顺序）。 */
    internal val activeEntries: List<OverlayEntry> field = mutableStateListOf()

    private val tokenCounter = AtomicLong()

    private val frameListeners = CopyOnWriteArrayList<FrameListener>()

    private class FrameListener(val token: Any, val callback: (Duration) -> Unit)

    private class IdentityKey(val token: Any) {
        override fun equals(other: Any?): Boolean = other is IdentityKey && token == other.token

        override fun hashCode(): Int = token.hashCode()
    }

    /**
     * 注册一段覆盖层内容。
     *
     * @param key 条目键；已存在同 key 条目时替换它（旧句柄随之失效）
     * @param present 是否参与渲染，缺省恒为 `true`
     * @param content 内容，作用域是铺满窗口的 [BoxScope]
     */
    fun register(
        key: Any,
        present: () -> Boolean = { true },
        content: @Composable BoxScope.() -> Unit,
    ): OverlayHandle {
        val token = tokenCounter.incrementAndGet()
        val entry = OverlayEntry(key, token, present, content)
        val index = activeEntries.indexOfFirst { it.key == key }
        if (index >= 0) activeEntries[index] = entry else activeEntries.add(entry)
        return OverlayHandle { unregisterByToken(key, token) }
    }

    /**
     * 注册一段覆盖层内容，条目键自动分配（不与其它条目冲突）。
     *
     * @param present 是否参与渲染，缺省恒为 `true`
     * @param content 内容，作用域是铺满窗口的 [BoxScope]
     */
    fun register(
        present: () -> Boolean = { true },
        content: @Composable BoxScope.() -> Unit,
    ): OverlayHandle {
        val key = IdentityKey(tokenCounter.incrementAndGet())
        return register(key, present, content)
    }

    /** 注销指定键的条目；返回是否真的移除了条目。 */
    fun unregister(key: Any): Boolean = activeEntries.removeAll { it.key == key }

    /** 指定键的条目是否已注册。 */
    fun contains(key: Any): Boolean = activeEntries.any { it.key == key }

    /** 注销全部覆盖层内容（不含帧回调）。 */
    fun clear() {
        activeEntries.clear()
    }

    /**
     * 注册每帧回调，用于推进注册内容自己的计时（如提示倒计时）。
     *
     * 回调在渲染线程、于本帧渲染之前调用，参数是本帧时长（长停顿已截断）。
     */
    fun registerFrameListener(callback: (Duration) -> Unit): OverlayHandle {
        val listener = FrameListener(tokenCounter.incrementAndGet(), callback)
        frameListeners.add(listener)
        return OverlayHandle { frameListeners.remove(listener) }
    }

    /** 是否有需要渲染的条目 —— 没有任何条目参与时整帧跳过组合与绘制。 */
    internal val hasPresentEntry: Boolean get() = activeEntries.any { it.present() }

    /** 推进全部帧回调。 */
    internal fun runFrameListeners(delta: Duration) {
        for (listener in frameListeners) listener.callback(delta)
    }

    private fun unregisterByToken(key: Any, token: Any) {
        activeEntries.removeAll { it.key == key && it.token == token }
    }
}
