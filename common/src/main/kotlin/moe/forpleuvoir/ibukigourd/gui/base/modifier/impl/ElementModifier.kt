package moe.forpleuvoir.ibukigourd.gui.base.modifier.impl

import moe.forpleuvoir.ibukigourd.gui.base.element.GuiElementUserData.setName
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidget


fun Modifier.data(key: String, value: Any) = this then WidgetModifier {
    it.userData[key] = value
}

fun Modifier.name(name: String) = this then WidgetModifier {
    it.setName(name)
}

fun Modifier.tick(action: GuiWidget.() -> Unit) = this then WidgetModifier { element ->
    element.tick = { element.action() }
}

fun Modifier.process(action: GuiWidget.(Float) -> Unit) = this then WidgetModifier { element ->
    element.process = { element.action(it) }
}

fun Modifier.mouseEnter(action: GuiWidget.(MouseEnterEvent) -> Unit) = this then WidgetModifier { element ->
    element.mouseEnter = { element.action(it) }
}

fun Modifier.mouseLeave(action: GuiWidget.(MouseLeaveEvent) -> Unit) = this then WidgetModifier { element ->
    element.mouseLeave = { element.action(it) }
}

fun Modifier.mousePress(action: GuiWidget.(MousePressEvent) -> Unit) = this then WidgetModifier { element ->
    element.mousePress = { element.action(it) }
}

fun Modifier.mouseRelease(action: GuiWidget.(MouseReleaseEvent) -> Unit) = this then WidgetModifier { element ->
    element.mouseRelease = { element.action(it) }
}

fun Modifier.mouseDragged(action: GuiWidget.(MouseDragEvent) -> Unit) = this then WidgetModifier { element ->
    element.mouseDragging = { element.action(it) }
}

fun Modifier.mouseScrolling(action: GuiWidget.(MouseScrollEvent) -> Unit) = this then WidgetModifier { element ->
    element.mouseScrolling = { element.action(it) }
}

fun Modifier.focused(action: GuiWidget.(FocusedEvent) -> Unit) = this then WidgetModifier { element ->
    element.focused = { element.action(it) }
}

fun Modifier.keyPress(action: GuiWidget.(KeyPressEvent) -> Unit) = this then WidgetModifier { element ->
    element.keyPress = { element.action(it) }
}

fun Modifier.keyRelease(action: GuiWidget.(KeyReleaseEvent) -> Unit) = this then WidgetModifier { element ->
    element.keyRelease = { element.action(it) }
}

fun Modifier.charTyped(action: GuiWidget.(CharTypedEvent) -> Unit) = this then WidgetModifier { widget ->
    widget.charTyped = { widget.action(it) }
}