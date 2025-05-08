package moe.forpleuvoir.ibukigourd.gui.widget.layout.list

import moe.forpleuvoir.ibukigourd.gui.base.element.addDefaultLayer
import moe.forpleuvoir.ibukigourd.gui.base.event.MousePressEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseScrollEvent
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.scissor
import moe.forpleuvoir.ibukigourd.gui.base.layout.ListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ListLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl
import moe.forpleuvoir.ibukigourd.gui.util.ScrollState

abstract class ListWidget(
    val scrollState: ScrollState,
    override val alignment: Alignment.Linear,
    override val spacing: Float = 0f,
) : WidgetContainerImpl(), ListLayout {

    var enableScissor: Boolean = true

    override fun amount(): Float = scrollState.amount

    init {
        scrollState.subscribe {
            layout()
        }

        addDefaultLayer { context, mouseX, mouseY, delta ->
            if (enableScissor) {
                context.scissor(contentBox(true)) {
                    renderChildren(context, mouseX, mouseY, delta)
                }
            } else {
                renderChildren(context, mouseX, mouseY, delta)
            }
        }
    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        super.onMouseScrolling(event)
        event.tryUse(wasMouseOver).onSuccess { scrollState.scroll(event.verticalAmount) }
    }

    //------------ Render ------------\\

    fun renderChildren(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        for (drawableChild in widgetChildren().sortedBy { it.renderPriority }) {
            renderChild(drawableChild, context, mouseX, mouseY, delta)
        }
    }

    private var renderChild: (child: IGWidget, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRenderChild

    fun onRenderChild(child: IGWidget, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        if ((child.transform.asWorldCoordinateBox intersectWith transform.asWorldCoordinateBox).exist) {
            child.clearActive()
            child.clearVisible()
        } else {
            child.active = false
            child.visible = false
        }
        if (child.visible) child.render(context, mouseX, mouseY, delta)
    }

    override fun onMousePress(event: MousePressEvent) {
        if (wasMouseOverContent) super.onMousePress(event)
    }

    interface Scope<L : ListWidget, A : Alignment.Linear> : GuiScope<L>, ListLayoutScope<A> {

        fun enableScissor() {
            owner().enableScissor = true
        }

        fun disableScissor() {
            owner().enableScissor = false
        }

        fun onRenderChild(render: (child: IGWidget, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit) {
            owner().renderChild = render
        }

    }

}

