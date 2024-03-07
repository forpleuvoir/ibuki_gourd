@file:Suppress("MemberVisibilityCanBePrivate")

package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.element.*
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.input.*
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.arrange.Orientation
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.mc

abstract class AbstractScreen(
    width: ElementDimension = MatchParent,
    height: ElementDimension = MatchParent,
    orientation: Orientation = Orientation.Vertical
) : LinearLayout(width, height, orientation), Screen {

    override val screen: () -> Screen get() = { this }

    override var parent: () -> Element = { this }

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

    override fun init() {
        super.init()
    }

    override fun measure(elementMeasureDimension: ElementMeasureDimension): ElementMeasureDimension {
        val (mWidth, mHeight) = elementMeasureDimension
        when (width) {
            is Fixed           -> transform.width = (width as Fixed).value
            FillRemainingSpace -> transform.width = mWidth.value
            MatchParent        -> transform.width = mWidth.value
            is Weight          -> transform.width = mWidth.value
            is Percentage      -> transform.width = mWidth.value * (width as Percentage).value
            is WrapContent     -> TODO()
        }

        when (height) {
            is Fixed           -> transform.height = (height as Fixed).value
            FillRemainingSpace -> transform.height = mHeight.value
            MatchParent        -> transform.height = mHeight.value
            is Weight          -> transform.height = mHeight.value
            is Percentage      -> transform.height = mHeight.value * (height as Percentage).value
            is WrapContent     -> TODO()
        }

    }

    override fun tick() {
        preMouseX = this.mouseX
        preMouseY = this.mouseY
        this.mouseX = mc.mouseX
        this.mouseY = mc.mouseY
        super.tick()
    }

    override fun onMouseMove(mouseX: Float, mouseY: Float): NextAction {
        if (!active) return NextAction.Cancel
        preMouseX = this.mouseX
        preMouseY = this.mouseY
        this.mouseX = mouseX
        this.mouseY = mouseY
        return super<LinearLayout>.onMouseMove(mouseX, mouseY)
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

    }


    override var resize: (width: Int, height: Int) -> Unit = ::onResize

    override fun onKeyPress(keyCode: KeyCode): NextAction {
        if (keyCode == Keyboard.ESCAPE && shouldCloseOnEsc) {
            close()
            return NextAction.Cancel
        }
        return super<LinearLayout>.onKeyPress(keyCode)
    }

}

fun screen(screenScope: Screen.() -> Unit): Screen {
    return object : AbstractScreen() {}.apply {
        screenScope(this)
    }
}