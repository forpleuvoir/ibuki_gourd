package moe.forpleuvoir.ibukigourd.gui.base.event

import moe.forpleuvoir.ibukigourd.input.KeyCode

sealed class KeyboardEvent(
    val keyCode: KeyCode
) :GUIEvent()

class KeyPressEvent(
    keyCode: KeyCode
) : KeyboardEvent(keyCode)

class KeyReleaseEvent(
    keyCode: KeyCode
) : KeyboardEvent(keyCode)


