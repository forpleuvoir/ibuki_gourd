package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.gui.screen.Screen
import moe.forpleuvoir.ibukigourd.gui.tip.Tip

@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractElement(
    override var width: ElementDimension = WrapContent(),
    override var height: ElementDimension = WrapContent()
) : Element, AbstractElementContainer() {

    override val screen: () -> Screen
        get() {
            return if (parent() is Screen) {
                { parent() as Screen }
            } else {
                { parent().screen.invoke() }
            }
        }

    override var visible: Boolean = true

    override val layoutData: Map<Any, Any> = hashMapOf()

    override var parent: () -> Element = { this }

    override var active = true

    override var fixed: Boolean = false

    override val focused: Boolean
        get() {
            if (focusable)
                return screen().focusedElement == this
            return false
        }

    override var onFocusedChanged: ((Boolean) -> Unit)? = null

    override val focusable: Boolean = false

    final override var tip: Tip? = null
        set(value) {
            if (value != null)
                this.addElement(value)
            else if (field != null)
                this.removeElement(field!!)
            field = value
        }

    override fun init() {
        modifier.apply(this)
        super.init()
        tip?.init?.invoke()
    }

    override fun tick() {
        if (!active) return
        for (element in handleElements) element.tick.invoke()
        tip?.tick?.invoke()
    }

    override var tick: () -> Unit = ::tick

    override fun onRender(renderContext: RenderContext) {
        if (!visible) return
        renderBackground.invoke(renderContext)
        renderOverlay.invoke(renderContext)
    }

    override var render: (renderContext: RenderContext) -> Unit = ::onRender

    override fun onRenderBackground(renderContext: RenderContext) = Unit

    override var renderBackground: (renderContext: RenderContext) -> Unit = ::onRenderBackground

    override fun onRenderOverlay(renderContext: RenderContext) = Unit

    override var renderOverlay: (renderContext: RenderContext) -> Unit = ::onRenderOverlay

    override var mouseEnter: (event: MouseEnterEvent) -> Unit = ::onMouseEnter

    override var mouseLeave: (event: MouseLeaveEvent) -> Unit = ::onMouseLeave

    override var mouseMove: (event: MouseMoveEvent) -> Unit = ::onMouseMove

    override var mouseClick: (event: MousePressEvent) -> Unit = ::onMouseClick

    override var mouseRelease: (event: MouseReleaseEvent) -> Unit = ::onMouseRelease

    override var dragging: Boolean = false

    override var mouseDragging: (event: MouseDragEvent) -> Unit = ::onMouseDragging

    override var mouseScrolling: (event: MouseScrollEvent) -> Unit = ::onMouseScrolling

    override var keyPress: (event: KeyPressEvent) -> Unit = ::onKeyPress

    override var keyRelease: (event: KeyReleaseEvent) -> Unit = ::onKeyRelease

    override var charTyped: (event: CharTypedEvent) -> Unit = ::onCharTyped
}