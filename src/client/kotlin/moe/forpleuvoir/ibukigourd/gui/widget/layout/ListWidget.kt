package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.event.MousePressEvent
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.scissor
import moe.forpleuvoir.ibukigourd.gui.base.layout.ListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext.Companion.toIGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.LinearLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ListLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl
import moe.forpleuvoir.ibukigourd.gui.widget.ScrollerWidget
import moe.forpleuvoir.ibukigourd.gui.widget.scroller
import moe.forpleuvoir.ibukigourd.gui.widget.theme.WidgetTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.ibukigourd.input.mousePosition
import moe.forpleuvoir.ibukigourd.util.DelegatedValue
import moe.forpleuvoir.nebula.common.util.primitive.pick
import net.minecraft.client.gui.DrawContext

class ListWidget(
    amounts: Float,
    var enableScissor: Boolean = true,
    override val orientation: Orientation = Orientation.Vertical,
    override val spacing: Float = 0f,
) : WidgetContainerImpl(), ListLayout {

    var amounts: Float = amounts
        set(value) {
            field = (value.isNaN().pick(0f, value)).coerceIn(0f..totalAmount)
            layout()
        }

    override val amount: () -> Float = this::amounts

    var totalAmount = 0f
        private set

    var totalContentSize = 0f
        private set

    override fun measure(constraints: Constraints): Placeable =
        super<ListLayout>.measure(constraints).also {
            //------------ 更新可滚动的总量 ------------\\
            totalContentSize = widgetChildren().lastIndex * spacing
            for (widgetChild in widgetChildren()) {
                totalContentSize += orientation.peek(widgetChild.wrappedHeight, widgetChild.wrappedWidth)
            }
            this.totalAmount = (totalContentSize - orientation.peek(contentHeight, contentWidth)).coerceAtLeast(0f)
        }


    //------------ Render ------------\\

    @Suppress("LocalVariableName")
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val ctx = context.toIGDrawContext()
        val (_mouseX, _mouseY) = context.client.mousePosition
        ctx.tryRender {
            renderBackground(this, _mouseX, _mouseY, delta)
            render.invoke(this, _mouseX, _mouseY, delta)
        }
        if (enableScissor) {
            ctx.scissor(contentBox(true)) {
                renderChildren(ctx, _mouseX, _mouseY, delta)
            }
        } else {
            renderChildren(ctx, _mouseX, _mouseY, delta)
        }

        ctx.tryRender { renderOverlay(this, _mouseX, _mouseY, delta) }
    }

    private fun renderChildren(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        for (drawableChild in widgetChildren().sortedBy { it.renderPriority }) {
            if ((drawableChild.transform.asWorldBox intersectWith transform.asWorldBox).exist) {
                drawableChild.active = true
                drawableChild.visible = true
            } else {
                drawableChild.active = false
                drawableChild.visible = false
            }
            if (drawableChild.visible) drawableChild.vanillaRender(context, mouseX, mouseY, delta)
        }
    }

    override fun onMousePress(event: MousePressEvent) {
        if (wasMouseOver) super.onMousePress(event)
    }

    companion object

    fun interface ListWidgetScope : GuiScope<ListWidget>, ListLayoutScope {

        fun enableScissor() {
            owner().enableScissor = true
        }

        fun disableScissor() {
            owner().enableScissor = false
        }

        infix fun amountBy(delegatedAmount: DelegatedValue<Float>) {
            delegatedAmount.onSetValue = {
                owner().amounts = it
                owner().amounts
            }
            delegatedAmount.onGetValue = {
                owner().amounts
            }

        }

    }

}

typealias ListWidgetScope = ListWidget.ListWidgetScope


fun GuiScope<out WidgetContainer>.list(
    orientation: Orientation = Orientation.Vertical,
    spacing: Float = 0f,
    modifier: Modifier = Modifier,
    content: ListWidgetScope.() -> Unit
) = addWidgetChild(ListWidget(amounts = 0f, orientation = orientation, spacing = spacing)) {
    ListWidgetScope { this }.content()
    modifier.foldInApply()
}

fun GuiScope<out WidgetContainer>.listWithScroller(
    orientation: Orientation = Orientation.Vertical,
    spacing: Float = 0f,
    barThickness: Float = 9f,
    amountConsumer: (Float) -> Unit = {},
    initialAmount: () -> Float? = { null },
    modifier: Modifier = Modifier,
    listModifier: LinearLayoutScope.() -> Modifier = { Modifier },
    scrollerModifier: LinearLayoutScope.() -> Modifier = { Modifier },
    content: ListWidgetScope.() -> Unit
): WidgetContainerImpl {
    var scrollerSupplier: () -> ScrollerWidget? = { null }

    val m = Modifier
        .renderBackground { context, mouseX, mouseY, delta ->
            val widget = this as IGWidget
            context.batchRenderTextureColored {
                context.drawWidgetTexture(widget.transform.asWorldBox, widget.theme(WidgetTheme.ListLayout))
            }
        }
        .padding(3)
    return orientation.peek(
        {
            column(
                modifier = m then modifier
            ) {
                val list = list(
                    orientation = orientation,
                    spacing = spacing,
                    content = content,
                    modifier = Modifier
                        .fill()
                        .mouseScrolling {
                            this as ListWidget
                            if (this.wasMouseOver)
                                scrollerSupplier.invoke()?.scroller(it.verticalAmount)
                        } then listModifier()
                )
                val scroller = scroller(
                    amountStep = { list.widgetChildren().minOf { it.transform.height } / 2f },
                    totalAmount = { list.totalAmount },
                    barProportion = { (list.contentHeight / list.totalContentSize).coerceIn(0f..1f) },
                    amountConsumer = {
                        list.amounts = it
                        amountConsumer(it)
                    },
                    initialAmount = initialAmount,
                    orientation = orientation,
                    modifier = Modifier
                        .fill()
                        .width(barThickness)
                        .margin(left = 1f) then scrollerModifier()
                )
                scrollerSupplier = { scroller }
            }
        },
        {
            row(
                modifier = m then modifier
            ) {
                val list = list(
                    orientation = orientation,
                    spacing = spacing,
                    content = content,
                    modifier = Modifier
                        .fill()
                        .mouseScrolling {
                            this as ListWidget
                            if (this.wasMouseOver)
                                scrollerSupplier.invoke()?.scroller(it.verticalAmount)
                        } then listModifier()
                )
                val scroller = scroller(
                    amountStep = { list.widgetChildren().minOf { it.transform.width } / 2f },
                    totalAmount = { list.totalAmount },
                    barProportion = { (list.contentWidth / list.totalContentSize).coerceIn(0f..1f) },
                    amountConsumer = {
                        list.amounts = it
                        amountConsumer(it)
                    },
                    initialAmount = initialAmount,
                    orientation = orientation,
                    modifier = Modifier
                        .fill()
                        .height(barThickness)
                        .margin(top = 1f) then scrollerModifier()
                )
                scrollerSupplier = { scroller }
            }
        }
    )


}