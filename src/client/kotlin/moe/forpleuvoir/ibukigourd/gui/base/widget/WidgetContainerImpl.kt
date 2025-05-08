package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.element.DrawableContainer
import moe.forpleuvoir.ibukigourd.gui.base.element.DrawableElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.element.addDefaultLayer
import moe.forpleuvoir.ibukigourd.gui.base.element.findLastInParentChain
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layoutable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.foreachWithIterator
import moe.forpleuvoir.ibukigourd.util.mc


abstract class WidgetContainerImpl : IGWidgetImpl(), WidgetContainer, DrawableElementContainer<IGWidget, IGWidget>, Layout {

    init {
        addDefaultLayer(DrawableContainer.getRenderLayer(this))
    }

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

    private val drawableChildren = mutableListOf<IGWidget>()

    override fun widgetChildren(): List<IGWidget> = widgetChildren

    override fun clearWidgetChildren() {
        drawableChildren.clear()
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

    override fun <W : IGWidget> addWidgetChild(child: W): W = child.also { widget ->
        widget.transform.parent = { this.transform }
        widget.parent = { this }
        widgetChildren.add(widget)
        drawableChildren.add(widget)
        drawableChildren.sortBy { it.renderPriority }
    }

    override fun <W : IGWidget> setWidgetChildren(index: Int, child: W) = child.also { widget ->
        widget.transform.parent = { this.transform }
        widget.parent = { this }
        widgetChildren[index] = widget
        drawableChildren[index] = widget
        drawableChildren.sortBy { it.renderPriority }
    }

    override fun swapWidgetChildren(index1: Int, index2: Int) {
        val temp = widgetChildren[index2]
        widgetChildren[index2] = widgetChildren[index1]
        widgetChildren[index1] = temp
    }

    override fun removeWidgetChild(child: IGWidget): Boolean {
        val removed = drawableChildren.remove(child)
        drawableChildren.sortBy { it.renderPriority }
        return widgetChildren.remove(child) && removed
    }

    override fun removeWidgetChildAt(index: Int): IGWidget? {
        drawableChildren.removeAt(index)
        drawableChildren.sortBy { it.renderPriority }
        return widgetChildren.removeAt(index)
    }

    override fun flat(): List<IGWidget> {
        return (widgetChildren().flatMap { if (it is WidgetContainer) it.flat() else listOf(it) } + this)
    }

    //------------ Drawable ------------\\

    override fun drawableChildren(): List<IGWidget> = drawableChildren

    //------------ Element ------------\\

    override fun elementChildren(): List<IGWidget> = widgetChildren()

    override fun onTick() {
        widgetChildren().forEach { it.tick() }
    }

    override fun onMouseEnter(event: MouseEnterEvent) = Unit

    override fun onMouseLeave(event: MouseLeaveEvent) = Unit

    @Suppress("DuplicatedCode")
    override fun onMouseMove(event: MouseMoveEvent) {
        super<IGWidgetImpl>.onMouseMove(event)

        widgetChildren().foreachWithIterator {
            if (it.active) it.mouseMove.invoke(event)
        }
    }

    override fun onMousePress(event: MousePressEvent) {
        super<IGWidgetImpl>.onMousePress(event)

        widgetChildren().foreachWithIterator {
            if (it.active) it.mousePress.invoke(event)
        }
    }

    override fun onFocused(event: FocusedEvent) {
        widgetChildren().foreachWithIterator {
            if (it.active) it.focused.invoke(event)
        }

        super<IGWidgetImpl>.onFocused(event)
    }

    override fun onMouseRelease(event: MouseReleaseEvent) {
        super<IGWidgetImpl>.onMouseRelease(event)

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