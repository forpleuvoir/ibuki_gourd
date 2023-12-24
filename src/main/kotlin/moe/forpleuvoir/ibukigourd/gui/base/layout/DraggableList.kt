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
import moe.forpleuvoir.ibukigourd.util.moveElement
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
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

    var elementSwap: (oldIndex: Int, newIndex: Int) -> Unit = { _, _ -> }

    var targetIndex: Int = -1

    private val idleState: State = object : State {

        override fun onTick() {
            if (pressed) {
                stateMachineManager.currentState = pressedState
            }
        }
    }

    //TODO("鼠标滚动时拖拽中的元素也要跟着移动")
    private val draggingState: State = object : State {
        override fun onEnter() {
            draggingElement = elements.find { it.mouseHover() }
            draggingElement?.transform?.translate(draggingOffset)
            targetIndex = subElements.indexOf(draggingElement)
        }

        override fun onExit() {
            if (targetIndex != -1 && targetIndex != subElements.indexOf(draggingElement)) {
                subElements.moveElement(subElements.indexOf(draggingElement), targetIndex)
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
            val x = mouseX + deltaX
            val y = mouseY + deltaY
            val draggingIndex = elementIndexOf(draggingElement!!)
            arrangeElements.filter { it != draggingElement }.minByOrNull { element ->
                arrangement.switch({
                    //在元素内
                    if (y in element.transform.worldTop..element.transform.worldBottom) {
                        0f
                    } else if (y < element.transform.worldTop) {//在元素上
                        element.transform.worldTop - y
                    } else {//在元素下
                        y - element.transform.worldBottom
                    }
                }, {
                    //在元素内
                    if (x in element.transform.worldLeft..element.transform.worldRight) {
                        0f
                    } else if (x < element.transform.worldLeft) {//在元素上
                        element.transform.worldLeft - x
                    } else {//在元素下
                        x - element.transform.worldRight
                    }
                })
            }?.let { element ->
                var index = elementIndexOf(element)
                if (index < draggingIndex) {
                    index = (index + 1).coerceAtLeast(0)
                }
                targetIndex = arrangement.switch({
                    //在元素内
                    if (y in element.transform.worldTop..element.transform.worldBottom) {
                        if (y < element.transform.worldCenter.y) index - 1
                        else index
                    } else if (y < element.transform.worldTop) {//在元素上
                        index - 1
                    } else {//在元素下
                        index
                    }
                }, {
                    //在元素内
                    if (x in element.transform.worldLeft..element.transform.worldRight) {
                        if (x < element.transform.worldCenter.x) index - 1
                        else index
                    } else if (x < element.transform.worldLeft) {//在元素上
                        index - 1
                    } else {//在元素下
                        index
                    }
                })
                if (targetIndex == elementIndexOf(draggingElement!!)) targetIndex = -1
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
        val thickness = spacing.coerceAtLeast(1f)
        //TODO("颜色待修改")
        arrangement.switch({
            if (targetIndex > elementIndexOf(draggingElement!!)) {
                vertex(0f, element.transform.height, 0f)
            } else {
                vertex(0f, -thickness, 0f)
            }.let {
                renderRect(
                    renderContext.matrixStack,
                    rect(element.transform.worldPosition + it, draggingElement!!.transform.width, thickness),
                    Colors.BLACK
                )
            }
        }, {
            if (targetIndex > elementIndexOf(draggingElement!!)) {
                vertex(element.transform.width, 0f, 0f)
            } else {
                vertex(-thickness, -1f, 0f)
            }.let {
                renderRect(
                    renderContext.matrixStack,
                    rect(element.transform.worldPosition + it, thickness, draggingElement!!.transform.height),
                    Colors.BLACK
                )
            }
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