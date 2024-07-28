package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.BoxAlignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.LinearLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

class ColumnWidget(
    override val alignment: (Orientation) -> Alignment = BoxAlignment::Center
) : WidgetContainerImpl(), ColumnLayout

data class ColumScope(private val column: ColumnWidget) : GuiScope<ColumnWidget>, LinearLayoutScope {
    override fun owner(): ColumnWidget = column

}

fun GuiScope<out WidgetContainer>.column(
    alignment: (Orientation) -> Alignment = BoxAlignment::Center,
    modifier: Modifier? = null,
    content: ColumScope.() -> Unit
) = owner().addWidgetChild(
    ColumnWidget(alignment).apply {
        ColumScope(this).content()
        modifier?.foldIn(Unit) { _, e -> e.tryApplyModify(this) }
    }
)