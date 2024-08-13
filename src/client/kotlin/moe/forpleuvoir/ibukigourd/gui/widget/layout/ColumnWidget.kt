package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.LinearLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

class ColumnWidget(
    override val arrangement: Arrangement = Arrangement.Center
) : WidgetContainerImpl(), ColumnLayout {

    override var spacing: Float = 0f

    fun interface ColumnScope : GuiScope<ColumnWidget>, LinearLayoutScope {
        override val linearLayout: LinearLayout
            get() = owner()

    }

}

typealias ColumnScope = ColumnWidget.ColumnScope


fun GuiScope<out WidgetContainer>.column(
    arrangement: Arrangement = Arrangement.Center,
    modifier: Modifier = Modifier,
    content: ColumnScope.() -> Unit
) = addWidgetChild(ColumnWidget(arrangement)) {
    ColumnScope { this }.content()
    modifier.foldInApply()
}