package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.event.MousePressEvent
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.scissor
import moe.forpleuvoir.ibukigourd.gui.base.layout.ListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.WrappedListLayoutData
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext.Companion.toIGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
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
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.gui.DrawContext

class ListWidget(
    amounts: Float,
    var enableScissor: Boolean = true,
    override val orientation: Orientation = Orientation.Vertical,
    override val spacing: Float = 0f,
) : WidgetContainerImpl(), ListLayout {

    var amounts: Float = amounts
        set(value) {
            field = value.coerceIn(0f..totalAmount)
            layout(widgetChildren(), widgetChildren().map { WrappedListLayoutData.getOrDefault(it) })
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
            context.tryRender(drawableChild) {
                if (drawableChild.visible) drawableChild.vanillaRender(this, mouseX, mouseY, delta)
            }
        }
    }

    override fun onMousePress(event: MousePressEvent) {
        if (wasMouseOver) super.onMousePress(event)
    }

}

data class ListWidgetScope(private val list: ListWidget) : GuiScope<ListWidget>, ListLayoutScope {
    override fun owner(): ListWidget = list

    fun enableScissor() {
        list.enableScissor = true
    }

    fun disableScissor() {
        list.enableScissor = false
    }

    infix fun amountBy(delegatedAmount: DelegatedValue<Float>) {
        delegatedAmount.onSetValue = {
            list.amounts = it
            list.amounts
        }
        delegatedAmount.onGetValue = {
            list.amounts
        }

    }

}

fun GuiScope<out WidgetContainer>.list(
    orientation: Orientation = Orientation.Vertical,
    spacing: Float = 0f,
    modifier: Modifier? = null,
    content: ListWidgetScope.() -> Unit
) = this.owner().addWidgetChild(ListWidget(amounts = 0f, orientation = orientation, spacing = spacing)) {
    ListWidgetScope(this).content()
    modifier?.foldIn(Unit) { _, e -> e.tryApplyModify(this) }
}

fun GuiScope<out WidgetContainer>.listWithScroller(
    orientation: Orientation = Orientation.Vertical,
    spacing: Float = 0f,
    barThickness: Float = 9f,
    amountConsumer: (Float) -> Unit = {},
    initialAmount: Float = 0f,
    modifier: Modifier? = null,
    listModifier: (LinearLayoutScope.() -> Modifier)? = null,
    scrollerModifier: (LinearLayoutScope.() -> Modifier)? = null,
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
                modifier = m thenNullable modifier
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
                        } thenNullable listModifier?.invoke(this)
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
                        .margin(left = 1f) thenNullable scrollerModifier?.invoke(this)
                )
                scrollerSupplier = { scroller }
            }
        },
        {
            row(
                modifier = m thenNullable modifier
            ) {
                val list = list(
                    orientation = orientation,
                    spacing = spacing,
                    content = content,
                    modifier = Modifier
                        .fill()
                        .renderOverlay { ctx, _, _, _ ->
                            this as IGWidget
                            ctx.batchRenderBox {
                                ctx.boxOutline(transform.asWorldBox, Colors.BANANA_YELLOW)
                            }
                        }
                        .mouseScrolling {
                            this as ListWidget
                            if (this.wasMouseOver)
                                scrollerSupplier.invoke()?.scroller(it.verticalAmount)
                        } thenNullable listModifier?.invoke(this)
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
                        .margin(top = 1f) thenNullable scrollerModifier?.invoke(this)
                )
                scrollerSupplier = { scroller }
            }
        }
    )


}