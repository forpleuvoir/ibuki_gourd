package moe.forpleuvoir.ibukigourd.gui.widget.layout.list

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.RowListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.margin
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.renderBackground
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.RowListLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.util.ScrollState
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.theme.WidgetTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.nebula.common.util.primitive.sumOf

class RowListWidget(
    scrollState: ScrollState = ScrollState(),
    override val alignment: Alignment.Horizontal,
    spacing: Float = 0f,
) : ListWidget(scrollState, alignment, spacing), RowListLayout {

    override fun onMeasureCompletion() {
        val totalSpace = widgetChildren().sumOf { it.wrappedHeight + spacing } - spacing
        scrollState {
            maxAmount = totalSpace - contentHeight
            barProportion = contentHeight / totalSpace
            amountStep = widgetChildren().minOf { it.transform.height } / 2f
        }
        super<ListWidget>.onMeasureCompletion()
    }

    fun interface Scope : ListWidget.Scope<RowListWidget, Alignment.Horizontal>, RowListLayoutScope

}

typealias RowListScope = RowListWidget.Scope

fun WidgetContainerScope.RowList(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = ScrollState(),
    spacing: Float = 0f,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: RowListScope.() -> Unit
) = addWidgetChild(RowListWidget(scrollState, alignment = horizontalAlignment, spacing = spacing)) {
    RowListScope { this }.content()
    modifier.foldInApply()
}

fun WidgetContainerScope.RowListWrapped(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = ScrollState(),
    spacing: Float = 0f,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    barThickness: Float = 9f,
    listModifier: ColumnScope.() -> Modifier = { Modifier },
    scrollerModifier: ColumnScope.() -> Modifier = { Modifier },
    content: RowListScope.() -> Unit
) = Column(
    modifier = Modifier
        .renderBackground { context, _, _, _ ->
            context.batchRenderTextureColored {
                pushWidgetTexture(transform, theme(WidgetTheme.ListLayout))
            }
        }
        .padding(3).then(modifier),
    horizontalArrangement = Arrangement.SpaceBetween
) {
    RowList(
        modifier = Modifier then listModifier(),
        scrollState = scrollState,
        spacing = spacing,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
    Scroller(
        scrollState = scrollState,
        orientation = Orientation.Vertical,
        modifier = Modifier
            .matchSibling()
            .width(barThickness)
            .margin(top = 1f) then scrollerModifier()
    )
}

