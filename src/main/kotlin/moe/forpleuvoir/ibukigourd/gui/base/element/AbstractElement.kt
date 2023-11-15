package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.screen.Screen
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.util.NextAction

@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractElement : Element, AbstractElementContainer() {

    override val screen: () -> Screen
        get() {
            return if (parent() is Screen) {
                { parent() as Screen }
            } else {
                { parent().screen.invoke() }
            }
        }

    override var visible: Boolean = true

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
            if (value != null) {
                this.addElement(value)
            } else if (field != null)
                this.removeElement(field!!)
            field = value
        }

    override fun init() {
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
        for (element in renderElements) element.render(renderContext)
        renderOverlay.invoke(renderContext)
    }

    override var render: (renderContext: RenderContext) -> Unit = ::onRender

    override fun onRenderBackground(renderContext: RenderContext) = Unit

    override var renderBackground: (renderContext: RenderContext) -> Unit = ::onRenderBackground

    override fun onRenderOverlay(renderContext: RenderContext) = Unit

    override var renderOverlay: (renderContext: RenderContext) -> Unit = ::onRenderOverlay

    override val mouseMoveIn: (mouseX: Float, mouseY: Float) -> Unit = ::onMouseMoveIn
    override fun onMouseMoveIn(mouseX: Float, mouseY: Float) = Unit

    override val mouseMoveOut: (mouseX: Float, mouseY: Float) -> Unit = ::onMouseMoveOut

    override fun onMouseMoveOut(mouseX: Float, mouseY: Float) = Unit

    override fun onMouseMove(mouseX: Float, mouseY: Float) {
        if (!active) return
        for (element in handleElements) element.mouseMove(mouseX, mouseY)
        //上一帧不在元素内,这一帧在 触发 mouseMoveIn
        if (!visible) return
        screen().let {
            if (!mouseHover(it.preMousePosition) && mouseHover(it.mousePosition)) {
                mouseMoveIn(mouseX, mouseY)
            } else if (mouseHover(it.preMousePosition) && !mouseHover(it.mousePosition)) {
                mouseMoveOut(mouseX, mouseY)
            }
        }
    }

    override var mouseMove: (mouseX: Float, mouseY: Float) -> Unit = ::onMouseMove

    override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
        if (!active) return NextAction.Continue
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
            if (element.mouseClick(mouseX, mouseY, button) == NextAction.Cancel)
                return NextAction.Cancel
        }
        return NextAction.Continue
    }

    override var mouseClick: (mouseX: Float, mouseY: Float, button: Mouse) -> NextAction = ::onMouseClick

    override fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
        if (!active) return NextAction.Continue
        if (button == Mouse.LEFT) dragging = false
        for (element in handleElements) {
            if (element.mouseRelease(mouseX, mouseY, button) == NextAction.Cancel) return NextAction.Cancel
        }
        return NextAction.Continue
    }

    override var mouseRelease: (mouseX: Float, mouseY: Float, button: Mouse) -> NextAction = ::onMouseRelease

    override var dragging: Boolean = false

    override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float): NextAction {
        if (!active || !dragging) return NextAction.Continue
        for (element in handleElements) {
            if (element.mouseDragging(mouseX, mouseY, button, deltaX, deltaY) == NextAction.Cancel) return NextAction.Cancel
        }
        return NextAction.Continue
    }

    override var mouseDragging: (mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) -> NextAction =
        ::onMouseDragging

    override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction {
        if (!active) return NextAction.Continue
        for (element in handleElements) {
            if (element.mouseScrolling(mouseX, mouseY, amount) == NextAction.Cancel) return NextAction.Cancel
        }
        return NextAction.Continue
    }

    override var mouseScrolling: (mouseX: Float, mouseY: Float, amount: Float) -> NextAction = ::onMouseScrolling

    override fun onKeyPress(keyCode: KeyCode): NextAction {
        if (!active) return NextAction.Continue
        for (element in handleElements) {
            if (element.keyPress(keyCode) == NextAction.Cancel) return NextAction.Cancel
        }
        return NextAction.Continue
    }

    override var keyPress: (keyCode: KeyCode) -> NextAction = ::onKeyPress

    override fun onKeyRelease(keyCode: KeyCode): NextAction {
        if (!active) return NextAction.Continue
        for (element in handleElements) {
            if (element.keyRelease(keyCode) == NextAction.Cancel) return NextAction.Cancel
        }
        return NextAction.Continue
    }

    override var keyRelease: (keyCode: KeyCode) -> NextAction = ::onKeyRelease

    override fun onCharTyped(chr: Char): NextAction {
        if (!active) return NextAction.Continue
        for (element in handleElements) {
            if (element.charTyped(chr) == NextAction.Cancel) return NextAction.Cancel
        }
        return NextAction.Continue
    }

    override var charTyped: (chr: Char) -> NextAction = ::onCharTyped
}