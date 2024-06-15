package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.LayoutData
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.gui.screen.Screen
import moe.forpleuvoir.ibukigourd.gui.tip.Tip
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractElement(
    override val modifier: Modifier = Modifier
) : Element, AbstractElementContainer() {

    override val screen: () -> Screen
        get() {
            return if (parent() is Screen) {
                { parent() as Screen }
            } else {
                { parent().screen() }
            }
        }

    override var width: ElementDimension = wrap_content
        set(value) {
            if (field != value) {
                field = value
                screen().apply {
                    if (isInitialized) screenLayout()
                }
            }
        }

    override var height: ElementDimension = wrap_content
        set(value) {
            if (field != value) {
                field = value
                screen().apply {
                    if (isInitialized) screenLayout()
                }
            }
        }


    override var layer: Layer = Layer.default

    override val layoutData: Map<KClass<out LayoutData>, LayoutData> = HashMap()

    override var visible: Boolean = true

    override var parent: () -> Element = { this }

    override var active = true

    override var fixed: Boolean = false

    override var wasFocused: Boolean
        set(value) {
            if (value) {
                screen().focusedElement = this
            } else {
                if (screen().focusedElement == this) screen().focusedElement = screen()
            }
        }
        get() {
            return screen().focusedElement == this
        }

    override var onFocusedChanged: ((Boolean) -> Unit)? = null

    override val focusable: Boolean = false

    override var wasMouseOver: Boolean = false

    override val layout: Layout = LinearLayout(Orientation.Horizontal)

    override fun setMeasureWidth(width: Float) {
        transform.width = width
    }

    override fun setMeasureHeight(height: Float) {
        transform.height = height
    }

    final override var tip: Tip? = null
        set(value) {
            if (value != null)
                this.addElement(value)
            else if (field != null)
                this.removeElement(field!!)
            field = value
        }

    override fun init() {
        modifier.apply(this)
        super.init()
        tip?.init?.invoke()
    }

    override fun tick() {
        if (!active) return
        for (element in handleElements) element.tick.invoke()
        tip?.tick?.invoke()
    }

    override var tick: () -> Unit = ::tick

    override var render: (renderContext: RenderContext) -> Unit = ::onRender

    override fun onRender(renderContext: RenderContext) {
        renderContext.tryRender {
            renderBackground.invoke(this)
        }

        for (renderElement in renderElements) {
            renderElement.render(renderContext)
        }

        renderContext.tryRender {
            renderOverlay.invoke(this)
        }
    }

    override var renderBackground: (renderContext: RenderContext) -> Unit = ::onRenderBackground

    override fun onRenderBackground(renderContext: RenderContext) = Unit

    override var renderOverlay: (renderContext: RenderContext) -> Unit = ::onRenderOverlay

    override fun onRenderOverlay(renderContext: RenderContext) = Unit

    override var mouseEnter: (event: MouseEnterEvent) -> Unit = ::onMouseEnter

    override var mouseLeave: (event: MouseLeaveEvent) -> Unit = ::onMouseLeave

    override var mouseMove: (event: MouseMoveEvent) -> Unit = ::onMouseMove

    override fun onMouseMove(event: MouseMoveEvent) {

        wasMouseOver = event.position in transform.asWorldBox

        for (element in handleElements) {
            val mouseOver = element.wasMouseOver
            element.mouseMove(event)
            if (!mouseOver && element.wasMouseOver) {
                element.mouseEnter(MouseEnterEvent(event.x, event.y).apply { this.layer = event.layer })
            } else if (mouseOver && !element.wasMouseOver) {
                element.mouseLeave(MouseLeaveEvent(event.x, event.y).apply { this.layer = event.layer })
            }
        }
    }

    override var mouseClick: (event: MousePressEvent) -> Unit = ::onMouseClick

    override fun onMouseClick(event: MousePressEvent) {
        dragging = wasMouseOver

        if (wasMouseOver) {
            focused(FocusedEvent())
        }

        for (element in handleElements) {
            element.mouseClick(event)
        }
    }

    override var focused: (event: FocusedEvent) -> Unit = ::onFocused

    override fun onFocused(event: FocusedEvent) {

        for (element in handleElements) {
            element.focused(event)
        }

        event.tryUse().onSuccess {
            wasFocused = true
        }
    }

    override var mouseRelease: (event: MouseReleaseEvent) -> Unit = ::onMouseRelease

    override fun onMouseRelease(event: MouseReleaseEvent) {

        dragging = false

        for (element in handleElements) {
            element.mouseRelease(event)
        }
    }

    override var dragging: Boolean = false

    override var mouseDragging: (event: MouseDragEvent) -> Unit = ::onMouseDragging

    override fun onMouseDragging(event: MouseDragEvent) {
        for (element in handleElements) {
            if (element.dragging) element.mouseDragging(event)
        }
    }

    override var mouseScrolling: (event: MouseScrollEvent) -> Unit = ::onMouseScrolling

    override fun onMouseScrolling(event: MouseScrollEvent) {
        for (element in handleElements) {
            element.mouseScrolling(event)
        }
    }

    override var keyPress: (event: KeyPressEvent) -> Unit = ::onKeyPress

    override fun onKeyPress(event: KeyPressEvent) {
        for (element in handleElements) {
            element.keyPress(event)
        }
    }

    override var keyRelease: (event: KeyReleaseEvent) -> Unit = ::onKeyRelease

    override fun onKeyRelease(event: KeyReleaseEvent) {
        for (element in handleElements) {
            element.keyRelease(event)
        }
    }

    override var charTyped: (event: CharTypedEvent) -> Unit = ::onCharTyped

    override fun onCharTyped(event: CharTypedEvent) {
        for (element in handleElements) {
            element.charTyped(event)
        }
    }
}