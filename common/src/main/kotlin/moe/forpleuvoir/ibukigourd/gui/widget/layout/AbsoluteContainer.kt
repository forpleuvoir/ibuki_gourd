package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.AbsoluteLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.scope.AbsoluteLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.Compose
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetContainerImpl
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.scaledSize
import moe.forpleuvoir.ibukigourd.util.size

class AbsoluteWidget : GuiWidgetContainerImpl(), AbsoluteLayout {

    override val interactableBox: Box
        get() {
            val parent = parent()
            return if (parent is GuiWidget) {
                parent.interactableBox
            } else {
                screen()?.interactableBox ?: Box(0f, 0f, mc.window.scaledSize)
            }
        }

    override val interactableContentBox: Box
        get() {
            val parent = parent()
            return if (parent is GuiWidget) {
                parent.interactableContentBox
            } else {
                screen()?.interactableContentBox ?: Box(0f, 0f, mc.window.scaledSize)
            }
        }

}

fun interface AbsoluteScope : GuiScope<AbsoluteWidget>, AbsoluteLayoutScope

fun ContainerScope.Absolute(
    modifier: Modifier = Modifier,
    context: AbsoluteScope.() -> Unit
): AbsoluteWidget = owner().addWidgetChild(AbsoluteWidget()) {
    modifier.foldInApply()
    AbsoluteScope { this }.Compose(context)
}


