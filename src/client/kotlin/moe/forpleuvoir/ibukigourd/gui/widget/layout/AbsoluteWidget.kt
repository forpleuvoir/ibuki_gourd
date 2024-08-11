package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.AbsoluteLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.AbsoluteLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl


class AbsoluteWidget : WidgetContainerImpl(), AbsoluteLayout {

    companion object

    fun interface AbsoluteScope : GuiScope<AbsoluteWidget>, AbsoluteLayoutScope

}

typealias AbsoluteScope = AbsoluteWidget.AbsoluteScope


fun GuiScope<out WidgetContainer>.absolute(
    modifier: Modifier = Modifier,
    context: AbsoluteWidget.AbsoluteScope.() -> Unit = { }
): AbsoluteWidget = owner().addWidgetChild(AbsoluteWidget()) {
    AbsoluteScope { this }.context()
    modifier.foldInApply()
}


