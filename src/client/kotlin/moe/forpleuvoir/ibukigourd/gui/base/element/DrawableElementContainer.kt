package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext.Companion.toIGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.foreachWithIterator
import moe.forpleuvoir.ibukigourd.input.mousePosition
import net.minecraft.client.gui.DrawContext

interface DrawableElementContainer : DrawableElement, ElementContainer, DrawableContainer {

    //------------ Tickable ------------\\

    override var tick: () -> Unit

    override fun onTick() {
        elementChildren().foreachWithIterator {
            it.tick.invoke()
        }
    }


    //------------ ElementContainer ------------\\
    override fun drawableChildren(): List<IGDrawable>

    override fun elementChildren(): List<IGElement>

    override fun <T : IGDrawable> addDrawableChild(child: T): T

    override fun <T : IGElement> addElementChild(child: T): T

    //------------ Vanilla Drawable ------------\\

    @Suppress("LocalVariableName", "DuplicatedCode")
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val ctx = context.toIGDrawContext()
        val (_mouseX, _mouseY) = context.client.mousePosition

        renderBackground(ctx, _mouseX, _mouseY, delta)

        render(ctx, _mouseX, _mouseY, delta)

        drawableChildren().sortedBy { it.renderPriority }.foreachWithIterator { drawableChild ->
            if (drawableChild.visible) drawableChild.vanillaRender(ctx, _mouseX, _mouseY, delta)
        }

        renderOverlay(ctx, _mouseX, _mouseY, delta)
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

    override var mouseEnter: (event: MouseEnterEvent) -> Unit

    override fun onMouseEnter(event: MouseEnterEvent) = Unit

    override var mouseLeave: (event: MouseLeaveEvent) -> Unit

    override fun onMouseLeave(event: MouseLeaveEvent) = Unit

    override var mouseMove: (event: MouseMoveEvent) -> Unit

    override fun onMouseMove(event: MouseMoveEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.mouseMove.invoke(event)
        }
    }

    override var mousePress: (event: MousePressEvent) -> Unit

    override fun onMousePress(event: MousePressEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.mousePress.invoke(event)
        }
    }

    override var focused: (event: FocusedEvent) -> Unit

    override fun onFocused(event: FocusedEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.focused.invoke(event)
        }
    }

    override var mouseRelease: (event: MouseReleaseEvent) -> Unit

    override fun onMouseRelease(event: MouseReleaseEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.mouseRelease.invoke(event)
        }
    }

    override var mouseDragging: (event: MouseDragEvent) -> Unit

    override fun onMouseDragging(event: MouseDragEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.mouseDragging.invoke(event)
        }
    }

    override var mouseScrolling: (event: MouseScrollEvent) -> Unit

    override fun onMouseScrolling(event: MouseScrollEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.mouseScrolling.invoke(event)
        }
    }

    override var keyPress: (event: KeyPressEvent) -> Unit

    override fun onKeyPress(event: KeyPressEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.keyPress.invoke(event)
        }
    }

    override var keyRelease: (event: KeyReleaseEvent) -> Unit

    override fun onKeyRelease(event: KeyReleaseEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.keyRelease.invoke(event)
        }
    }

    override var charTyped: (event: CharTypedEvent) -> Unit

    override fun onCharTyped(event: CharTypedEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.charTyped.invoke(event)
        }
    }

}