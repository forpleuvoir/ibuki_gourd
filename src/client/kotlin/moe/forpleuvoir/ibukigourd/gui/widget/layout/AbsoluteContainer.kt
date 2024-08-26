package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.AbsoluteLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.AbsoluteLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

class AbsoluteWidget : WidgetContainerImpl(), AbsoluteLayout {
    fun interface Scope : GuiScope<AbsoluteWidget>, AbsoluteLayoutScope
}

typealias AbsoluteScope = AbsoluteWidget.Scope

fun WidgetContainerScope.Absolute(
    modifier: Modifier = Modifier,
    context: AbsoluteScope.() -> Unit
): AbsoluteWidget = owner().addWidgetChild(AbsoluteWidget()) {
    AbsoluteScope { this }.context()
    modifier.foldInApply()
}


