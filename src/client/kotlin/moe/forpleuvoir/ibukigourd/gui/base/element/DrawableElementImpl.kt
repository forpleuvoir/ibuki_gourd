package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen

abstract class DrawableElementImpl : DrawableElement {

    //------------ Tickable ------------\\

    override var tick: () -> Unit = ::onTick

    override fun onTick() {}


    //------------ IGDrawable ------------\\

    private var _visible: Boolean? = null

    override var visible: Boolean
        set(value) {
            _visible = value
        }
        get() {
            val parentVisible = (parent() as? IGDrawable)?.visible
            return _visible ?: (parentVisible ?: true)
        }

    override fun clearVisible() {
        _visible = null
    }

    override var renderPriority: Int = 0

    //------------ IGElement ------------\\

    override val screen: () -> IGScreen?
        get() {
            return if (parent() is IGScreen) {
                { parent() as IGScreen }
            } else {
                { parent()?.screen?.let { it() } }
            }
        }

    override var parent: () -> IGElement? = { null }

    private var _active: Boolean? = null

    override var active: Boolean
        set(value) {
            _active = value
        }
        get() {
            return _active ?: (parent()?.active ?: true)
        }

    override fun clearActive() {
        _active = null
    }

    override var mouseEnter: (event: MouseEnterEvent) -> Unit = ::onMouseEnter

    override var mouseLeave: (event: MouseLeaveEvent) -> Unit = ::onMouseLeave

    override var mouseMove: (event: MouseMoveEvent) -> Unit = ::onMouseMove

    override var mousePress: (event: MousePressEvent) -> Unit = ::onMousePress

    override var focused: (event: FocusedEvent) -> Unit = ::onFocused

    override var mouseRelease: (event: MouseReleaseEvent) -> Unit = ::onMouseRelease

    override var mouseDragging: (event: MouseDragEvent) -> Unit = ::onMouseDragging

    override var mouseScrolling: (event: MouseScrollEvent) -> Unit = ::onMouseScrolling

    override var keyPress: (event: KeyPressEvent) -> Unit = ::onKeyPress

    override var keyRelease: (event: KeyReleaseEvent) -> Unit = ::onKeyRelease

    override var charTyped: (event: CharTypedEvent) -> Unit = ::onCharTyped

}