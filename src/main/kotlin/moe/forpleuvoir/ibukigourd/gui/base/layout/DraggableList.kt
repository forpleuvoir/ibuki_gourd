package moe.forpleuvoir.ibukigourd.gui.base.layout

import com.mojang.blaze3d.platform.GlStateManager
import moe.forpleuvoir.ibukigourd.gui.base.*
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.input.notEquals
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.renderRect
import moe.forpleuvoir.ibukigourd.render.helper.renderRoundRect
import moe.forpleuvoir.ibukigourd.render.helper.useBlend
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.abs

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

    var elementSwap: (oldIndex: Int, newIndex: Int) -> Unit = { _, _ -> }

    var targetIndex: Int = -1

    private val idleState: State = object : State {

        override fun onTick() {
            if (pressed) {
                stateMachineManager.currentState = pressedState
            }
        }
    }

    private val draggingState: State = object : State {
        override fun onEnter() {
            draggingElement = elements.find { it.mouseHover() }
            draggingElement?.transform?.translate(draggingOffset)
            targetIndex = subElements.indexOf(draggingElement)
        }

        override fun onExit() {
            if (targetIndex != -1 && targetIndex != subElements.indexOf(draggingElement)) {
                subElements.removeAt(subElements.indexOf(draggingElement))
                subElements.add(targetIndex, draggingElement!!)
                elementSwap(targetIndex, subElements.indexOf(draggingElement))
            }
            arrange()
            draggingElement = null
            targetIndex = -1
        }

        override fun onTick() {
            if (!pressed) {
                stateMachineManager.currentState = idleState
            }
        }

        override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) {
            draggingElement?.transform?.translate(arrangement.switch(vertex(0f, deltaY, 0f), vertex(deltaX, 0f, 0f)))
            // TODO("鼠标位置附近最近的两个元素,如果在元素内则选择当前元素的前后")
            arrangeElements.filter { it != draggingElement }.minByOrNull { element ->
                arrangement.switch({
                    if (element.transform.worldTop < mouseY) {
                        abs(element.transform.worldBottom - mouseY)
                    } else {
                        abs(element.transform.worldTop - mouseY)
                    }
                }, {
                    if (element.transform.worldLeft < mouseX) {
                        abs(element.transform.worldRight - mouseX)
                    } else {
                        abs(element.transform.worldLeft - mouseX)
                    }
                })
            }?.let { element ->
                val index = elementIndexOf(element)
                targetIndex = arrangement.switch({
                    if (element.transform.worldTop < mouseY) {
                        index + 1
                    } else {
                        index
                    }
                }, {
                    if (element.transform.worldLeft < mouseX) {
                        index + 1
                    } else {
                        index
                    }
                })
                if (index == elementIndexOf(draggingElement!!)) targetIndex = -1
            }
        }

    }

    private val pressedState: State = object : State {

        override fun onTick() {
            if (!pressed) {
                stateMachineManager.currentState = idleState
            }
            if (screen().preMousePosition notEquals screen().mousePosition) {
                draggingCounter = 0
            }
            if (draggingCounter == draggingDelay) {
                stateMachineManager.currentState = draggingState
            }
            draggingCounter++
        }

    }

    // 管理 DraggableList 的各种状态的状态机管理器，默认状态为 [idleState]
    private val stateMachineManager = StateMachineManager(idleState)

    val state by stateMachineManager::currentState

    private var pressed: Boolean = false

    var draggingElement: Element? = null
        private set

    private var draggingElementOldPosition: Vector3<out Float>? = null

    var draggingOffset: Vector3<out Float> = vertex(5f, -5f, 0f)

    var draggingDelay: Int = 10

    private var draggingCounter: Int = 0
        private set(value) {
            field = value

        }

    override fun tick() {
        super.tick()
        stateMachineManager.onTick()
    }


    fun swapElements(mouseX: Float, mouseY: Float) {
        if (draggingElement != null) {
            //TODO 交换位置
            draggingElement = null
        }
    }

    override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
        super.onMouseClick(mouseX, mouseY, button).ifCancel { return NextAction.Cancel }
        if (button == Mouse.LEFT && mouseHoverContent()) {
            pressed = true
            return NextAction.Cancel
        }
        return NextAction.Continue
    }


    override fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
        super.onMouseRelease(mouseX, mouseY, button).ifCancel { return NextAction.Cancel }
        if (button == Mouse.LEFT && pressed) {
            pressed = false
        }
//        swapElements(mouseX, mouseY)
        return super.onMouseRelease(mouseX, mouseY, button)
    }

    override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float): NextAction {
        super.onMouseDragging(mouseX, mouseY, button, deltaX, deltaY).ifCancel { return NextAction.Cancel }
        stateMachineManager.onMouseDragging(mouseX, mouseY, button, deltaX, deltaY)
        return NextAction.Continue
    }

    override fun onMouseMove(mouseX: Float, mouseY: Float): NextAction {
        super.onMouseMove(mouseX, mouseY).ifCancel { return NextAction.Cancel }
        stateMachineManager.onMouseMove(mouseX, mouseY)
        return super.onMouseMove(mouseX, mouseY)
    }

    override fun onRender(renderContext: RenderContext) {
        if (!visible) return
        renderBackground.invoke(renderContext)
        renderContext.scissor(super.contentRect(true)) {
            renderElements.filter { it != scrollerBar || !it.fixed || it != draggingElement }.forEach { it.render(renderContext) }
        }
        fixedElements.forEach { it.render(renderContext) }
        scrollerBar.render(renderContext)
        draggingElement?.let {
            renderDraggingElement(renderContext, it)
            renderTargetIndex(renderContext, targetIndex)
        }

        renderOverlay.invoke(renderContext)
    }

    fun renderDraggingElement(renderContext: RenderContext, element: Element) {
        element.let {
            it.render(renderContext)
            useBlend(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.DST_ALPHA) {
                renderRoundRect(renderContext.matrixStack, it.transform.asWorldRect, Color(0x7F8CECFF), 2)
            }
        }
    }

    fun renderTargetIndex(renderContext: RenderContext, targetIndex: Int) {
        if (targetIndex == -1 || targetIndex == elementIndexOf(draggingElement!!)) return
        val element = subElements[targetIndex]
        arrangement.switch({
            renderRect(renderContext.matrixStack, rect(element.transform.worldPosition + vertex(0f, -1f, 0f), element.transform.width, 1f), Colors.BLACK)
        }, {
            renderRect(renderContext.matrixStack, rect(element.transform.worldPosition + vertex(-1f, 0f, 0f), 1f, element.transform.height), Colors.BLACK)
        })
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