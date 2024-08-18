package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.Bias
import moe.forpleuvoir.ibukigourd.gui.base.layout.RowLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.LinearLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

class RowWidget(
    override val arrangement: Arrangement = Arrangement.Center,
    override var bias: Bias = Bias.of()
) : WidgetContainerImpl(), RowLayout {


    companion object

    fun interface RowScope : GuiScope<RowWidget>, LinearLayoutScope

}

typealias RowScope = RowWidget.RowScope

fun GuiScope<out WidgetContainer>.row(
    arrangement: Arrangement = Arrangement.Center,
    bias: Bias = Bias.of(),
    modifier: Modifier = Modifier,
    content: RowScope.() -> Unit
) = addWidgetChild(RowWidget(arrangement, bias)) {
    RowScope { this }.content()
    modifier.foldInApply()
}