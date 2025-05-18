package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.AbsoluteLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.AbsoluteLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.Compose
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

class AbsoluteWidget : WidgetContainerImpl(), AbsoluteLayout

fun interface AbsoluteScope : GuiScope<WidgetContainer>, AbsoluteLayoutScope

fun ContainerScope.Absolute(
    modifier: Modifier = Modifier,
    context: AbsoluteScope.() -> Unit
): AbsoluteWidget = owner().addWidgetChild(AbsoluteWidget()) {
    modifier.foldInApply()
    AbsoluteScope { this }.Compose(context)
}


