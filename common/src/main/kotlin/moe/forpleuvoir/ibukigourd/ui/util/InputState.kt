package moe.forpleuvoir.ibukigourd.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import moe.forpleuvoir.ibukigourd.event.events.client.input.KeyboardEvent
import moe.forpleuvoir.ibukigourd.event.events.client.input.MouseEvent
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.nebula.event.invoke

/** 快速动作键（[IGConfig.Gui.quickActionKeyCode]）当前是否按下。 */
val isQuickAction: Boolean get() = InputHandler.wasKeyPressed(IGConfig.Gui.quickActionKeyCode)

/** 隐藏动作键（[IGConfig.Gui.hideActionKeyCode]）当前是否按下。 */
val isHideAction: Boolean get() = InputHandler.wasKeyPressed(IGConfig.Gui.hideActionKeyCode)

/**
 * 当前按下的按键集合，按**按下顺序**维护（组合键显示与捕获用）。
 *
 * 只由键盘 / 鼠标事件回调写入：事件在 [InputHandler] 增删按下集合之前广播，
 * 因此键码取事件自身携带的值，不查 [InputHandler]。
 */
@Stable
class PressedKeysState internal constructor() {

    /** 保序 + 去重：按住重复触发时同一个键不会重复入列，释放一次即完全移除。 */
    private val ordered = LinkedHashSet<KeyCode>()

    /** 当前按下的按键，按按下顺序排列。 */
    var keys by mutableStateOf<List<KeyCode>>(emptyList())
        private set

    internal fun press(keyCode: KeyCode) {
        if (ordered.add(keyCode)) keys = ordered.toList()
    }

    internal fun release(keyCode: KeyCode) {
        if (ordered.remove(keyCode)) keys = ordered.toList()
    }
}

/** 订阅键盘 / 鼠标事件维护的「当前按下按键」，组件销毁时自动注销。 */
@Composable
fun rememberPressedKeys(): PressedKeysState {
    val state = remember { PressedKeysState() }

    DisposableEffect(state) {
        val onKeyPress = KeyboardEvent.Pressed.register { state.press(it.keyCode) }
        val onKeyRelease = KeyboardEvent.Released.register { state.release(it.keyCode) }
        val onMousePress = MouseEvent.Pressed.register { state.press(it.keyCode) }
        val onMouseRelease = MouseEvent.Released.register { state.release(it.keyCode) }

        onDispose {
            onKeyPress()
            onKeyRelease()
            onMousePress()
            onMouseRelease()
        }
    }

    return state
}

/**
 * 按住 [keyCode] 期间为 `true` 的组合状态，键盘与鼠标键码通用。
 */
@Composable
fun rememberKeyPressedState(keyCode: KeyCode): Boolean = rememberPressedKeys().keys.contains(keyCode)

/** 按住 [IGConfig.Gui.hideActionKeyCode] 期间为 `true`。 */
@Composable
fun rememberHideActionState(): Boolean = rememberKeyPressedState(IGConfig.Gui.hideActionKeyCode)

/** 按住 [IGConfig.Gui.quickActionKeyCode] 期间为 `true`。 */
@Composable
fun rememberQuickActionState(): Boolean = rememberKeyPressedState(IGConfig.Gui.quickActionKeyCode)

/**
 * 组合键捕获：启用期间累计按下的按键（按按下顺序），**全部松开**时把结果交回 [onCaptured]。
 *
 * 捕获全程由事件回调驱动 —— 不在组合期写状态、不启动逐帧轮询：
 * - 捕获期间每个按键 / 鼠标事件都 `cancel`，不落到游戏与其它监听器（含按住的重复触发）；
 * - 按 `Esc` 或 `Ctrl + Backspace` 取消本次捕获（回调 [onCancelled]）；
 * - 返回值是本次会话已按下的按键，供调用方显示"正在按什么"。
 *
 * @param enabled 是否处于捕获状态
 * @param onCaptured 捕获完成（全部松开）时回调，参数为按按下顺序排列的按键
 * @param onCancelled 取消捕获时回调
 */
@Composable
fun rememberKeyCapture(
    enabled: Boolean,
    onCaptured: (List<KeyCode>) -> Unit,
    onCancelled: () -> Unit = {},
): List<KeyCode> {
    var displayed by remember(enabled) { mutableStateOf<List<KeyCode>>(emptyList()) }
    val capturedHandler = rememberUpdatedState(onCaptured)
    val cancelledHandler = rememberUpdatedState(onCancelled)

    DisposableEffect(enabled) {
        if (!enabled) return@DisposableEffect onDispose { }

        // 保序 + 去重：按住不放的重复触发不会重复入列，否则显示会堆一长串、释放也删不干净
        val captured = LinkedHashSet<KeyCode>()
        val pressed = LinkedHashSet<KeyCode>()
        var cancelled = false

        fun reset() {
            captured.clear()
            pressed.clear()
            displayed = emptyList()
        }

        val press: (KeyCode) -> Unit = { keyCode ->
            when {
                keyCode == Keyboard.ESCAPE -> {
                    cancelled = true
                    reset()
                    cancelledHandler.value()
                }

                keyCode == Keyboard.BACKSPACE && Keyboard.LEFT_CONTROL in pressed -> {
                    cancelled = true
                    reset()
                    cancelledHandler.value()
                }

                else -> {
                    pressed.add(keyCode)
                    if (captured.add(keyCode)) displayed = captured.toList()
                }
            }
        }

        val release: (KeyCode) -> Unit = { keyCode ->
            pressed.remove(keyCode)
            if (pressed.isEmpty() && !cancelled && captured.isNotEmpty()) {
                val result = captured.toList()
                reset()
                capturedHandler.value(result)
            }
        }

        val onKeyPress = KeyboardEvent.Pressed.register { context ->
            context.cancel()
            // 按住的重复触发不再入列（去重之外再挡一层，避免无谓的状态写入）
            if (!context.isRepeat) press(context.keyCode)
        }
        val onKeyRelease = KeyboardEvent.Released.register { context ->
            context.cancel()
            release(context.keyCode)
        }
        val onMousePress = MouseEvent.Pressed.register { context ->
            context.cancel()
            press(context.keyCode)
        }
        val onMouseRelease = MouseEvent.Released.register { context ->
            context.cancel()
            release(context.keyCode)
        }

        onDispose {
            onKeyPress()
            onKeyRelease()
            onMousePress()
            onMouseRelease()
        }
    }

    return displayed
}
