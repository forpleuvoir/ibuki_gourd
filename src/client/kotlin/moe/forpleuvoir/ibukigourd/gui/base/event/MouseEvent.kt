package moe.forpleuvoir.ibukigourd.gui.base.event

import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.input.MousePosition
import moe.forpleuvoir.ibukigourd.render.math.Vector2f

sealed class MouseEvent(
    val x: Float,
    val y: Float
) : GUIEvent() {

    val position: MousePosition get() = MousePosition(x, y)

    operator fun component1() = x

    operator fun component2() = y

}

class MousePressEvent(
    x: Float,
    y: Float,
    val button: Mouse,
) : MouseEvent(x, y) {

    operator fun component3() = button

}

class MouseReleaseEvent(
    x: Float,
    y: Float,
    val button: Mouse,
) : MouseEvent(x, y) {

    operator fun component3() = button

}

class MouseScrollEvent(
    x: Float,
    y: Float,
    val verticalAmount: Float,
    val horizontalAmount: Float,
) : MouseEvent(x, y) {
    operator fun component3() = verticalAmount
    operator fun component4() = horizontalAmount

}

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

    val delta get() = Vector2f(deltaX, deltaY)

    operator fun component3() = button

    operator fun component4() = deltaX

    operator fun component5() = deltaY

    operator fun component6() = delta
}

class MouseEnterEvent(
    x: Float,
    y: Float,
) : MouseEvent(x, y)

class MouseLeaveEvent(
    x: Float,
    y: Float,
) : MouseEvent(x, y)