package moe.forpleuvoir.ibukigourd.gui.base.state

import moe.forpleuvoir.ibukigourd.gui.base.element.UserInteractionHandler
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.util.NextAction


open class StateMachineManager(initialState: State) : UserInteractionHandler {

    var currentState: State = initialState
        set(value) {
            field.onExit()
            field = value
            field.onEnter()
        }

    override fun onMouseMoveIn(mouseX: Float, mouseY: Float) {
        currentState.onMouseMove(mouseX, mouseY)
    }

    override fun onMouseMoveOut(mouseX: Float, mouseY: Float) {
        currentState.onMouseMoveOut(mouseX, mouseY)
    }

    override fun onMouseMove(mouseX: Float, mouseY: Float): NextAction {
        return currentState.onMouseMove(mouseX, mouseY)
    }

    override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
        return currentState.onMouseClick(mouseX, mouseY, button)
    }

    override fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
        return currentState.onMouseRelease(mouseX, mouseY, button)
    }

    override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float): NextAction {
        return currentState.onMouseDragging(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction {
        return currentState.onMouseScrolling(mouseX, mouseY, amount)
    }

    override fun onKeyPress(keyCode: KeyCode): NextAction {
        return currentState.onKeyPress(keyCode)
    }

    override fun onKeyRelease(keyCode: KeyCode): NextAction {
        return currentState.onKeyRelease(keyCode)
    }

    override fun onCharTyped(chr: Char): NextAction {
        return currentState.onCharTyped(chr)
    }

    override fun tick() {
        currentState.tick()
    }


}