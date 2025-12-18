package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.AbsoluteLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.AbsoluteLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.Compose
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetContainerImpl

class AbsoluteWidget : GuiWidgetContainerImpl(), AbsoluteLayout

fun interface AbsoluteScope : GuiScope<AbsoluteWidget>, AbsoluteLayoutScope

fun ContainerScope.Absolute(
    modifier: Modifier = Modifier,
    context: AbsoluteScope.() -> Unit
): AbsoluteWidget = owner().addWidgetChild(AbsoluteWidget()) {
    modifier.foldInApply()
    AbsoluteScope { this }.Compose(context)
}


