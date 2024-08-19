package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.ColumnLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

class ColumnWidget(
    override val arrangement: Arrangement.Horizontal,
    override val alignment: Alignment.Vertical
) : WidgetContainerImpl(), ColumnLayout

fun interface ColumnScope : GuiScope<ColumnWidget>, ColumnLayoutScope

fun WidgetContainerScope.Column(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: ColumnScope.() -> Unit
): ColumnWidget = addWidgetChild(ColumnWidget(horizontalArrangement, verticalAlignment)) {
    ColumnScope { this }.content()
    modifier.foldInApply()
}