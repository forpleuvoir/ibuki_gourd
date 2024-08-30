package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.DrawableElementImpl
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.event.GUIEvent.Companion.layer
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.input.mousePosition
import moe.forpleuvoir.ibukigourd.util.mc

/**
 * 所有组件的基类,实现任何组件都应该继承此类
 */
abstract class IGWidgetImpl : DrawableElementImpl(), IGWidget, Measurable {

    //------------ IbukiGourd Widget ------------\\

    final override val transform: Transform = Transform().apply {
        subscribeChange({ _, _ ->
            wasMouseOver = isMouseOvered(mc.mousePosition)
        }, { _, _ ->
            wasMouseOver = isMouseOvered(mc.mousePosition)
        })
    }

    override var padding: Padding = Padding(0)

    override var margin: Margin = Margin(0)

    override var placeCompleted: () -> Unit = ::onPlaceCompleted

    /**
     * 鼠标是否在组件中
     */
    override var wasMouseOver: Boolean = false
        protected set

    override val mouseOverCursor: MouseCursor.Cursor
        get() {
            val parent = parent()
            return if (parent is IGWidget) {
                parent.mouseOverCursor
            } else screen()?.mouseOverCursor ?: MouseCursor.default
        }

    /**
     * 组件是否在拖动中
     */
    override var wasDragging: Boolean = false
        protected set


    //------------ Drawable ------------\\

    override var visible: Boolean = true

    override var renderPriority: Int = 0

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) = Unit

    override fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) = Unit

    override fun onRenderOverlay(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) = Unit


    //------------ Measurable ------------\\

    override var constraints: Constraints = Constraints()

    override var parentData: Any? = null

    override var measureCompleted: () -> Unit = ::onMeasureCompleted

    override fun onMeasureCompleted() = Unit


    //------------ DrawableElement ------------\\

    override val screen: () -> IGScreen?
        get() {
            return if (parent() is IGScreen) {
                { parent() as IGScreen }
            } else {
                { parent()?.screen?.let { it() } }
            }
        }

    override fun onMouseEnter(event: MouseEnterEvent) = Unit

    override fun onMouseLeave(event: MouseLeaveEvent) = Unit

    @Suppress("DuplicatedCode")
    override fun onMouseMove(event: MouseMoveEvent) {
        //判断鼠标是否在组件内
        if (event.position in transform.asWorldBox) {
            //如果之前的[wasMouseOver]状态为False,则更新状态并且触发[MouseEnterEvent]
            if (!wasMouseOver) {
                wasMouseOver = true
                mouseEnter(MouseEnterEvent(event.x, event.y).layer(this.layer))
            }
        } else {
            //如果之前的[wasMouseOver]状态为True,则更新状态并触发[MouseLeaveEvent]
            if (wasMouseOver) {
                wasMouseOver = false
                mouseLeave(MouseLeaveEvent(event.x, event.y).layer(this.layer))
            }
        }

    }

    override fun onMousePress(event: MousePressEvent) {
        wasDragging = wasMouseOver
    }

    override fun onFocused(event: FocusedEvent) {
        event.tryUse {
            wasMouseOver
        }.onSuccess {
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

    override fun toString(): String {
        return (parent()?.toString() ?: "") + "=>" + this::class.simpleName + "@${hashCode()}"
    }
}
