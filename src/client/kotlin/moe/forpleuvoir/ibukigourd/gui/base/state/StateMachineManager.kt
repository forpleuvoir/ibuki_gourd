package moe.forpleuvoir.ibukigourd.gui.base.state

import moe.forpleuvoir.ibukigourd.gui.base.element.UserInteractionHandler
import moe.forpleuvoir.ibukigourd.gui.base.event.*

open class StateMachineManager(initialState: State) : UserInteractionHandler {

    var currentState: State = initialState
        set(value) {
            field.onExit()
            field = value
            field.onEnter()
        }

    override fun onMouseEnter(event: MouseEnterEvent) {
        currentState.onMouseEnter(event)
    }

    override fun onMouseLeave(event: MouseLeaveEvent) {
        currentState.onMouseLeave(event)
    }

    override fun onMouseMove(event: MouseMoveEvent) {
        currentState.onMouseMove(event)
    }

    override fun onMouseClick(event: MousePressEvent) {
        currentState.onMouseClick(event)
    }

    override fun onFocused(event: FocusedEvent) {
        currentState.onFocused(event)
    }

    override fun onMouseRelease(event: MouseReleaseEvent) {
        currentState.onMouseRelease(event)
    }

    override fun onMouseDragging(event: MouseDragEvent) {
        currentState.onMouseDragging(event)
    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        currentState.onMouseScrolling(event)
    }

    override fun onKeyPress(event: KeyPressEvent) {
        currentState.onKeyPress(event)
    }

    override fun onKeyRelease(event: KeyReleaseEvent) {
        currentState.onKeyRelease(event)
    }

    override fun onCharTyped(event: CharTypedEvent) {
        currentState.onCharTyped(event)
    }

    override fun tick() {
        currentState.tick()
    }


}