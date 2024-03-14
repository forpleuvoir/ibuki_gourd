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
        currentState.onMouseMove()
    }

    override fun onMouseLeave(event: MouseLeaveEvent) {
        currentState.onMouseLeave(event)
    }

    override fun onMouseMove(event: MouseMoveEvent) {
        currentState.onMouseMove()
    }

    override fun onMouseClick(event: MousePressEvent) {
        currentState.onMouseClick()
    }

    override fun onMouseRelease(event: MouseReleaseEvent) {
        currentState.onMouseRelease()
    }

    override fun onMouseDragging(event: MouseDragEvent) {
        currentState.onMouseDragging()
    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        currentState.onMouseScrolling()
    }

    override fun onKeyPress(event: KeyPressEvent) {
        currentState.onKeyPress()
    }

    override fun onKeyRelease(event: KeyReleaseEvent) {
        currentState.onKeyRelease()
    }

    override fun onCharTyped(event: CharTypedEvent) {
        currentState.onCharTyped()
    }

    override fun tick() {
        currentState.tick()
    }


}