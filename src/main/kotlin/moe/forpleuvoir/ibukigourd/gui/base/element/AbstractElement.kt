package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.gui.screen.Screen
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.input.KeyCode
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
                    mouseMoveOut(position.x, position.y)
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
        for (element in renderElements) element.render(renderContext)
        renderOverlay.invoke(renderContext)
    }

    override var render: (renderContext: RenderContext) -> Unit = ::onRender

    override fun onRenderBackground(renderContext: RenderContext) = Unit

    override var renderBackground: (renderContext: RenderContext) -> Unit = ::onRenderBackground

    override fun onRenderOverlay(renderContext: RenderContext) = Unit

    override var renderOverlay: (renderContext: RenderContext) -> Unit = ::onRenderOverlay

    override var mouseMoveIn: (mouseX: Float, mouseY: Float) -> Unit = ::onMouseMoveIn

    override fun onMouseMoveIn(mouseX: Float, mouseY: Float) = Unit

    override var mouseMoveOut: (mouseX: Float, mouseY: Float) -> Unit = ::onMouseMoveOut

    override fun onMouseMoveOut(mouseX: Float, mouseY: Float) = Unit

    override fun onMouseMove(mouseX: Float, mouseY: Float) {
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
                mouseMoveOut(mouseX, mouseY)
            }
        }
    }

    override var mouseMove: (mouseX: Float, mouseY: Float) -> Unit = ::onMouseMove

    override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse) {
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

    override var mouseClick: (mouseX: Float, mouseY: Float, button: Mouse) -> Unit = ::onMouseClick

    override fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse) {
        if (!active) return
        if (button == Mouse.LEFT) dragging = false
        for (element in handleElements) {
            element.mouseRelease(mouseX, mouseY, button)
        }
    }

    override var mouseRelease: (mouseX: Float, mouseY: Float, button: Mouse) -> Unit = ::onMouseRelease

    override var dragging: Boolean = false

    override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) {
        if (!active || !dragging) return
        for (element in handleElements) {
            element.mouseDragging(mouseX, mouseY, button, deltaX, deltaY)
        }
    }

    override var mouseDragging: (mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) -> Unit =
        ::onMouseDragging

    override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): Unit {
        if (!active) return
        for (element in handleElements) {
            element.mouseScrolling(mouseX, mouseY, amount)
        }
    }

    override var mouseScrolling: (mouseX: Float, mouseY: Float, amount: Float) -> Unit = ::onMouseScrolling

    override fun onKeyPress(keyCode: KeyCode) {
        if (!active) return
        for (element in handleElements) {
            element.keyPress(keyCode)
        }
    }

    override var keyPress: (keyCode: KeyCode) -> Unit = ::onKeyPress

    override fun onKeyRelease(keyCode: KeyCode) {
        if (!active) return
        for (element in handleElements) {
            element.keyRelease(keyCode)
        }
    }

    override var keyRelease: (keyCode: KeyCode) -> Unit = ::onKeyRelease

    override fun onCharTyped(chr: Char) {
        if (!active) return
        for (element in handleElements) {
            element.charTyped(chr)
        }
    }

    override var charTyped: (chr: Char) -> Unit = ::onCharTyped
}