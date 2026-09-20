package moe.forpleuvoir.ibukigourd.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import moe.forpleuvoir.ibukigourd.event.events.client.input.KeyboardEvent
import moe.forpleuvoir.ibukigourd.event.events.client.input.MouseEvent
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.nebula.event.invoke

/** 快速动作键（[IGConfig.Gui.quickActionKeyCode]）当前是否按下。 */
val isQuickAction: Boolean get() = InputHandler.wasKeyPressed(IGConfig.Gui.quickActionKeyCode)

/** 隐藏动作键（[IGConfig.Gui.hideActionKeyCode]）当前是否按下。 */
val isHideAction: Boolean get() = InputHandler.wasKeyPressed(IGConfig.Gui.hideActionKeyCode)

/**
 * 按住 [keyCode] 期间为 `true` 的组合状态，键盘与鼠标键码通用。
 *
 * 按下 / 释放由 [KeyboardEvent] / [MouseEvent] 维护，键码取事件自身携带的值；
 * 事件在 [InputHandler] 增删按下集合之前广播，因此不查 [InputHandler]。
 */
@Composable
fun rememberKeyPressedState(keyCode: KeyCode): Boolean {
    val pressed = remember(keyCode) { mutableStateOf(false) }

    DisposableEffect(keyCode) {
        val onKeyPress = KeyboardEvent.Pressed.register { if (it.keyCode == keyCode) pressed.value = true }
        val onKeyRelease = KeyboardEvent.Released.register { if (it.keyCode == keyCode) pressed.value = false }
        val onMousePress = MouseEvent.Pressed.register { if (it.keyCode == keyCode) pressed.value = true }
        val onMouseRelease = MouseEvent.Released.register { if (it.keyCode == keyCode) pressed.value = false }

        onDispose {
            onKeyPress()
            onKeyRelease()
            onMousePress()
            onMouseRelease()
        }
    }

    return pressed.value
}

/** 按住 [IGConfig.Gui.hideActionKeyCode] 期间为 `true`。 */
@Composable
fun rememberHideActionState(): Boolean = rememberKeyPressedState(IGConfig.Gui.hideActionKeyCode)

/** 按住 [IGConfig.Gui.quickActionKeyCode] 期间为 `true`。 */
@Composable
fun rememberQuickActionState(): Boolean = rememberKeyPressedState(IGConfig.Gui.quickActionKeyCode)
