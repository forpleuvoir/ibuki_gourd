package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext.Companion.toIGDrawContext
import moe.forpleuvoir.ibukigourd.input.mousePosition
import net.minecraft.client.gui.DrawContext

interface DrawableElementContainer<E : DrawableElement> : DrawableElement, ElementContainer<E>, DrawableContainer<E> {

    //------------ Tickable ------------\\

    override var tick: () -> Unit

    override fun tick() {
        for (child in children()) {
            child.tick.invoke()
        }
    }


    //------------ ElementContainer ------------\\

    override fun addChild(child: E): E

    override fun children(): List<E>


    //------------ Vanilla Drawable ------------\\

    @Suppress("LocalVariableName")
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val ctx = context.toIGDrawContext()
        val (_mouseX, _mouseY) = context.client.mousePosition
        ctx.tryRender {
            renderBackground(this, _mouseX, _mouseY, delta)
            render.invoke(this, _mouseX, _mouseY, delta)
        }

        for (drawableChild in children().sortedBy { it.renderPriority }) {
            ctx.tryRender(drawableChild) {
                drawableChild.render.invoke(this, _mouseX, _mouseY, delta)
            }
        }

        ctx.tryRender { renderOverlay(this, _mouseX, _mouseY, delta) }
    }


    //------------ Drawable ------------\\

    override var renderPriority: Int

    override var visible: Boolean

    override fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float)

    override var render: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit

    override fun onRenderOverlay(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) = Unit

    override var renderOverlay: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) = Unit

    override var renderBackground: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit


    //------------ IGElement ------------\\

    override var layer: GuiLayer

    override var mouseEnter: (event: MouseEnterEvent) -> Unit
    override fun onMouseEnter(event: MouseEnterEvent) = Unit

    override var mouseLeave: (event: MouseLeaveEvent) -> Unit

    override fun onMouseLeave(event: MouseLeaveEvent) = Unit

    override var mouseMove: (event: MouseMoveEvent) -> Unit

    override fun onMouseMove(event: MouseMoveEvent) {
        for (child in children()) {
            child.mouseMove.invoke(event)
        }
    }

    override var mouseClick: (event: MousePressEvent) -> Unit

    override fun onMouseClick(event: MousePressEvent) {
        for (child in children()) {
            child.mouseClick.invoke(event)
        }
    }

    override var focused: (event: FocusedEvent) -> Unit

    override fun onFocused(event: FocusedEvent) {
        for (child in children()) {
            child.focused.invoke(event)
        }
    }

    override var mouseRelease: (event: MouseReleaseEvent) -> Unit

    override fun onMouseRelease(event: MouseReleaseEvent) {
        for (child in children()) {
            child.mouseRelease.invoke(event)
        }
    }

    override var mouseDragging: (event: MouseDragEvent) -> Unit

    override fun onMouseDragging(event: MouseDragEvent) {
        for (child in children()) {
            child.mouseDragging.invoke(event)
        }
    }

    override var mouseScrolling: (event: MouseScrollEvent) -> Unit

    override fun onMouseScrolling(event: MouseScrollEvent) {
        for (child in children()) {
            child.mouseScrolling.invoke(event)
        }
    }

    override var keyPress: (event: KeyPressEvent) -> Unit

    override fun onKeyPress(event: KeyPressEvent) {
        for (child in children()) {
            child.keyPress.invoke(event)
        }
    }

    override var keyRelease: (event: KeyReleaseEvent) -> Unit

    override fun onKeyRelease(event: KeyReleaseEvent) {
        for (child in children()) {
            child.keyRelease.invoke(event)
        }
    }

    override var charTyped: (event: CharTypedEvent) -> Unit

    override fun onCharTyped(event: CharTypedEvent) {
        for (child in children()) {
            child.charTyped.invoke(event)
        }
    }

}