package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.RowLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.LinearLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

class RowWidget(
    override val arrangement: Arrangement = Arrangement.Center
) : WidgetContainerImpl(), RowLayout {

    override var spacing: Float = 0f

    companion object

    fun interface RowScope : GuiScope<RowWidget>, LinearLayoutScope {

        override val linearLayout: LinearLayout
            get() = owner()

    }

}

typealias RowScope = RowWidget.RowScope

fun GuiScope<out WidgetContainer>.row(
    arrangement: Arrangement = Arrangement.Center,
    modifier: Modifier = Modifier,
    content: RowScope.() -> Unit
) = addWidgetChild(RowWidget(arrangement)) {
    RowScope { this }.content()
    modifier.foldInApply()
}