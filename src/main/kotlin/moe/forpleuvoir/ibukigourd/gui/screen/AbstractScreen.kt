@file:Suppress("MemberVisibilityCanBePrivate")

package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.element.*
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.input.*
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Dimension
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.mc

abstract class AbstractScreen(
    override var width: ElementDimension = MatchParent,
    override var height: ElementDimension = MatchParent,
) : AbstractElement(), Screen {

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
        arrange()
    }

    override fun measure(measureWidth: MeasureDimension, measureHeight: MeasureDimension): Dimension<Float> {
        when (width) {
            is MatchParent -> {
                transform.width = measureWidth.value
            }

            is WrapContent -> {
                //需要测量子元素
            }

            is Fixed       -> {
                transform.width = if (measureWidth.mode == MeasureDimension.Mode.AT_MOST)
                    (width as Fixed).value.coerceAtMost(measureWidth.value)
                else (width as Fixed).value
            }

            is Weight      -> {
                //需要测量子元素
            }
        }
        return super.measure(measureWidth, measureHeight)
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
        arrange()
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

fun screen(screenScope: Screen.() -> Unit): Screen {
    return object : AbstractScreen() {}.apply {
        screenScope(this)
    }
}