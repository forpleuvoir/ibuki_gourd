package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.RowLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.RowLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

class RowWidget(
    override val arrangement: Arrangement.Vertical,
    override val alignment: Alignment.Horizontal,
) : WidgetContainerImpl(), RowLayout

fun interface RowScope : GuiScope<RowWidget>, RowLayoutScope

fun WidgetContainerScope.Row(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: RowScope.() -> Unit
) = addWidgetChild(RowWidget(verticalArrangement, horizontalAlignment)) {
    RowScope { this }.content()
    modifier.foldInApply()
}