package moe.forpleuvoir.ibukigourd.gui.base.modifier.widget

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget

fun Modifier.tick(action: IGWidget.() -> Unit) = this then WidgetModifier { element ->
    element.tick = { element.action() }
}

fun Modifier.mouseEnter(action: IGWidget.(MouseEnterEvent) -> Unit) = this then WidgetModifier { element ->
    element.mouseEnter = { element.action(it) }
}

fun Modifier.mouseLeave(action: IGWidget.(MouseLeaveEvent) -> Unit) = this then WidgetModifier { element ->
    element.mouseLeave = { element.action(it) }
}

fun Modifier.mousePress(action: IGWidget.(MousePressEvent) -> Unit) = this then WidgetModifier { element ->
    element.mousePress = { element.action(it) }
}

fun Modifier.mouseRelease(action: IGWidget.(MouseReleaseEvent) -> Unit) = this then WidgetModifier { element ->
    element.mouseRelease = { element.action(it) }
}

fun Modifier.mouseDragged(action: IGWidget.(MouseDragEvent) -> Unit) = this then WidgetModifier { element ->
    element.mouseDragging = { element.action(it) }
}

fun Modifier.mouseScrolling(action: IGWidget.(MouseScrollEvent) -> Unit) = this then WidgetModifier { element ->
    element.mouseScrolling = { element.action(it) }
}

fun Modifier.focused(action: IGWidget.(FocusedEvent) -> Unit) = this then WidgetModifier { element ->
    element.focused = { element.action(it) }
}

fun Modifier.keyPress(action: IGWidget.(KeyPressEvent) -> Unit) = this then WidgetModifier { element ->
    element.keyPress = { element.action(it) }
}

fun Modifier.keyRelease(action: IGWidget.(KeyReleaseEvent) -> Unit) = this then WidgetModifier { element ->
    element.keyRelease = { element.action(it) }
}

fun Modifier.charTyped(action: IGWidget.(CharTypedEvent) -> Unit) = this then WidgetModifier { widget ->
    widget.charTyped = { widget.action(it) }
}