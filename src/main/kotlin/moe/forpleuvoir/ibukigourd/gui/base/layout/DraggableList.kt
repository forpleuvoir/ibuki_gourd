package moe.forpleuvoir.ibukigourd.gui.base.layout

import com.mojang.blaze3d.platform.GlStateManager
import moe.forpleuvoir.ibukigourd.gui.base.*
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.helper.renderRoundRect
import moe.forpleuvoir.ibukigourd.render.helper.useBlend
import moe.forpleuvoir.ibukigourd.util.NextAction
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
    arrangement: Arrangement = Arrangement.Vertical,
    scrollerThickness: Float = 10f,
) : ListLayout(width, height, padding, showScroller, showBackground, arrangement, scrollerThickness) {

    var elementOrderSwap: (oldIndex: Int, newIndex: Int) -> Unit = { _, _ -> }

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
        }

        override fun onExit() {
            draggingElement?.transform?.translate(-draggingOffset)
            draggingElement = null
        }

        override fun onTick() {
            if (!pressed) {
                stateMachineManager.currentState = idleState
            }
        }

        override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) {
            draggingElement?.transform?.translate(deltaX, deltaY, 0f)
        }

    }

    private val pressedState: State = object : State {

        override fun onTick() {
            if (screen().mousePosition != screen().preMousePosition) {
                draggingCounter = 0
            }
            if (draggingCounter == draggingDelay) {
                stateMachineManager.currentState = draggingState
            }
            draggingCounter++
        }

    }

    /**
     * State Machine Manager
     * @param draggableList DraggableList
     * @constructor
     */
    private val stateMachineManager = StateMachineManager(idleState)


    val state by stateMachineManager::currentState

    private var pressed: Boolean = false

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
        if (super.onMouseClick(mouseX, mouseY, button) == NextAction.Cancel) return NextAction.Cancel

        if (button == Mouse.LEFT && mouseHoverContent()) {
            pressed = true
            renderElements.find { it.mouseHover() }?.let {
                draggingElement = it
                return NextAction.Cancel
            }
        }
        return NextAction.Continue
    }


    override fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
        if (super.onMouseRelease(mouseX, mouseY, button) == NextAction.Cancel) return NextAction.Cancel
        if (button == Mouse.LEFT && pressed) {
            pressed = false
        }
        swapElements(mouseX, mouseY)
        return super.onMouseRelease(mouseX, mouseY, button)
    }

    override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float): NextAction {
        if (super.onMouseDragging(mouseX, mouseY, button, deltaX, deltaY) == NextAction.Cancel) return NextAction.Cancel
        return NextAction.Continue
    }

    override fun onMouseMove(mouseX: Float, mouseY: Float): NextAction {
        if (super.onMouseMove(mouseX, mouseY).value) return NextAction.Cancel
        stateMachineManager.currentState.onMouseMove(mouseX, mouseY)
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
            it.render(renderContext)
            useBlend(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.DST_ALPHA) {
                renderRoundRect(renderContext.matrixStack, it.transform.asWorldRect, Color(0x7F8CECFF), 2)
            }
        }
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