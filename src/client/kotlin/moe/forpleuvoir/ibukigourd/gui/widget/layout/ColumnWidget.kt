package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.PlanarAlignment
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.LinearLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

class ColumnWidget(
    override val alignment: (Orientation) -> Alignment = PlanarAlignment::Center
) : WidgetContainerImpl(), ColumnLayout {

    override val widget: IGWidget
        get() = this

    override fun measurableChildren(): List<Measurable> =
        widgetChildren()

}

data class ColumScope(private val column: ColumnWidget) : GuiScope<ColumnLayout>, LinearLayoutScope {
    override fun owner(): ColumnLayout = column

    override fun layout(): LinearLayout = owner()

}

fun GuiScope<out WidgetContainer>.column(
    alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    modifier: Modifier? = null,
    content: ColumScope.() -> Unit
) = owner().addWidgetChild(
    ColumnWidget(alignment).apply {
        ColumScope(this).content()
        modifier?.foldIn(Unit) { _, e -> e.tryApplyModify(this) }
    }
)