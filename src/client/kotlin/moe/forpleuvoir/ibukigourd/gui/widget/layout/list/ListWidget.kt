package moe.forpleuvoir.ibukigourd.gui.widget.layout.list

import moe.forpleuvoir.ibukigourd.gui.base.event.MousePressEvent
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.scissor
import moe.forpleuvoir.ibukigourd.gui.base.layout.ListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext.Companion.toIGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ListLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl
import moe.forpleuvoir.ibukigourd.input.mousePosition
import moe.forpleuvoir.ibukigourd.util.DelegatedValue
import moe.forpleuvoir.nebula.common.util.primitive.pick
import net.minecraft.client.gui.DrawContext

abstract class ListWidget(
    amounts: Float = 0f,
    override val alignment: Alignment.Linear,
    override val spacing: Float = 0f,
) : WidgetContainerImpl(), ListLayout {

    var enableScissor: Boolean = true

    var amount: Float = amounts
        set(value) {
            field = (value.isNaN().pick(0f, value)).coerceIn(0f..totalAmount)
            layout()
        }

    override fun amount(): Float = this.amount

    abstract val totalAmount: Float

    abstract val totalSpace: Float


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
            onRenderChild(drawableChild, context, mouseX, mouseY, delta)
        }
    }

    var onRenderChild: (child: IGWidget, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = this::renderChild

    fun renderChild(child: IGWidget, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        if ((child.transform.asWorldBox intersectWith transform.asWorldBox).exist) {
            child.active = true
            child.visible = true
        } else {
            child.active = false
            child.visible = false
        }
        if (child.visible) child.vanillaRender(context, mouseX, mouseY, delta)
    }

    override fun onMousePress(event: MousePressEvent) {
        if (wasMouseOver) super.onMousePress(event)
    }

    companion object

    interface ListWidgetScope<L : ListWidget, A : Alignment.Linear> : GuiScope<L>, ListLayoutScope<A> {

        fun enableScissor() {
            owner().enableScissor = true
        }

        fun disableScissor() {
            owner().enableScissor = false
        }

        fun onRenderChild(render: (child: IGWidget, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit) {
            owner().onRenderChild = render
        }

        infix fun amountBy(delegatedAmount: DelegatedValue<Float>) {
            delegatedAmount.onSetValue = {
                owner().amount = it
                owner().amount
            }
            delegatedAmount.onGetValue = {
                owner().amount
            }

        }

    }

}

