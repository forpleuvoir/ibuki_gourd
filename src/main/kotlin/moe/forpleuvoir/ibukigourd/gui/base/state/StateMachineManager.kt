package moe.forpleuvoir.ibukigourd.gui.base.state

import moe.forpleuvoir.ibukigourd.gui.base.element.UserInteractionHandler
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseEnterEvent
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Mouse

open class StateMachineManager(initialState: State) : UserInteractionHandler {

    var currentState: State = initialState
        set(value) {
            field.onExit()
            field = value
            field.onEnter()
        }

    override fun onMouseMoveEnter(event: MouseEnterEvent) {
        currentState.onMouseMove(mouseX, mouseY)
    }

    override fun onMouseMoveOut(mouseX: Float, mouseY: Float) {
        currentState.onMouseMoveOut(mouseX, mouseY)
    }

    override fun onMouseMove(mouseX: Float, mouseY: Float) {
        currentState.onMouseMove(mouseX, mouseY)
    }

    override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse) {
        currentState.onMouseClick(mouseX, mouseY, button)
    }

    override fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse) {
        currentState.onMouseRelease(mouseX, mouseY, button)
    }

    override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) {
        currentState.onMouseDragging(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float) {
        currentState.onMouseScrolling(mouseX, mouseY, amount)
    }

    override fun onKeyPress(keyCode: KeyCode) {
        currentState.onKeyPress(keyCode)
    }

    override fun onKeyRelease(keyCode: KeyCode) {
        currentState.onKeyRelease(keyCode)
    }

    override fun onCharTyped(chr: Char) {
        currentState.onCharTyped(chr)
    }

    override fun tick() {
        currentState.tick()
    }


}