package moe.forpleuvoir.ibukigourd.gui.widget.layout.list

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.HorizontalListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.HorizontalListLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.ScrollerWidget
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope
import moe.forpleuvoir.ibukigourd.gui.widget.theme.WidgetTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.nebula.common.util.primitive.sumOf

class HorizontalListWidget(
    amounts: Float = 0f,
    override val alignment: Alignment.Vertical,
    spacing: Float = 0f,
) : ListWidget(amounts, alignment, spacing), HorizontalListLayout {

    override var totalAmount: Float = 0f
        private set

    override var totalSpace: Float = 0f
        private set

    override fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable {
        return super.measureChildren(measurables, constraints).also {
            totalSpace = widgetChildren().sumOf { it.wrappedWidth + spacing } - spacing
            totalAmount = (totalSpace - contentWidth).coerceAtLeast(0f)
        }
    }

    fun interface HorizontalListScope : ListWidgetScope<HorizontalListWidget, Alignment.Vertical>, HorizontalListLayoutScope

}

typealias HorizontalListScope = HorizontalListWidget.HorizontalListScope

fun WidgetContainerScope.ColumnList(
    modifier: Modifier = Modifier,
    spacing: Float = 0f,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: HorizontalListScope.() -> Unit
) = addWidgetChild(HorizontalListWidget(alignment = verticalAlignment, spacing = spacing)) {
    HorizontalListScope { this }.content()
    modifier.foldInApply()
}


fun WidgetContainerScope.ColumnListWrapped(
    modifier: Modifier = Modifier,
    spacing: Float = 0f,
    barThickness: Float = 9f,
    amountConsumer: (Float) -> Unit = {},
    initialAmount: () -> Float? = { null },
    listModifier: RowScope.() -> Modifier = { Modifier },
    scrollerModifier: RowScope.() -> Modifier = { Modifier },
    content: HorizontalListScope.() -> Unit
) = Row(
    modifier = Modifier
        .renderBackground { context, _, _, _ ->
            val widget = this as IGWidget
            context.batchRenderTextureColored {
                pushWidgetTexture(widget.transform, widget.theme(WidgetTheme.ListLayout))
            }
        }
        .padding(3).then(modifier),
    verticalArrangement = Arrangement.SpaceBetween
) {
    var scroller: ScrollerWidget? = null
    val list = ColumnList(
        modifier = Modifier
            .fill()
            .mouseScrolling { event ->
                this as ListWidget
                onMouseScrolling(event)
                event.tryUse(wasMouseOver).onSuccess { scroller?.scroller(event.verticalAmount) }
            } then listModifier(),
        spacing = spacing,
        content = content
    )
    scroller = Scroller(
        amountStep = { list.widgetChildren().minOf { it.transform.width } / 2f },
        totalAmount = { list.totalAmount },
        barProportion = { (list.contentWidth / list.totalSpace).coerceIn(0f..1f) },
        amountConsumer = {
            list.amount = it
            amountConsumer(it)
        },
        initialAmount = initialAmount,
        orientation = Orientation.Horizontal,
        modifier = Modifier
            .fill()
            .height(barThickness)
            .margin(top = 1f) then scrollerModifier()
    )
}

