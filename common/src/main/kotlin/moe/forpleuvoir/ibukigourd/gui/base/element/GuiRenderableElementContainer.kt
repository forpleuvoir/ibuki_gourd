package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics.Companion.toIGGUIGraphics
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.foreachWithIterator
import moe.forpleuvoir.ibukigourd.input.mousePosition
import net.minecraft.client.gui.GuiGraphics

interface GuiRenderableElementContainer : GuiRenderableElement, GuiElementContainer, GuiRenderableContainer {

    //------------ Tickable ------------\\

    override var tick: () -> Unit

    override fun onTick() {
        elementChildren().foreachWithIterator {
            it.tick.invoke()
        }
    }


    //------------ ElementContainer ------------\\
    override fun renderableChildren(): List<GuiRenderable>

    override fun elementChildren(): List<GuiElement>

    override fun <T : GuiRenderable> addRenderableChild(child: T): T

    override fun <T : GuiElement> addElementChild(child: T): T

    //------------ Vanilla Drawable ------------\\

    @Suppress("LocalVariableName", "DuplicatedCode")
    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val ctx = guiGraphics.toIGGUIGraphics()
        val (_mouseX, _mouseY) = guiGraphics.minecraft.mousePosition

        renderBackground(ctx, _mouseX, _mouseY, delta)

        render(ctx, _mouseX, _mouseY, delta)

        renderableChildren().sortedBy { it.renderPriority }.foreachWithIterator { drawableChild ->
            if (drawableChild.visible) drawableChild.vanillaRender(ctx, _mouseX, _mouseY, delta)
        }

        renderOverlay(ctx, _mouseX, _mouseY, delta)
    }


    //------------ Drawable ------------\\

    override fun onRenderOverlay(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) = Unit

    override fun onRenderBackground(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) = Unit


    //------------ IGElement ------------\\


    override fun onMouseEnter(event: MouseEnterEvent) = Unit


    override fun onMouseLeave(event: MouseLeaveEvent) = Unit


    override fun onMouseMove(event: MouseMoveEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.mouseMove.invoke(event)
        }
    }

    override fun onMousePress(event: MousePressEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.mousePress.invoke(event)
        }
    }

    override fun onFocused(event: FocusedEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.focused.invoke(event)
        }
    }

    override fun onMouseRelease(event: MouseReleaseEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.mouseRelease.invoke(event)
        }
    }

    override fun onMouseDragging(event: MouseDragEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.mouseDragging.invoke(event)
        }
    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.mouseScrolling.invoke(event)
        }
    }

    override fun onKeyPress(event: KeyPressEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.keyPress.invoke(event)
        }
    }

    override fun onKeyRelease(event: KeyReleaseEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.keyRelease.invoke(event)
        }
    }

    override fun onCharTyped(event: CharTypedEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.charTyped.invoke(event)
        }
    }

}