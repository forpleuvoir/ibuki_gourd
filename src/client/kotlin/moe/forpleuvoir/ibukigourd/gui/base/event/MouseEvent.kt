package moe.forpleuvoir.ibukigourd.gui.base.event

import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.input.MousePosition

sealed class MouseEvent(
    val x: Float,
    val y: Float
) : GUIEvent() {

    val position: MousePosition get() = MousePosition(x, y)

}

class MousePressEvent(
    x: Float,
    y: Float,
    val button: Mouse,
) : MouseEvent(x, y)

class MouseReleaseEvent(
    x: Float,
    y: Float,
    val button: Mouse,
) : MouseEvent(x, y)

class MouseScrollEvent(
    x: Float,
    y: Float,
    val amount: Float,
) : MouseEvent(x, y)

class MouseMoveEvent(
    x: Float,
    y: Float,
) : MouseEvent(x, y)

class MouseDragEvent(
    x: Float,
    y: Float,
    val button: Mouse,
    val deltaX: Float,
    val deltaY: Float,
) : MouseEvent(x, y) {
    val deltaPosition: MousePosition get() = MousePosition(deltaX, deltaY)
}

class MouseEnterEvent(
    x: Float,
    y: Float,
) : MouseEvent(x, y)

class MouseLeaveEvent(
    x: Float,
    y: Float,
) : MouseEvent(x, y)