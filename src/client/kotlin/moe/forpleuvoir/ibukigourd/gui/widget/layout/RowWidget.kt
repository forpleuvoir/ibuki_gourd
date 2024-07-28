package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.RowLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.BoxAlignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.LinearLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

class RowWidget(
    override val alignment: (Orientation) -> Alignment = BoxAlignment::Center
) : WidgetContainerImpl(), RowLayout

data class RowScope(private val row: RowWidget) : GuiScope<RowWidget>, LinearLayoutScope {
    override fun owner(): RowWidget = row

}

fun GuiScope<out WidgetContainer>.row(
    alignment: (Orientation) -> Alignment = BoxAlignment::Center,
    modifier: Modifier? = null,
    content: RowScope.() -> Unit
) = owner().addWidgetChild(
    RowWidget(alignment).apply {
        RowScope(this).content()
        modifier?.foldIn(Unit) { _, e -> e.tryApplyModify(this) }
    }
)