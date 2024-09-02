package moe.forpleuvoir.ibukigourd.gui.widget.layout.list

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.ColumnListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.height
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.margin
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.renderBackground
import moe.forpleuvoir.ibukigourd.gui.base.scope.ColumnListLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.util.ScrollState
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope
import moe.forpleuvoir.ibukigourd.gui.widget.theme.WidgetTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.nebula.common.util.primitive.sumOf

class ColumnListWidget(
    scrollState: ScrollState = ScrollState(),
    override val alignment: Alignment.Vertical,
    spacing: Float = 0f,
) : ListWidget(scrollState, alignment, spacing), ColumnListLayout {

    override fun onMeasureCompletion() {
        val totalSpace = widgetChildren().sumOf { child -> child.wrappedWidth + spacing } - spacing
        scrollState {
            maxAmount = (totalSpace - contentWidth).coerceAtLeast(0f)
            barProportion = contentWidth / totalSpace
            amountStep = widgetChildren().minOf { it.transform.width } / 2f
        }
        super<ListWidget>.onMeasureCompletion()
    }

    fun interface Scope : ListWidget.Scope<ColumnListWidget, Alignment.Vertical>, ColumnListLayoutScope

}

typealias ColumnListScope = ColumnListWidget.Scope

fun WidgetContainerScope.ColumnList(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = ScrollState(),
    spacing: Float = 0f,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: ColumnListScope.() -> Unit
) = addWidgetChild(ColumnListWidget(scrollState, alignment = verticalAlignment, spacing = spacing)) {
    ColumnListScope { this }.content()
    modifier.foldInApply()
}


fun WidgetContainerScope.ColumnListWrapped(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = ScrollState(),
    spacing: Float = 0f,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
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
    verticalArrangement = Arrangement.SpaceBetween
) {
    ColumnList(
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

