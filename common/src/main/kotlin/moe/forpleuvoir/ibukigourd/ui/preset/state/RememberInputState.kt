package moe.forpleuvoir.ibukigourd.ui.preset.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import moe.forpleuvoir.ibukigourd.event.events.client.input.KeyboardEvent
import moe.forpleuvoir.ibukigourd.event.events.client.input.MouseEvent
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.MouseButton
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig.Gui.hideActionKeyCode
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig.Gui.quickActionKeyCode
import moe.forpleuvoir.nebula.event.invoke


inline val isQuickAction: Boolean get() = InputHandler.wasKeyPressed(quickActionKeyCode)

@Composable
fun rememberQuickActionState(): Boolean = when (quickActionKeyCode) {
    is Keyboard    -> rememberKeyboardState(quickActionKeyCode as Keyboard)
    is MouseButton -> rememberMouseButtonState(quickActionKeyCode as MouseButton)
}

@Composable
fun rememberHideActionState(): Boolean = when (hideActionKeyCode) {
    is Keyboard    -> rememberKeyboardState(hideActionKeyCode as Keyboard)
    is MouseButton -> rememberMouseButtonState(hideActionKeyCode as MouseButton)
}

@Composable
fun rememberKeyboardState(keycode: Keyboard): Boolean {
    val pressed = remember { mutableStateOf(false) }

    DisposableEffect(keycode) {
        val press = KeyboardEvent.Pressed.register {
            if (it.keyCode == keycode) {
                pressed.value = true
            }
        }

        val release = KeyboardEvent.Released.register {
            if (it.keyCode == keycode) {
                pressed.value = false
            }
        }

        onDispose {
            press()
            release()
        }
    }

    return pressed.value
}


@Composable
fun rememberMouseButtonState(button: MouseButton): Boolean {
    val pressed = remember { mutableStateOf(false) }

    DisposableEffect(button) {
        val press = MouseEvent.Pressed.register {
            if (it.keyCode == button) {
                pressed.value = true
            }
        }

        val release = MouseEvent.Released.register {
            if (it.keyCode == button) {
                pressed.value = false
            }
        }

        onDispose {
            press()
            release()
        }
    }

    return pressed.value
}