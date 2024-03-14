package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.gui.screen.Screen
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.input.Mouse

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
        //上一帧不在元素内,这一帧在 触发 mouseMoveIn
        screen().let {
            if (!mouseHover(it.preMousePosition) && mouseHover(it.mousePosition)) {
                it.mousePosition.let { position ->
                    mouseMoveIn(position.x, position.y)
                }
            } else if (mouseHover(it.preMousePosition) && !mouseHover(it.mousePosition)) {
                it.mousePosition.let { position ->
                    mouseLeave(position.x, position.y)
                }
            }
        }
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

    override fun onMouseEnter(event: MouseEnterEvent) = Unit

    override var mouseLeave: (event: MouseLeaveEvent) -> Unit = ::onMouseLeave

    override fun onMouseLeave(event: MouseLeaveEvent) = Unit

    override fun onMouseMove(event: MouseMoveEvent) {
        event.unUsed {

        }
        if (!active)
        for (element in handleElements) {
            element.mouseMove(mouseX, mouseY)
        }
        if (!visible) return
        //上一帧不在元素内,这一帧在 触发 mouseMoveIn
        screen().let {
            if (!mouseHover(it.preMousePosition) && mouseHover(it.mousePosition)) {
                mouseMoveIn(mouseX, mouseY)
            } else if (mouseHover(it.preMousePosition) && !mouseHover(it.mousePosition)) {
                mouseLeave(mouseX, mouseY)
            }
        }
    }

    override var mouseMove: (event: MouseMoveEvent) -> Unit = ::onMouseMove

    override fun onMouseClick(event: MousePressEvent) {
        if (!active) return
        if (button == Mouse.LEFT && mouseHover()) {
            dragging = true
            if (focusable) {
                screen().focusedElement = this
                onFocusedChanged?.invoke(true)
            }
        }
        if (!mouseHover()) {
            screen().let {
                if (it.focusedElement == this) {
                    it.focusedElement = null
                    onFocusedChanged?.invoke(false)
                }
            }
        }
        for (element in handleElements) {
            element.mouseClick(mouseX, mouseY, button)
        }
    }

    override var mouseClick: (event: MousePressEvent) -> Unit = ::onMouseClick

    override fun onMouseRelease(event: MouseReleaseEvent) {
        if (!active) return
        if (button == Mouse.LEFT) dragging = false
        for (element in handleElements) {
            element.mouseRelease(mouseX, mouseY, button)
        }
    }

    override var mouseRelease: (event: MouseReleaseEvent) -> Unit = ::onMouseRelease

    override var dragging: Boolean = false

    override fun onMouseDragging(event: MouseDragEvent) {
        if (!active || !dragging) return
        for (element in handleElements) {
            element.mouseDragging(mouseX, mouseY, button, deltaX, deltaY)
        }
    }

    override var mouseDragging: (event: MouseDragEvent) -> Unit =
        ::onMouseDragging

    override fun onMouseScrolling(event: MouseScrollEvent): Unit {
        if (!active) return
        for (element in handleElements) {
            element.mouseScrolling(mouseX, mouseY, amount)
        }
    }

    override var mouseScrolling: (event: MouseScrollEvent) -> Unit = ::onMouseScrolling

    override fun onKeyPress(event: KeyPressEvent) {
        if (!active) return
        for (element in handleElements) {
            element.keyPress(keyCode)
        }
    }

    override var keyPress: (event: KeyPressEvent) -> Unit = ::onKeyPress

    override fun onKeyRelease(event: KeyReleaseEvent) {
        if (!active) return
        for (element in handleElements) {
            element.keyRelease(keyCode)
        }
    }

    override var keyRelease: (event: KeyReleaseEvent) -> Unit = ::onKeyRelease

    override fun onCharTyped(event: CharTypedEvent) {
        if (!active) return
        for (element in handleElements) {
            element.charTyped(chr)
        }
    }

    override var charTyped: (event: CharTypedEvent) -> Unit = ::onCharTyped
}