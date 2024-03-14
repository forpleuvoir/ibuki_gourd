package moe.forpleuvoir.ibukigourd.gui.base.state

import moe.forpleuvoir.ibukigourd.gui.base.event.MouseEnterEvent
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Mouse

abstract class AbstractState(override val name: String = "nameless") : State {

    override fun onEnter() = Unit

    override fun onExit() = Unit

    override fun onRender(renderContext: RenderContext) = Unit

    override fun tick() = Unit

    override fun onMouseMoveEnter(event: MouseEnterEvent) = Unit

    override fun onMouseMoveOut(mouseX: Float, mouseY: Float) = Unit

    override fun onMouseMove(mouseX: Float, mouseY: Float) = Unit

    override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse) = Unit

    override fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse) = Unit

    override fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) = Unit

    override fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float) = Unit

    override fun onKeyPress(keyCode: KeyCode) = Unit

    override fun onKeyRelease(keyCode: KeyCode) = Unit

    override fun onCharTyped(chr: Char) = Unit
}