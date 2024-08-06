package moe.forpleuvoir.ibukigourd.gui.base.modifier.widget

import moe.forpleuvoir.ibukigourd.gui.base.element.IGElement
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier

fun interface ElementModifier : Modifier.Element {
    fun applyModify(element: IGElement)

    override fun tryApplyModify(target: Any) {
        if (target is IGElement) applyModify(target)
    }

}

fun Modifier.tick(action: IGElement.() -> Unit) = this then ElementModifier { element ->
    element.tick = { element.action() }
}

fun Modifier.mouseEnter(action: IGElement.(MouseEnterEvent) -> Unit) = this then ElementModifier { element ->
    element.mouseEnter = { element.action(it) }
}

fun Modifier.mouseLeave(action: IGElement.(MouseLeaveEvent) -> Unit) = this then ElementModifier { element ->
    element.mouseLeave = { element.action(it) }
}

fun Modifier.mousePress(action: IGElement.(MousePressEvent) -> Unit) = this then ElementModifier { element ->
    element.mousePress = { element.action(it) }
}

fun Modifier.mouseRelease(action: IGElement.(MouseReleaseEvent) -> Unit) = this then ElementModifier { element ->
    element.mouseRelease = { element.action(it) }
}

fun Modifier.mouseDragged(action: IGElement.(MouseDragEvent) -> Unit) = this then ElementModifier { element ->
    element.mouseDragging = { element.action(it) }
}

fun Modifier.mouseScrolling(action: IGElement.(MouseScrollEvent) -> Unit) = this then ElementModifier { element ->
    element.mouseScrolling = { element.action(it) }
}

fun Modifier.focused(action: IGElement.(FocusedEvent) -> Unit) = this then ElementModifier { element ->
    element.focused = { element.action(it) }
}

fun Modifier.keyPress(action: IGElement.(KeyPressEvent) -> Unit) = this then ElementModifier { element ->
    element.keyPress = { element.action(it) }
}

fun Modifier.keyRelease(action: IGElement.(KeyReleaseEvent) -> Unit) = this then ElementModifier { element ->
    element.keyRelease = { element.action(it) }
}

fun Modifier.charTyped(action: IGElement.(CharTypedEvent) -> Unit) = this then ElementModifier { element ->
    element.charTyped = { element.action(it) }
}