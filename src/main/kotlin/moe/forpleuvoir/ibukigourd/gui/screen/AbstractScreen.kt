@file:Suppress("MemberVisibilityCanBePrivate")

package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.input.MousePosition
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.mc

abstract class AbstractScreen(
    width: Int = mc.window.scaledWidth,
    height: Int = mc.window.scaledHeight
) : AbstractElement(), Screen {

    override val screen: () -> Screen get() = { this }

    override var parent: () -> Element = { this }

    init {
        this.transform.fixedWidth = true
        this.transform.fixedHeight = true
        this.transform.width = width.toFloat()
        this.transform.height = height.toFloat()
    }

    override val tipList = ArrayList<Tip>()

    override var maxTip: Int = 5

    override fun pushTip(tip: Tip): Boolean {
        if (tipList.contains(tip)) return false
        return if (tipList.size < maxTip)
            tipList.add(tip)
        else false
    }

    override fun popTip(tip: Tip): Boolean {
        return tipList.remove(tip)
    }

    override var parentScreen: Screen? = null

    override var focusedElement: Element? = null

    override var pauseGame: Boolean = false

    override var shouldCloseOnEsc: Boolean = true

    override var close: () -> Unit = ::onClose

    override val handleElements: List<Element>
        get() = buildList {
            addAll(tipList.sortedByDescending { it.priority })
            addAll(super.handleElements)
        }

    protected var mouseX: Float = 0f
    protected var mouseY: Float = 0f

    override val mousePosition: MousePosition = object : MousePosition {
        override val x: Float get() = mouseX
        override val y: Float get() = mouseY
    }

    protected var preMouseX: Float = 0f
    protected var preMouseY: Float = 0f

    override val preMousePosition = object : MousePosition {
        override val x: Float get() = preMouseX
        override val y: Float get() = preMouseY
    }

    override fun onMouseMove(mouseX: Float, mouseY: Float): NextAction {
        if (!active) return NextAction.Cancel
        preMouseX = this.mouseX
        preMouseY = this.mouseY
        this.mouseX = mouseX
        this.mouseY = mouseY
        return super<AbstractElement>.onMouseMove(mouseX, mouseY)
    }


    override fun onRender(renderContext: RenderContext) {
        if (!visible) return
        renderBackground.invoke(renderContext)
        for (element in renderElements) element.render(renderContext)
        tipList.sortedBy { it.renderPriority }.forEach {
            if (it.visible) it.render.invoke(renderContext)
        }
        renderOverlay.invoke(renderContext)
    }

    override fun onClose() {
        ScreenManager.setScreen(parentScreen)
        MouseCursor.current = MouseCursor.Cursor.ARROW_CURSOR
    }

    override fun onResize(width: Int, height: Int) {
        this.transform.width = width.toFloat()
        this.transform.height = height.toFloat()
        layout.arrange(this.subElements, this.margin, this.padding)
    }

    override var resize: (width: Int, height: Int) -> Unit = ::onResize

    override fun onKeyPress(keyCode: KeyCode): NextAction {
        if (keyCode == Keyboard.ESCAPE && shouldCloseOnEsc) {
            close()
            return NextAction.Cancel
        }
        return super<AbstractElement>.onKeyPress(keyCode)
    }


}