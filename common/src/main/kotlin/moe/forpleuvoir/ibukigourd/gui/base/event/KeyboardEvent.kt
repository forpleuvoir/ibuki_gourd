package moe.forpleuvoir.ibukigourd.gui.base.event

import moe.forpleuvoir.ibukigourd.input.Keyboard

sealed class KeyboardEvent(
    val keyCode: Keyboard,
    val scanCode: Int,
    val modifiers: Int
) : GUIEvent()

class KeyPressEvent(
    keyCode: Keyboard,
    scanCode: Int,
    modifiers: Int
) : KeyboardEvent(keyCode, scanCode, modifiers)

class KeyReleaseEvent(
    keyCode: Keyboard,
    scanCode: Int,
    modifiers: Int
) : KeyboardEvent(keyCode, scanCode, modifiers)

