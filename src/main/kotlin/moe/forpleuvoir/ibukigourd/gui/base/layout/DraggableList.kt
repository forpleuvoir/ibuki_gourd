package moe.forpleuvoir.ibukigourd.gui.base.layout

import com.mojang.blaze3d.platform.GlStateManager
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.base.state.AbstractState
import moe.forpleuvoir.ibukigourd.gui.base.state.State
import moe.forpleuvoir.ibukigourd.gui.base.state.StateMachineManager
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.input.MousePosition
import moe.forpleuvoir.ibukigourd.input.notEquals
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.arrange.Orientation
import moe.forpleuvoir.ibukigourd.render.base.arrange.peek
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.helper.renderRect
import moe.forpleuvoir.ibukigourd.render.helper.renderRoundRect
import moe.forpleuvoir.ibukigourd.render.helper.useBlend
import moe.forpleuvoir.ibukigourd.render.shape.rectangle.Rect
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.moveElement
import moe.forpleuvoir.nebula.common.color.Color
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
    Orientation: Orientation = Orientation.Vertical,
    scrollerThickness: Float = 10f,
) : ListLayout(width, height, padding, showScroller, showBackground, Orientation, scrollerThickness) {

    var elementSwap: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> }

    var targetIndex: Int = -1

    override var onElementTranslate: (Element, Vector3<Float>) -> Unit = { e, v ->
        if (e != draggingElement) e.transform.translateTo(v)
    }

    private val idleState: State = object : AbstractState("idle_state") {

        override fun onMouseClick(event: MousePressEvent) {
            event.used { return }
            if (event.button == Mouse.LEFT && mouseHover() && elements.find { it.mouseHover() } != null) {
                stateMachineManager.currentState = pressedState
                event.use()
            }
        }
    }

    private val draggingState: State = object : AbstractState("dragging_state") {
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
            draggingElement = null
            targetIndex = -1
            arrange()
        }

        override fun onMouseRelease(event: MouseReleaseEvent): NextAction {
            stateMachineManager.currentState = idleState
            return NextAction.Continue
        }

        override fun onMouseDragging(event: MouseDragEvent): NextAction {
            draggingElement?.transform?.translate(Orientation.peek(vertex(0f, deltaY, 0f), vertex(deltaX, 0f, 0f)))
            val x = mouseX + deltaX
            val y = mouseY + deltaY
            val draggingIndex = elementIndexOf(draggingElement!!)
            layoutElements.filter { it != draggingElement }.minByOrNull { element ->
                Orientation.peek({
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
                targetIndex = Orientation.peek({
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
            return NextAction.Continue
        }

    }

    private val pressedState: State = object : AbstractState("pressed_state") {

        private lateinit var enterMousePosition: MousePosition

        override fun onEnter() {
            enterMousePosition = object : MousePosition {
                override val x: Float = screen().mousePosition.x
                override val y: Float = screen().mousePosition.y
            }
        }

        override fun onExit() {
            draggingCounter = 0
        }

        override fun tick() {
            draggingCounter++
            if (enterMousePosition notEquals screen().mousePosition) {
                draggingCounter = 0
            }
            if (draggingCounter == draggingDelay) {
                stateMachineManager.currentState = draggingState
            }
        }

        override fun onMouseRelease(event: MouseReleaseEvent): NextAction {
            stateMachineManager.currentState = idleState
            return NextAction.Continue
        }

    }

    // 管理 DraggableList 的各种状态的状态机管理器，默认状态为 [idleState]
    private val stateMachineManager = StateMachineManager(idleState)

    val state: State by stateMachineManager::currentState

    var draggingElement: Element? = null
        private set


    var draggingOffset: Vector3<out Float> = vertex(5f, -5f, 0f)

    var draggingDelay: Int = 10

    var draggingCounter: Int = 0
        private set

    override fun tick() {
        super.tick()
        stateMachineManager.tick()
    }

    override fun onMouseClick(event: MousePressEvent): NextAction {
        super.onMouseClick().ifCancel { return NextAction.Cancel }
        return stateMachineManager.onMouseClick()
    }


    override fun onMouseRelease(event: MouseReleaseEvent): NextAction {
        super.onMouseRelease().ifCancel { return NextAction.Cancel }
        return stateMachineManager.onMouseRelease()
    }

    override fun onMouseDragging(event: MouseDragEvent): NextAction {
        super.onMouseDragging().ifCancel { return NextAction.Cancel }
        stateMachineManager.onMouseDragging()
        return NextAction.Continue
    }

    override fun onMouseMove(event: MouseMoveEvent): NextAction {
        super.onMouseMove().ifCancel { return NextAction.Cancel }
        stateMachineManager.onMouseMove()
        return super.onMouseMove()
    }

    override fun onMouseScrolling(event: MouseScrollEvent): NextAction {
        stateMachineManager.onMouseScrolling()
        return super.onMouseScrolling()
    }

    override fun onRender(renderContext: RenderContext) {
        if (!visible) return
        renderBackground.invoke(renderContext)
        val rect = super.contentBox(true)
        renderContext.scissor(rect) {
            renderElements.filter { it != scrollerBar || !it.fixed || it != draggingElement }.forEach { it.render(renderContext) }
        }
        fixedElements.forEach { it.render(renderContext) }
        scrollerBar.render(renderContext)
        draggingElement?.let {
            renderDraggingElement(renderContext, it)
            renderContext.scissor(rect) {
                renderTargetIndex(renderContext, targetIndex)
            }
        }
        renderOverlay.invoke(renderContext)
    }

    fun renderDraggingElement(renderContext: RenderContext, element: Element) {
        element.let {
            it.render(renderContext)
            useBlend(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.DST_ALPHA) {
                renderRoundRect(renderContext.matrixStack, it.transform.asWorldBox, Color(0x7F8CECFF), 2)
            }
        }
    }

    fun renderTargetIndex(renderContext: RenderContext, targetIndex: Int) {
        if (targetIndex == -1 || targetIndex == elementIndexOf(draggingElement!!)) return
        val element = subElements[targetIndex]
        val thickness = spacing.coerceAtLeast(1f)
        val color = Color(0x7F8CECFF)
        orientation.peek({
                             if (targetIndex > elementIndexOf(draggingElement!!)) {
                                 vertex(0f, element.transform.height, 0f)
                             } else {
                                 vertex(0f, -thickness, 0f)
                             }.let {
                                 renderRect(
                                     renderContext.matrixStack,
                                     Rect(element.transform.worldPosition + it, draggingElement!!.transform.width, thickness),
                                     color
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
                                     Rect(element.transform.worldPosition + it, thickness, draggingElement!!.transform.height),
                                     color
                                 )
                             }
                         })
    }

}


@OptIn(ExperimentalContracts::class)
fun ElementContainer.draggableList(
    width: Float? = null,
    height: Float? = null,
    Orientation: Orientation = Orientation.Vertical,
    padding: Margin? = Margin(6),
    showScroller: Boolean = true,
    showBackground: Boolean = true,
    scrollerThickness: Float = 10f,
    scope: ListLayout.() -> Unit = {}
): DraggableList {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return this.addElement(DraggableList(width, height, Orientation, padding, showScroller, showBackground, scrollerThickness, scope))
}

@OptIn(ExperimentalContracts::class)
fun DraggableList(
    width: Float? = null,
    height: Float? = null,
    Orientation: Orientation = Orientation.Vertical,
    padding: Margin? = Margin(6),
    showScroller: Boolean = true,
    showBackground: Boolean = true,
    scrollerThickness: Float = 10f,
    scope: ListLayout.() -> Unit = {}
): DraggableList {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return DraggableList(width, height, padding, showScroller, showBackground, Orientation, scrollerThickness).apply(scope)
}