package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.foreachWithIterator

interface DrawableElementContainer<D : IGDrawable, E : IGElement> : DrawableElement, ElementContainer<E>, DrawableContainer<D> {

    //------------ Tickable ------------\\

    override var tick: () -> Unit

    override fun onTick() {
        elementChildren().foreachWithIterator {
            it.tick.invoke()
        }
    }

    //------------ Drawable ------------\\

    override var renderPriority: Int

    override var visible: Boolean

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