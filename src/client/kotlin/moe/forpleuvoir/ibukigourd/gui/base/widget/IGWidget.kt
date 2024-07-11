package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractDrawableElement
import moe.forpleuvoir.ibukigourd.gui.base.element.IGElement
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen

/**
 * 所有组件的基类,实现任何组件都应该继承此类
 */
abstract class IGWidget : AbstractDrawableElement(), Measurable {

    //------------ IbukiGourd Widget ------------\\

    val transform: Transform = Transform()

    var padding: Padding = Padding(0)

    var margin: Margin = Margin(0)

    /**
     * 鼠标是否在组件中
     */
    var wasMouseOver: Boolean = false
        private set

    /**
     * 组件是否在拖动中
     */
    var wasDragging: Boolean = false
        private set

    var constraints: Constraints = Constraints()

    val contentWidth: Float get() = transform.width - padding.width

    val contentHeight: Float get() = transform.height - padding.height

    fun contentBox(isWorldAxis: Boolean): Box {
        val left = transform.left - padding.left
        val top = transform.top - padding.top
        val right = transform.right - padding.right
        val bottom = transform.bottom - padding.bottom
        return Box(left, top, right, bottom)
    }


    //------------ Drawable ------------\\

    override var visible: Boolean = true

    override var renderPriority: Int = 0

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) = Unit

    override fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) = Unit

    override fun onRenderOverlay(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) = Unit


    //------------ Measurable ------------\\


    override var parentData: Any? = null

    abstract override fun measure(constraints: Constraints): SizeFloat

    abstract override fun minIntrinsicWidth(height: Float): Float

    abstract override fun maxIntrinsicWidth(height: Float): Float

    abstract override fun minIntrinsicHeight(width: Float): Float

    abstract override fun maxIntrinsicHeight(width: Float): Float


    //------------ DrawableElement ------------\\

    override val screen: () -> IGScreen?
        get() {
            return if (parent() is IGScreen) {
                { parent() as IGScreen }
            } else {
                { parent()?.screen?.let { it() } }
            }
        }

    override var parent: () -> IGElement? = { null }

    override fun onMouseEnter(event: MouseEnterEvent) = Unit

    override fun onMouseLeave(event: MouseLeaveEvent) = Unit
    override fun onMouseMove(event: MouseMoveEvent) {
        //判断鼠标是否在组件内
        if (event.position in transform.asWorldBox) {
            //如果之前的[wasMouseOver]状态为False,则更新状态并且触发[MouseEnterEvent]
            if (!wasMouseOver) {
                wasMouseOver = true
                mouseEnter(MouseEnterEvent(event.x, event.y))
            }
        } else {
            //如果之前的[wasMouseOver]状态为True,则更新状态并触发[MouseLeaveEvent]
            if (wasMouseOver) {
                wasMouseOver = false
                mouseLeave(MouseLeaveEvent(event.x, event.y))
            }
        }
    }

    override fun onMouseClick(event: MousePressEvent) {
        wasDragging = wasMouseOver

        if (wasMouseOver) {
            focused(FocusedEvent())
        }
    }

    override fun onFocused(event: FocusedEvent) {
        event.tryUse().onSuccess {
            isFocused = true
        }
    }

    override fun onMouseRelease(event: MouseReleaseEvent) {
        wasDragging = false
    }

    override fun onMouseDragging(event: MouseDragEvent) = Unit

    override fun onMouseScrolling(event: MouseScrollEvent) = Unit

    override fun onKeyPress(event: KeyPressEvent) = Unit
    override fun onKeyRelease(event: KeyReleaseEvent) = Unit

    override fun onCharTyped(event: CharTypedEvent) = Unit


    //------------ Vanilla Element ------------\\

    override fun isFocused(): Boolean = screen()?.focusedWidget == this

    override fun setFocused(focused: Boolean) {
        if (focused) {
            screen()?.focusedWidget = this
        } else {
            if (screen()?.focusedWidget == this) screen()?.focusedWidget = null
        }
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean = wasMouseOver

}
