package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.element.findLastInParentChain
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layoutable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext.Companion.toIGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.foreachWithIterator
import moe.forpleuvoir.ibukigourd.input.mousePosition
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.gui.DrawContext


abstract class WidgetContainerImpl : IGWidgetImpl(), WidgetContainer, Layout {

    //------------ Container ------------\\

    override val widget: IGWidget
        get() = this

    override lateinit var compose: () -> Unit

    override fun recompose() {
        clearWidgetChildren()
        compose()
        remeasure()
    }

    private val widgetChildren = mutableListOf<IGWidget>()

    override fun widgetChildren(): List<IGWidget> = widgetChildren

    override fun clearWidgetChildren() {
        widgetChildren.clear()
    }

    override fun layoutableChildren(): List<Layoutable> = widgetChildren

    override var layoutCompletion: () -> Unit = ::onLayoutCompletion

    override fun onMeasureCompletion() {
        super<Layout>.onMeasureCompletion()
    }

    override fun remeasure() {
        this.findLastInParentChain(false) { it is Measurable }?.let {
            it as Measurable
            it.remeasure()
            return
        }
        measure(Constraints.of(0f, mc.window.scaledWidth.toFloat(), 0f, mc.window.scaledHeight.toFloat()))
        measureCompletion()
        layout()

    }

    override fun <W : IGWidget> addWidgetChild(child: W): W = child.also {
        it.transform.parent = { this.transform }
        it.parent = { this }
        widgetChildren.add(it)
    }

    override fun <W : IGWidget> setWidgetChildren(index: Int, child: W) = child.also {
        it.transform.parent = { this.transform }
        it.parent = { this }
        widgetChildren[index] = it
    }

    override fun swapWidgetChildren(index1: Int, index2: Int) {
        val temp = widgetChildren[index2]
        widgetChildren[index2] = widgetChildren[index1]
        widgetChildren[index1] = temp
    }

    override fun removeWidgetChild(child: IGWidget) = widgetChildren.remove(child)

    override fun removeWidgetChildAt(index: Int): IGWidget? = widgetChildren.removeAt(index)

    override fun flat(): List<IGWidget> {
        return (widgetChildren().flatMap { if (it is WidgetContainer) it.flat() else listOf(it) } + this)
    }

    //------------ Drawable ------------\\

    @Suppress("LocalVariableName")
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val ctx = context.toIGDrawContext()
        val (_mouseX, _mouseY) = context.client.mousePosition
        ctx.tryRender {
            renderBackground(this, _mouseX, _mouseY, delta)
            render.invoke(this, _mouseX, _mouseY, delta)
        }

        widgetChildren().sortedBy { it.renderPriority }.foreachWithIterator { drawableChild ->
            if (drawableChild.visible) drawableChild.vanillaRender(ctx, _mouseX, _mouseY, delta)
        }

        ctx.tryRender { renderOverlay(this, _mouseX, _mouseY, delta) }
    }

    //------------ Element ------------\\

    override fun onTick() {
        widgetChildren().forEach { it.tick() }
    }

    override fun onMouseEnter(event: MouseEnterEvent) = Unit

    override fun onMouseLeave(event: MouseLeaveEvent) = Unit

    @Suppress("DuplicatedCode")
    override fun onMouseMove(event: MouseMoveEvent) {
        super.onMouseMove(event)

        widgetChildren().foreachWithIterator {
            if (it.active) it.mouseMove.invoke(event)
        }
    }

    override fun onMousePress(event: MousePressEvent) {
        super.onMousePress(event)
        widgetChildren().foreachWithIterator {
            if (it.active) it.mousePress.invoke(event)
        }
    }

    override fun onFocused(event: FocusedEvent) {
        widgetChildren().foreachWithIterator {
            if (it.active) it.focused.invoke(event)
        }
        super.onFocused(event)
    }

    override fun onMouseRelease(event: MouseReleaseEvent) {
        super.onMouseRelease(event)
        widgetChildren().foreachWithIterator {
            if (it.active) it.mouseRelease.invoke(event)
        }
    }

    override fun onMouseDragging(event: MouseDragEvent) {
        widgetChildren().foreachWithIterator {
            if (it.active) it.mouseDragging.invoke(event)
        }
    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        widgetChildren().foreachWithIterator {
            if (it.active) it.mouseScrolling.invoke(event)
        }
    }

    override fun onKeyPress(event: KeyPressEvent) {
        widgetChildren().foreachWithIterator {
            if (it.active) it.keyPress.invoke(event)
        }
    }

    override fun onKeyRelease(event: KeyReleaseEvent) {
        widgetChildren().foreachWithIterator {
            if (it.active) it.keyRelease.invoke(event)
        }
    }

    override fun onCharTyped(event: CharTypedEvent) {
        widgetChildren().foreachWithIterator {
            if (it.active) it.charTyped.invoke(event)
        }
    }
}