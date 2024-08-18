package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.WidgetModifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.LinearLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

abstract class ColumnWidget : WidgetContainerImpl(), ColumnLayout

fun interface ColumnScope : GuiScope<ColumnWidget>, LinearLayoutScope<Alignment.Vertical> {

    override fun Modifier.weight(weight: Int) = this then WidgetModifier {
        check(weight >= 0) { "weight must be >= 0" }
        when (val parentData = it.parentData) {
            is ColumnLayout.WrappedColumnLayoutData -> it.parentData = parentData.copy(weight = weight)
            null                                    -> it.parentData = ColumnLayout.WrappedColumnLayoutData(weight = weight)
        }
    }

    override fun Modifier.fill(fill: Boolean) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is ColumnLayout.WrappedColumnLayoutData -> it.parentData = parentData.copy(fill = fill)
            null                                    -> it.parentData = ColumnLayout.WrappedColumnLayoutData(fill = fill)
        }
    }

    override fun Modifier.align(alignment: Alignment.Vertical) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is ColumnLayout.WrappedColumnLayoutData -> it.parentData = parentData.copy(alignment = alignment)
            null                                    -> it.parentData = ColumnLayout.WrappedColumnLayoutData(alignment = alignment)
        }
    }

}

fun GuiScope<out WidgetContainer>.column(
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    modifier: Modifier = Modifier,
    content: ColumnScope.() -> Unit
): ColumnWidget = addWidgetChild(object : ColumnWidget() {
    override val arrangement: Arrangement.Horizontal = horizontalArrangement
    override val alignment: Alignment.Vertical = verticalAlignment
}) {
    ColumnScope { this }.content()
    modifier.foldInApply()
}