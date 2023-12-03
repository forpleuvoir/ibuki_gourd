package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.base.mouseHoverContent
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.util.NextAction
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 应该为下一级元素可被拖动
 * @constructor
 */
class DraggableList(
    width: Float? = null,
    height: Float? = null,
    padding: Margin? = Margin(6),
    showScroller: Boolean = true,
    showBackground: Boolean = true,
    arrangement: Arrangement = Arrangement.Vertical,
    scrollerThickness: Float = 10f,
) : ListLayout(width, height, padding, showScroller, showBackground, arrangement, scrollerThickness) {

    var elementOrderSwap: (oldIndex: Int, newIndex: Int) -> Unit = { _, _ -> }

    var draggingElement: Element? = null
        private set(value) {
            if (value == null) {
                draggingCounter = 0
                field?.transform?.translate(-draggingOffset)
            } else {
                draggingElementOldPosition = value.transform.position

            }
            field = value
        }

    private var draggingElementOldPosition: Vector3<out Float>? = null

    var draggingOffset: Vector3<out Float> = vertex(5f, -5f, 0f)

    var draggingDelay: Int = 10

    private var draggingCounter: Int = 0
        private set(value) {
            field = value
            if (draggingCounter == draggingDelay) {
                draggingElement?.transform?.translate(draggingOffset)
            }
        }

    override fun tick() {
        super.tick()
        if (draggingElement != null && draggingCounter < draggingDelay) {
            draggingCounter++
        }
    }

    override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
        if (super.onMouseClick(mouseX, mouseY, button) == NextAction.Cancel) return NextAction.Cancel
        if (button == Mouse.LEFT && mouseHoverContent()) {
            renderElements.find { it.mouseHover() }?.let {
                draggingElement = it
                return NextAction.Cancel
            }
        }
        return NextAction.Continue
    }


    override fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
        if (super.onMouseRelease(mouseX, mouseY, button) == NextAction.Cancel) return NextAction.Cancel
        if (draggingElement != null) {
            //TODO 交换位置
            draggingElement = null
        }
        return super.onMouseRelease(mouseX, mouseY, button)
    }

    override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float): NextAction {
        if (super.onMouseDragging(mouseX, mouseY, button, deltaX, deltaY) == NextAction.Cancel) return NextAction.Cancel
        if (draggingCounter == draggingDelay) {
            draggingElement?.transform?.translate(this.arrangement.switch(vertex(0f, deltaY, 0f), vertex(deltaX, 0f, 0f)))

        }
        return NextAction.Continue
    }

    override fun onMouseMove(mouseX: Float, mouseY: Float): NextAction {
        if (draggingCounter != draggingDelay) draggingCounter = 0
        return super.onMouseMove(mouseX, mouseY)
    }

    override fun onRender(renderContext: RenderContext) {
        if (!visible) return
        renderBackground.invoke(renderContext)
        renderContext.scissor(super.contentRect(true)) {
            renderElements.filter { it != scrollerBar || !it.fixed || it != draggingElement }.forEach { it.render(renderContext) }
        }
        fixedElements.forEach { it.render(renderContext) }
        draggingElement?.render?.invoke(renderContext)
        scrollerBar.render(renderContext)
        renderOverlay.invoke(renderContext)
    }

}


@OptIn(ExperimentalContracts::class)
fun ElementContainer.draggableList(
    width: Float? = null,
    height: Float? = null,
    arrangement: Arrangement = Arrangement.Vertical,
    padding: Margin? = Margin(6),
    showScroller: Boolean = true,
    showBackground: Boolean = true,
    scrollerThickness: Float = 10f,
    scope: ListLayout.() -> Unit = {}
): DraggableList {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(DraggableList(width, height, padding, showScroller, showBackground, arrangement, scrollerThickness).apply(scope))
}