package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.RowLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.ColumnLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.RowLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.Compose
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetContainerImpl

//------------ Row ------------\\

class RowWidget(
    override val arrangement: Arrangement.Horizontal,
    override val alignment: Alignment.Vertical
) : GuiWidgetContainerImpl(), RowLayout

fun interface RowScope : GuiScope<GuiWidgetContainer>, RowLayoutScope

fun ContainerScope.Row(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: RowScope.() -> Unit
): RowWidget = addWidgetChild(RowWidget(horizontalArrangement, verticalAlignment)) {
    modifier.foldInApply()
    RowScope { this }.Compose(content)
}

//------------ Column ------------\\

class ColumnWidget(
    override val arrangement: Arrangement.Vertical,
    override val alignment: Alignment.Horizontal,
) : GuiWidgetContainerImpl(), ColumnLayout

fun interface ColumnScope : GuiScope<GuiWidgetContainer>, ColumnLayoutScope

fun ContainerScope.Column(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: ColumnScope.() -> Unit
) = addWidgetChild(ColumnWidget(verticalArrangement, horizontalAlignment)) {
    modifier.foldInApply()
    ColumnScope { this }.Compose(content)
}