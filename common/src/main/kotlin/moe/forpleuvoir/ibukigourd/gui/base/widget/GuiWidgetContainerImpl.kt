package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layoutable
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics.Companion.toIGGUIGraphics
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.foreachWithIterator
import moe.forpleuvoir.ibukigourd.input.mousePosition
import net.minecraft.client.gui.GuiGraphics


abstract class GuiWidgetContainerImpl : GuiWidgetImpl(), GuiWidgetContainer {

    override lateinit var compose: () -> Unit

    override fun recompose() {
        clearWidgetChildren()
        compose()
        remeasure()
    }

    private val widgetChildren = mutableListOf<GuiWidget>()

    override fun widgetChildren(): List<GuiWidget> = widgetChildren

    override fun clearWidgetChildren() {
        widgetChildren.clear()
    }

    override fun layoutableChildren(): List<Layoutable> = widgetChildren

    override var layoutCompletion: () -> Unit = ::onLayoutCompletion

    override fun onMeasureCompletion() {
        super<GuiWidgetContainer>.onMeasureCompletion()
    }

    override fun remeasure() {
        super.remeasure()
        layout()
    }

    override fun <W : GuiWidget> addWidgetChild(child: W): W = child.also {
        it.transform.parent = { this.transform }
        it.parent = { this }
        widgetChildren.add(it)
    }

    override fun <W : GuiWidget> setWidgetChildren(index: Int, child: W) = child.also {
        it.transform.parent = { this.transform }
        it.parent = { this }
        widgetChildren[index] = it
    }

    override fun swapWidgetChildren(index1: Int, index2: Int) {
        val temp = widgetChildren[index2]
        widgetChildren[index2] = widgetChildren[index1]
        widgetChildren[index1] = temp
    }

    override fun removeWidgetChild(child: GuiWidget) = widgetChildren.remove(child)

    override fun removeWidgetChildAt(index: Int): GuiWidget? = widgetChildren.removeAt(index)

    override fun flat(): List<GuiWidget> {
        return (widgetChildren().flatMap { if (it is GuiWidgetContainer) it.flat() else listOf(it) } + this)
    }

    //------------ Drawable ------------\\

    @Suppress("LocalVariableName")
    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val ctx = guiGraphics.toIGGUIGraphics()
        val (_mouseX, _mouseY) = guiGraphics.minecraft.mousePosition
        renderBackground(ctx, _mouseX, _mouseY, delta)
        render.invoke(ctx, _mouseX, _mouseY, delta)

        widgetChildren().sortedBy { it.renderPriority }.foreachWithIterator { drawableChild ->
            if (drawableChild.visible) drawableChild.vanillaRender(ctx, _mouseX, _mouseY, delta)
        }

        renderOverlay(ctx, _mouseX, _mouseY, delta)
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