package moe.forpleuvoir.ibukigourd.gui.widget.layout.list

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.RowListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.ColumnListLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.RowListLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.Compose
import moe.forpleuvoir.ibukigourd.gui.util.ScrollState
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope
import moe.forpleuvoir.ibukigourd.gui.widget.theme.WidgetTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.nebula.common.util.primitive.sumOf

//------------ Row ------------\\

class RowListWidget(
    scrollState: ScrollState = ScrollState(),
    override val alignment: Alignment.Vertical,
    spacing: Float = 0f,
) : ListWidget(scrollState, alignment, spacing), RowListLayout {

    override fun onMeasureCompletion() {
        val totalSpace = widgetChildren().sumOf { child -> child.wrappedWidth + spacing } - spacing
        scrollState {
            maxAmount = (totalSpace - contentWidth).coerceAtLeast(0f)
            barProportion = contentWidth / totalSpace

            amountStep = _amountStep ?: when (widgetChildren().size) {
                0    -> 0f
                else -> widgetChildren().minOf { it.transform.width } / 2f
            }
        }
        super<ListWidget>.onMeasureCompletion()
    }


}

fun interface RowListScope : ListWidget.Scope<RowListWidget, Alignment.Vertical>, RowListLayoutScope

fun ContainerScope.RowList(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = ScrollState(),
    spacing: Float = 0f,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: RowListScope.() -> Unit
) = addWidgetChild(RowListWidget(scrollState, alignment = verticalAlignment, spacing = spacing)) {
    modifier.foldInApply()
    RowListScope { this }.Compose(content)
}


fun ContainerScope.RowListWrapped(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = ScrollState(),
    spacing: Float = 0f,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
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
    verticalArrangement = Arrangement.SpaceBetween
) {
    RowList(
        modifier = Modifier then listModifier(),
        scrollState = scrollState,
        spacing = spacing,
        verticalAlignment = verticalAlignment,
        content = content
    )
    Scroller(
        scrollState = scrollState,
        orientation = Orientation.Horizontal,
        modifier = Modifier
            .matchSibling()
            .height(barThickness)
            .margin(top = 1f) then scrollerModifier()
    )
}


//------------ Column ------------\\


class ColumnListWidget(
    scrollState: ScrollState = ScrollState(),
    override val alignment: Alignment.Horizontal,
    spacing: Float = 0f,
) : ListWidget(scrollState, alignment, spacing), ColumnListLayout {

    override fun onMeasureCompletion() {
        val totalSpace = widgetChildren().sumOf { it.wrappedHeight + spacing } - spacing
        scrollState {
            maxAmount = totalSpace - contentHeight
            barProportion = contentHeight / totalSpace
            amountStep = _amountStep ?: when (widgetChildren().size) {
                0    -> 0f
                else -> widgetChildren().minOf { it.transform.height } / 2f
            }
        }
        super<ListWidget>.onMeasureCompletion()
    }


}

fun interface ColumnListScope : ListWidget.Scope<ColumnListWidget, Alignment.Horizontal>, ColumnListLayoutScope

fun ContainerScope.ColumnList(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = ScrollState(),
    spacing: Float = 0f,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: ColumnListScope.() -> Unit
) = addWidgetChild(ColumnListWidget(scrollState, alignment = horizontalAlignment, spacing = spacing)) {
    modifier.foldInApply()
    ColumnListScope { this }.Compose(content)
}

fun ContainerScope.ColumnListWrapped(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = ScrollState(),
    spacing: Float = 0f,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    barThickness: Float = 9f,
    listModifier: RowScope.() -> Modifier = { Modifier },
    scrollerModifier: RowScope.() -> Modifier = { Modifier },
    content: ColumnListScope.() -> Unit
) = Row(
    modifier = Modifier
        .renderBackground { context, _, _, _ ->
            context.batchRenderTextureColored {
                pushWidgetTexture(transform, theme(WidgetTheme.ListLayout))
            }
        }
        .padding(3).then(modifier),
    horizontalArrangement = Arrangement.SpaceBetween
) {
    ColumnList(
        modifier = listModifier(),
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
            .margin(top = 1f, left = 1f) then scrollerModifier()
    )
}
