package moe.forpleuvoir.ibukigourd.ui.platformcontext

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize

class MinecraftWindowInfo : WindowInfo {
    private val _containerSize = mutableStateOf(IntSize.Zero)
    private val _containerDpSize = mutableStateOf(DpSize.Zero)

    override var isWindowFocused: Boolean by mutableStateOf(true)

    override var keyboardModifiers: PointerKeyboardModifiers
        get() = GlobalKeyboardModifiers.value
        set(value) {
            GlobalKeyboardModifiers.value = value
        }

    override var containerSize: IntSize
        get() = _containerSize.value
        set(value) {
            _containerSize.value = value
        }

    override var containerDpSize: DpSize
        get() = _containerDpSize.value
        set(value) {
            _containerDpSize.value = value
        }

    companion object {
        internal val GlobalKeyboardModifiers = mutableStateOf(PointerKeyboardModifiers())
    }
}