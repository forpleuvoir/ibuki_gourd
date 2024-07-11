package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.element.DrawableElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat
import net.minecraft.client.gui.DrawContext


abstract class WidgetContainer : IGWidget(), DrawableElementContainer<IGWidget> {

    //------------ Measure ------------\\

    override fun measure(constraints: Constraints): SizeFloat {
        TODO("Not yet implemented")
    }

    override fun minIntrinsicWidth(height: Float): Float {
        TODO("Not yet implemented")
    }

    override fun maxIntrinsicWidth(height: Float): Float {
        TODO("Not yet implemented")
    }

    override fun minIntrinsicHeight(width: Float): Float {
        TODO("Not yet implemented")
    }

    override fun maxIntrinsicHeight(width: Float): Float {
        TODO("Not yet implemented")
    }


    //------------ Container ------------\\

    private val children = mutableListOf<IGWidget>()

    override fun children(): List<IGWidget> = children

    override fun addChild(child: IGWidget): IGWidget = child.also {
        it.transform.parent = { this.transform }
        it.parent = { this }
        it.layer = this.layer
        children.add(it)
    }


    //------------ Drawable ------------\\

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super<DrawableElementContainer>.render(context, mouseX, mouseY, delta)
    }

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) =
        super<IGWidget>.onRenderBackground(context, mouseX, mouseY, delta)

    override fun onRenderOverlay(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) =
        super<IGWidget>.onRenderOverlay(context, mouseX, mouseY, delta)


    //------------ Tickable ------------\\

    override fun tick() = super<DrawableElementContainer>.tick()


    //------------ Element ------------\\

    override fun onMouseEnter(event: MouseEnterEvent) = Unit

    override fun onMouseLeave(event: MouseLeaveEvent) = Unit

    override fun onMouseMove(event: MouseMoveEvent) {
        super<IGWidget>.onMouseMove(event)
        super<DrawableElementContainer>.onMouseMove(event)
    }

    override fun onMouseClick(event: MousePressEvent) {
        super<IGWidget>.onMouseClick(event)
        super<DrawableElementContainer>.onMouseClick(event)
    }

    override fun onFocused(event: FocusedEvent) {
        super<DrawableElementContainer>.onFocused(event)
        super<IGWidget>.onFocused(event)
    }

    override fun onMouseRelease(event: MouseReleaseEvent) {
        super<IGWidget>.onMouseRelease(event)
        super<DrawableElementContainer>.onMouseRelease(event)
    }

    override fun onMouseDragging(event: MouseDragEvent) {
        super<DrawableElementContainer>.onMouseDragging(event)
    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        super<DrawableElementContainer>.onMouseScrolling(event)
    }

    override fun onKeyPress(event: KeyPressEvent) {
        super<DrawableElementContainer>.onKeyPress(event)
    }

    override fun onKeyRelease(event: KeyReleaseEvent) {
        super<DrawableElementContainer>.onKeyRelease(event)
    }

    override fun onCharTyped(event: CharTypedEvent) {
        super<DrawableElementContainer>.onCharTyped(event)
    }
}