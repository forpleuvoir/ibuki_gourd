package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.GuiElementUserData.name
import moe.forpleuvoir.ibukigourd.gui.base.element.GuiRenderableElementImpl
import moe.forpleuvoir.ibukigourd.gui.base.element.findLastInParentChain
import moe.forpleuvoir.ibukigourd.gui.base.element.isInParentChain
import moe.forpleuvoir.ibukigourd.gui.base.element.parentChain
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.input.mousePosition
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.util.primitive.onTrue

/**
 * 所有组件的基类,实现任何组件都应该继承此类
 */
abstract class GuiWidgetImpl : GuiRenderableElementImpl(), GuiWidget, Measurable {

    //------------ IbukiGourd Widget ------------\\

    override val userData: MutableMap<String, Any> = mutableMapOf()

    final override val transform: Transform = Transform()

    override var padding: Padding = Padding(0)

    override var margin: Margin = Margin(0)

    override var placeCompletion: () -> Unit = ::onPlaceCompletion

    override val interactableBox: Box
        get() {
            val parent = parent()
            return if (parent is GuiWidget) {
                parent.interactableBox.intersectWith(transform.asWorldCoordinateBox)
            } else {
                transform.asWorldCoordinateBox
            }
        }

    override val interactableContentBox: Box
        get() {
            val parent = parent()
            return if (parent is GuiWidget) {
                parent.interactableContentBox.intersectWith(contentBox(true))
            } else {
                contentBox(true)
            }
        }

    /**
     * 鼠标是否在组件中
     */
    override val wasMouseOver: Boolean
        get() {
            return mc.mousePosition in interactableBox && mc.screen == screen()
        }

    override val wasMouseOverContent: Boolean
        get() = mc.mousePosition in interactableContentBox && mc.screen == screen()

    /**
     * 组件是否在拖动中
     */
    override var wasDragging: Boolean = false
        protected set


    //------------ Drawable ------------\\

    override var renderPriority: Int = 0

    override fun onRenderBackground(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) = Unit

    override fun onRender(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) = Unit

    override fun onRenderOverlay(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) = Unit


    //------------ Measurable ------------\\

    override var constraints: Constraints = Constraints()

    override var parentData: Any? = null

    override var measureCompletion: () -> Unit = ::onMeasureCompletion

    override fun onMeasureCompletion() = Unit

    override fun remeasure() {
        this.findLastInParentChain(false) { it is Measurable }?.let {
            it as Measurable
            it.remeasure()
            return
        }
        measure(Constraints.of(0f, mc.window.guiScaledWidth.toFloat(), 0f, mc.window.guiScaledHeight.toFloat()))
        measureCompletion()
    }

    //------------ DrawableElement ------------\\


    override fun onMouseEnter(event: MouseEnterEvent) = Unit

    override fun onMouseLeave(event: MouseLeaveEvent) = Unit

    private var _wasMouseOver: Boolean = false

    @Suppress("DuplicatedCode")
    override fun onMouseMove(event: MouseMoveEvent) {
        //判断鼠标是否在组件内
        if (event.position in transform.asWorldCoordinateBox) {
            //如果之前的[wasMouseOver]状态为False,则更新状态并且触发[MouseEnterEvent]
            if (!_wasMouseOver) {
                mouseEnter(MouseEnterEvent(event.x, event.y))
            }
        } else {
            //如果之前的[wasMouseOver]状态为True,则更新状态并触发[MouseLeaveEvent]
            if (_wasMouseOver) {
                mouseLeave(MouseLeaveEvent(event.x, event.y))
            }
        }
        _wasMouseOver = wasMouseOver
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

    override fun isFocused(): Boolean = screen()?.focusedWidget?.getValue() == this

    override fun setFocused(focused: Boolean) {
        if (focused) {
            screen()?.focusedWidget?.setValue(this)
        } else {
            if (screen()?.focusedWidget == this) screen()?.focusedWidget?.setValue(null)
        }
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean = wasMouseOver

    override fun toString(): String {
        return this.name + "@${hashCode()}"
    }
}
