package moe.forpleuvoir.ibukigourd.gui.base.event

import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.input.MousePosition
import org.joml.Vector2d

sealed class MouseEvent(
    val x: Double,
    val y: Double
) : GUIEvent() {

    val position: MousePosition get() = MousePosition(x, y)

}

class MousePressEvent(
    x: Double,
    y: Double,
    val button: Mouse,
) : MouseEvent(x, y)

class MouseReleaseEvent(
    x: Double,
    y: Double,
    val button: Mouse,
) : MouseEvent(x, y)

class MouseScrollEvent(
    x: Double,
    y: Double,
    val horizontalAmount: Double,
    val verticalAmount: Double,
) : MouseEvent(x, y)

class MouseMoveEvent(
    x: Double,
    y: Double,
) : MouseEvent(x, y)

class MouseDragEvent(
    x: Double,
    y: Double,
    val button: Mouse,
    val deltaX: Double,
    val deltaY: Double,
) : MouseEvent(x, y) {
    val deltaPosition: MousePosition get() = MousePosition(deltaX, deltaY)

    val delta get() = Vector2d(deltaX, deltaY)
}

class MouseEnterEvent(
    x: Double,
    y: Double,
) : MouseEvent(x, y)

class MouseLeaveEvent(
    x: Double,
    y: Double,
) : MouseEvent(x, y)