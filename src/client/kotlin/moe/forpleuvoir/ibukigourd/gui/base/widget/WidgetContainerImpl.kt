package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.event.GUIEvent.Companion.layer
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layoutable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext.Companion.toIGDrawContext
import moe.forpleuvoir.ibukigourd.input.mousePosition
import net.minecraft.client.gui.DrawContext


abstract class WidgetContainerImpl : IGWidgetImpl(), WidgetContainer, Measurable, Layout {

    //------------ Container ------------\\

    override val widget: IGWidget
        get() = this

    private val widgetChildren = mutableListOf<IGWidget>()

    override fun widgetChildren(): List<IGWidget> = widgetChildren

    override fun clearWidgetChildren() {
        widgetChildren.clear()
    }

    override fun layoutableChildren(): List<Layoutable> = widgetChildren

    override fun <W : IGWidget> addWidgetChild(child: W): W = child.also {
        it.transform.parent = { this.transform }
        it.parent = { this }
        widgetChildren.add(it)
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

        for (drawableChild in widgetChildren().sortedBy { it.renderPriority }) {
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

        for (child in widgetChildren()) {
            if (!child.active) continue
            val mouseOver = child.wasMouseOver
            child.mouseMove.invoke(event)
            if (!mouseOver && child.wasMouseOver) {
                child.mouseEnter(MouseEnterEvent(event.x, event.y).layer(this.layer))
            } else if (mouseOver && !child.wasMouseOver) {
                child.mouseLeave(MouseLeaveEvent(event.x, event.y).layer(this.layer))
            }
        }
    }

    override fun onMousePress(event: MousePressEvent) {
        super.onMousePress(event)
        for (child in widgetChildren()) {
            if (!child.active) continue
            child.mousePress.invoke(event)
        }
    }

    override fun onFocused(event: FocusedEvent) {
        for (child in widgetChildren()) {
            if (!child.active) continue
            child.focused.invoke(event)
        }
        super.onFocused(event)
    }

    override fun onMouseRelease(event: MouseReleaseEvent) {
        super.onMouseRelease(event)
        for (child in widgetChildren()) {
            if (!child.active) continue
            child.mouseRelease.invoke(event)
        }
    }

    override fun onMouseDragging(event: MouseDragEvent) {
        for (child in widgetChildren()) {
            if (!child.active) continue
            child.mouseDragging.invoke(event)
        }
    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        for (child in widgetChildren()) {
            if (!child.active) continue
            child.mouseScrolling.invoke(event)
        }
    }

    override fun onKeyPress(event: KeyPressEvent) {
        for (child in widgetChildren()) {
            if (!child.active) continue
            child.keyPress.invoke(event)
        }
    }

    override fun onKeyRelease(event: KeyReleaseEvent) {
        for (child in widgetChildren()) {
            if (!child.active) continue
            child.keyRelease.invoke(event)
        }
    }

    override fun onCharTyped(event: CharTypedEvent) {
        for (child in widgetChildren()) {
            if (!child.active) continue
            child.charTyped.invoke(event)
        }
    }
}