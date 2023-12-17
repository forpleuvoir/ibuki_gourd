package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.ibukigourd.input.Mouse

interface State {
    fun onEnter() = Unit

    fun onExit() = Unit

    fun onTick() = Unit

    fun onMouseMove(x: Float, y: Float) = Unit

    fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) = Unit
}

open class StateMachineManager(initialState: State) {

    var currentState: State = initialState
        set(value) {
            field.onExit()
            field = value
            field.onEnter()
        }

    fun onTick() {
        currentState.onTick()
    }

    fun onMouseMove(x: Float, y: Float) {
        currentState.onMouseMove(x, y)
    }

    fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) {
        currentState.onMouseDragging(mouseX, mouseY, button, deltaX, deltaY)
    }

}