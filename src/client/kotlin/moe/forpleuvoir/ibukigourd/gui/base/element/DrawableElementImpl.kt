package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext.Companion.toIGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.input.mousePosition
import net.minecraft.client.gui.DrawContext

abstract class DrawableElementImpl : DrawableElement {

    //------------ Vanilla Drawable ------------\\

    @Suppress("LocalVariableName")
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val (_mouseX, _mouseY) = context.client.mousePosition
        context.toIGDrawContext().tryRender {
            renderBackground.invoke(this, _mouseX, _mouseY, delta)
            render.invoke(this, _mouseX, _mouseY, delta)
            renderOverlay.invoke(this, _mouseX, _mouseY, delta)
        }
    }


    //------------ Tickable ------------\\

    override var tick: () -> Unit = ::onTick

    override fun onTick() {}


    //------------ IGDrawable ------------\\

    override var visible: Boolean = true

    override var renderPriority: Int = 0

    override var renderBackground: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRenderBackground

    abstract override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float)

    override var render: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRender

    abstract override fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float)

    override var renderOverlay: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRenderOverlay

    abstract override fun onRenderOverlay(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float)


    //------------ IGElement ------------\\

    override val screen: () -> IGScreen?
        get() {
            return if (parent() is IGScreen) {
                { parent() as IGScreen }
            } else {
                { parent()?.screen?.let { it() } }
            }
        }

    override var parent: () -> IGElement? = { null }

    private var _layer: GuiLayer? = null

    override var layer: GuiLayer
        set(value) {
            _layer = value
        }
        get() {
            return _layer ?: (parent()?.layer ?: GuiLayer.default)
        }

    override var active: Boolean = true

    abstract override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean

    abstract override fun setFocused(focused: Boolean)

    abstract override fun isFocused(): Boolean

    override var mouseEnter: (event: MouseEnterEvent) -> Unit = ::onMouseEnter

    abstract override fun onMouseEnter(event: MouseEnterEvent)

    override var mouseLeave: (event: MouseLeaveEvent) -> Unit = ::onMouseLeave

    abstract override fun onMouseLeave(event: MouseLeaveEvent)

    override var mouseMove: (event: MouseMoveEvent) -> Unit = ::onMouseMove

    abstract override fun onMouseMove(event: MouseMoveEvent)

    override var mousePress: (event: MousePressEvent) -> Unit = ::onMousePress

    abstract override fun onMousePress(event: MousePressEvent)

    override var focused: (event: FocusedEvent) -> Unit = ::onFocused

    abstract override fun onFocused(event: FocusedEvent)

    override var mouseRelease: (event: MouseReleaseEvent) -> Unit = ::onMouseRelease

    abstract override fun onMouseRelease(event: MouseReleaseEvent)

    override var mouseDragging: (event: MouseDragEvent) -> Unit = ::onMouseDragging

    abstract override fun onMouseDragging(event: MouseDragEvent)

    override var mouseScrolling: (event: MouseScrollEvent) -> Unit = ::onMouseScrolling

    abstract override fun onMouseScrolling(event: MouseScrollEvent)

    override var keyPress: (event: KeyPressEvent) -> Unit = ::onKeyPress

    abstract override fun onKeyPress(event: KeyPressEvent)

    override var keyRelease: (event: KeyReleaseEvent) -> Unit = ::onKeyRelease

    abstract override fun onKeyRelease(event: KeyReleaseEvent)

    override var charTyped: (event: CharTypedEvent) -> Unit = ::onCharTyped

    abstract override fun onCharTyped(event: CharTypedEvent)

}