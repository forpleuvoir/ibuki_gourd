@file:Suppress("MemberVisibilityCanBePrivate")

package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.element.*
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.MouseCursor

abstract class AbstractScreen(
    width: ElementDimension = match_parent,
    height: ElementDimension = match_parent,
    final override val layers: List<Layer> = Layer.defaultLayers
) : AbstractElement(width, height), Screen {

    init {
        check(layers.toSet().size == layers.size) {
            "Duplicate layers are not allowed"
        }
    }

    override var layer: Layer = Layer.default


    override val screen: () -> Screen get() = { this }

    override var parent: () -> Element = { this }

    override var parentScreen: Screen? = null

    override var focusedElement: Element? = null

    override var pauseGame: Boolean = false

    override var shouldCloseOnEsc: Boolean = true

    override var close: () -> Unit = ::onClose

//    override val handleElements: List<Element>
//        get() = buildList {
//            addAll(super.handleElements)
//        }



    override fun onMouseEnter(event: MouseEnterEvent) = Unit

    override fun onMouseLeave(event: MouseLeaveEvent) = Unit

    override fun onMouseMove(event: MouseMoveEvent) {
        if (!active) return

        wasMouseOver = event.position in transform.asWorldBox

        for (layer in layers) {
            event.layer = layer
            for (element in handleElements) {
                val mouseOver = element.wasMouseOver
                element.mouseMove(event)
                if (!mouseOver && element.wasMouseOver) {
                    element.mouseEnter(MouseEnterEvent(event.x, event.y).apply { this.layer = layer })
                } else if (mouseOver && !element.wasMouseOver) {
                    element.mouseLeave(MouseLeaveEvent(event.x, event.y).apply { this.layer = layer })
                }
            }
        }

    }

    override fun onMouseClick(event: MousePressEvent) {
        if (!active) return

        dragging = wasMouseOver

        if (wasMouseOver) {
            focused(FocusedEvent())
        }

        for (layer in layers) {
            event.layer = layer
            for (element in handleElements) {
                element.mouseClick(event)
            }
        }
    }

    override fun onFocused(event: FocusedEvent) {
        if (!active) return

        for (layer in layers) {
            event.layer = layer
            for (element in handleElements) {
                element.focused(event)
            }
        }

        event.tryUse().onSuccess {
            wasFocused = true
        }
    }

    override fun onMouseRelease(event: MouseReleaseEvent) {
        if (!active) return

        dragging = false

        for (layer in layers) {
            event.layer = layer
            for (element in handleElements) {
                element.mouseRelease(event)
            }
        }
    }

    override fun onMouseDragging(event: MouseDragEvent) {
        if (!active && dragging) return

        for (layer in layers) {
            event.layer = layer
            for (element in handleElements) {
                if (element.dragging) element.mouseDragging(event)
            }
        }

    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        if (!active) return

        for (layer in layers) {
            event.layer = layer
            for (element in handleElements) {
                element.mouseScrolling(event)
            }
        }

    }

    override fun onKeyPress(event: KeyPressEvent) {
        if (!active) return

        for (layer in layers) {
            event.layer = layer
            for (element in handleElements) {
                element.keyPress(event)
            }
        }

        event.tryUse {
            event.keyCode == Keyboard.ESCAPE && shouldCloseOnEsc
        }.onSuccess {
            close()
        }
    }

    override fun onKeyRelease(event: KeyReleaseEvent) {
        if (!active) return
        for (layer in layers) {
            event.layer = layer
            for (element in handleElements) {
                element.keyRelease(event)
            }
        }
    }

    override fun onCharTyped(event: CharTypedEvent) {
        if (!active) return
        for (layer in layers) {
            event.layer = layer
            for (element in handleElements) {
                element.charTyped(event)
            }
        }
    }

    override fun onRender(renderContext: RenderContext) {
        if (!visible) return
        renderBackground(renderContext)

        for (i in layers.lastIndex downTo 0) {
            renderContext.layer = layers[i]
            for (renderElement in renderElements) {
                renderElement.render(renderContext)
            }
        }
        //迭代器实现
//        layers.listIterator(layers.lastIndex).let {
//            while (it.hasPrevious()) {
//                renderContext.layer = it.previous()
//                for (renderElement in renderElements) {
//                    renderElement.render(renderContext)
//                }
//            }
//        }
        renderOverlay(renderContext)
    }

    override fun onClose() {
        ScreenManager.setScreen(parentScreen)
        MouseCursor.current = MouseCursor.Cursor.ARROW_CURSOR
    }

    override fun onResize(width: Int, height: Int) {

    }


    override var resize: (width: Int, height: Int) -> Unit = ::onResize

}

fun screen(screenScope: Screen.() -> Unit): Screen {
    return object : AbstractScreen() {}.apply {
        screenScope(this)
    }
}