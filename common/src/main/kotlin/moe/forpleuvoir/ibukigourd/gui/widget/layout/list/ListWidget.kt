package moe.forpleuvoir.ibukigourd.gui.widget.layout.list

import moe.forpleuvoir.ibukigourd.gui.base.event.MousePressEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseScrollEvent
import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.useScissor
import moe.forpleuvoir.ibukigourd.gui.base.layout.ListLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics.Companion.toIGGUIGraphics
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ListLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetContainerImpl
import moe.forpleuvoir.ibukigourd.gui.util.ScrollState
import moe.forpleuvoir.ibukigourd.input.mousePosition
import net.minecraft.client.gui.GuiGraphics

abstract class ListWidget(
    val scrollState: ScrollState,
    override val alignment: Alignment.Linear,
    override val spacing: Float = 0f,
) : GuiWidgetContainerImpl(), ListLayout {

    var enableScissor: Boolean = true

    protected var _amountStep: Float? = 15f
        set(value) {
            field = value
            if (value != null) scrollState.amountStep = value
        }

    override fun amount(): Float = scrollState.amount

    init {
        scrollState.subscribe {
            layout()
        }
    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        super.onMouseScrolling(event)
        event.tryUse(wasMouseOver).onSuccess { scrollState.scroll(event.verticalAmount) }
    }

    //------------ Render ------------\\

    @Suppress("LocalVariableName")
    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val graphics = guiGraphics.toIGGUIGraphics()
        val (_mouseX, _mouseY) = guiGraphics.minecraft.mousePosition
        graphics.apply {
            renderBackground(this, _mouseX, _mouseY, delta)
            render.invoke(this, _mouseX, _mouseY, delta)
            if (enableScissor) {
                graphics.useScissor(contentBox(true)) {
                    renderChildren(graphics, _mouseX, _mouseY, delta)
                }
            } else {
                renderChildren(graphics, _mouseX, _mouseY, delta)
            }

            renderOverlay(this, _mouseX, _mouseY, delta)
        }
    }

    private fun renderChildren(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) {
        for (drawableChild in widgetChildren().sortedBy { it.renderPriority }) {
            onRenderChild(drawableChild, guiGraphics, mouseX, mouseY, delta)
        }
    }

    var onRenderChild: (child: GuiWidget, guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::renderChild

    fun renderChild(child: GuiWidget, guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) {
        if ((child.transform.asWorldCoordinateBox intersectWith transform.asWorldCoordinateBox).exist) {
            child.clearActive()
            child.clearVisible()
        } else {
            child.active = false
            child.visible = false
        }
        if (child.visible) child.vanillaRender(guiGraphics, mouseX, mouseY, delta)
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

        fun onRenderChild(render: (child: GuiWidget, guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) -> Unit) {
            owner().onRenderChild = render
        }

        fun amountStep(step: Float) {
            owner()._amountStep = step
        }

    }

}

