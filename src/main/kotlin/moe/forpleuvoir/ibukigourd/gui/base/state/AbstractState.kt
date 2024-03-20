package moe.forpleuvoir.ibukigourd.gui.base.state

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext

abstract class AbstractState(override val name: String = "nameless") : State {

    override fun onEnter() = Unit

    override fun onExit() = Unit

    override fun onRender(renderContext: RenderContext) = Unit

    override fun tick() = Unit

    override fun onMouseEnter(event: MouseEnterEvent) = Unit

    override fun onMouseLeave(event: MouseLeaveEvent) = Unit

    override fun onMouseMove(event: MouseMoveEvent) = Unit

    override fun onMouseClick(event: MousePressEvent) = Unit

    override fun onMouseRelease(event: MouseReleaseEvent) = Unit

    override fun onMouseDragging(event: MouseDragEvent) = Unit

    override fun onMouseScrolling(event: MouseScrollEvent) = Unit

    override fun onKeyPress(event: KeyPressEvent) = Unit

    override fun onKeyRelease(event: KeyReleaseEvent) = Unit

    override fun onCharTyped(event: CharTypedEvent) = Unit
}