package moe.forpleuvoir.ibukigourd.gui.base.state

import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.util.NextAction

abstract class AbstractState(override val name: String) : State {

    override fun onEnter() = Unit

    override fun onExit() = Unit

    override fun onRender(renderContext: RenderContext) = Unit

    override fun tick() = Unit

    override fun onMouseMoveIn(mouseX: Float, mouseY: Float) = Unit

    override fun onMouseMoveOut(mouseX: Float, mouseY: Float) = Unit

    override fun onMouseMove(mouseX: Float, mouseY: Float): NextAction = NextAction.Continue

    override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction = NextAction.Continue

    override fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse): NextAction = NextAction.Continue

    override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float): NextAction = NextAction.Continue

    override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction = NextAction.Continue

    override fun onKeyPress(keyCode: KeyCode): NextAction = NextAction.Continue

    override fun onKeyRelease(keyCode: KeyCode): NextAction = NextAction.Continue

    override fun onCharTyped(chr: Char): NextAction = NextAction.Continue
}