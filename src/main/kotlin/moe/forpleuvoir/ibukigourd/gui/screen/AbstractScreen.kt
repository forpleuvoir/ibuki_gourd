@file:Suppress("MemberVisibilityCanBePrivate")

package moe.forpleuvoir.ibukigourd.gui.screen

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementDimension
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementMeasureDimension
import moe.forpleuvoir.ibukigourd.gui.base.element.MatchParent
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.MouseCursor

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
            addAll(super.handleElements)
        }

    override val eventBus = ScreenEventBus()


    override fun measure(elementMeasureDimension: ElementMeasureDimension): ElementMeasureDimension {

    }

    override fun onMouseClick(event: MousePressEvent) {
        eventBus.broadcast(event)
    }

    override fun onMouseRelease(event: MouseReleaseEvent) {
        eventBus.broadcast(event)
    }

    override fun onMouseDragging(event: MouseDragEvent) {
        eventBus.broadcast(event)
    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        eventBus.broadcast(event)
    }

    override fun onMouseEnter(event: MouseEnterEvent) {
        eventBus.broadcast(event)
    }

    override fun onMouseLeave(event: MouseLeaveEvent) {
        eventBus.broadcast(event)
    }

    override fun onMouseMove(event: MouseMoveEvent) {
        eventBus.broadcast(event)
    }

    override fun onKeyPress(event: KeyPressEvent) {
        eventBus.broadcast(event)
        if (!event.used && event.keyCode == Keyboard.ESCAPE && shouldCloseOnEsc) {
            close()
            event.use()
        }
    }

    override fun onKeyRelease(event: KeyReleaseEvent) {
        eventBus.broadcast(event)
    }

    override fun onCharTyped(event: CharTypedEvent) {
        eventBus.broadcast(event)
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

}

fun screen(screenScope: Screen.() -> Unit): Screen {
    return object : AbstractScreen() {}.apply {
        screenScope(this)
    }
}