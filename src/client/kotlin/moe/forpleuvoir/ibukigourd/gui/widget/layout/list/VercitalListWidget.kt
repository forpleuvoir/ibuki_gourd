package moe.forpleuvoir.ibukigourd.gui.widget.layout.list

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.VerticalListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.VerticalListLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.ScrollerWidget
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.theme.WidgetTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.nebula.common.util.primitive.sumOf

class VerticalListWidget(
    amounts: Float = 0f,
    override val alignment: Alignment.Horizontal,
    spacing: Float = 0f,
) : ListWidget(amounts, alignment, spacing), VerticalListLayout {

    override var totalAmount: Float = 0f
        private set

    override var totalSpace: Float = 0f
        private set

    override fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable {
        return super.measureChildren(measurables, constraints).also {
            totalSpace = widgetChildren().sumOf { it.wrappedHeight + spacing } - spacing
            totalAmount = (totalSpace - contentHeight).coerceAtLeast(0f)
        }
    }

    fun interface VerticalListScope : ListWidgetScope<VerticalListWidget, Alignment.Horizontal>, VerticalListLayoutScope

}

typealias VerticalListScope = VerticalListWidget.VerticalListScope

fun WidgetContainerScope.RowList(
    modifier: Modifier = Modifier,
    spacing: Float = 0f,
    verticalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: VerticalListScope.() -> Unit
) = addWidgetChild(VerticalListWidget(alignment = verticalAlignment, spacing = spacing)) {
    VerticalListScope { this }.content()
    modifier.foldInApply()
}

fun WidgetContainerScope.RowListWrapped(
    modifier: Modifier = Modifier,
    spacing: Float = 0f,
    barThickness: Float = 9f,
    amountConsumer: (Float) -> Unit = {},
    initialAmount: () -> Float? = { null },
    listModifier: ColumnScope.() -> Modifier = { Modifier },
    scrollerModifier: ColumnScope.() -> Modifier = { Modifier },
    content: VerticalListScope.() -> Unit
) = Column(
    modifier = Modifier
        .renderBackground { context, _, _, _ ->
            val widget = this as IGWidget
            context.batchRenderTextureColored {
                pushWidgetTexture(widget.transform, widget.theme(WidgetTheme.ListLayout))
            }
        }
        .padding(3).then(modifier),
    horizontalArrangement = Arrangement.SpaceBetween
) {
    var scroller: ScrollerWidget? = null
    val list = RowList(
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
        orientation = Orientation.Vertical,
        modifier = Modifier
            .fill()
            .height(barThickness)
            .margin(top = 1f) then scrollerModifier()
    )
}

