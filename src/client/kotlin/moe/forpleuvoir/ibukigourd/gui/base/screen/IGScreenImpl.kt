package moe.forpleuvoir.ibukigourd.gui.base.screen

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.IGDrawable
import moe.forpleuvoir.ibukigourd.gui.base.element.IGElement
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.event.GUIEvent.Companion.layer
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.renderGradientBox
import moe.forpleuvoir.ibukigourd.gui.base.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext.Companion.toIGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.input.mousePosition
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.*
import net.minecraft.client.gui.navigation.GuiNavigation
import net.minecraft.client.gui.navigation.GuiNavigationPath
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.TooltipPositioner
import net.minecraft.text.OrderedText

open class IGScreenImpl : Screen(Literal("ibuki gourd screen")), IGScreen {

    //------------ IGWidget ------------\\

    override val transform: Transform = Transform(Vector2f(0f, 0f), this.width.toFloat(), this.height.toFloat(), true).apply {
        subscribeSizeChange { _, (width, height) ->
            this@IGScreenImpl.width = width.toInt()
            this@IGScreenImpl.height = height.toInt()
        }
    }

    override var padding: Padding = Padding(0)

    override var margin: Margin = Margin(0)

    override val screen: () -> IGScreen? = { this }

    override var parent: () -> IGElement? = { this }

    override var active: Boolean = true

    /**
     * 鼠标是否在组件中
     */
    override var wasMouseOver: Boolean = false
        protected set

    /**
     * 组件是否在拖动中
     */
    override var wasDragging: Boolean = false
        protected set


    //------------ IGScreen ------------\\

    override var layer: GuiLayer = GuiLayer.default

    override var layers: List<GuiLayer> = GuiLayer.defaultLayers
        internal set

    override var focusedWidget: IGWidget? = null


    //------------ Tickable ------------\\

    override var tick: () -> Unit = ::tick

    override fun tick() {
        super<IGScreen>.tick()
    }

    //------------ Measurable ------------\\

    override var parentData: Any? = null

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

    override var constraints: Constraints
        get() = Constraints(
            transform.width, transform.width,
            transform.height, transform.height
        )
        set(_) =
            throw UnsupportedOperationException("Default IGScreen implementation cannot set constraints")


    override fun measure(constraints: Constraints): SizeFloat {
        TODO("Not yet implemented")
    }

    //------------ Container ------------\\

    override fun clearChildren() {
        elementChildren.clear()
        drawableChildren.clear()
        widgetChildren.clear()
    }

    override fun remove(child: Element) {
        if (child is IGDrawable) {
            drawableChildren.remove(child)
        }
        if (child is IGDrawable) {
            drawableChildren.remove(child)
        }
        if (child is IGWidget) {
            widgetChildren.remove(child)
        }
    }

    //------------ Container.Element ------------\\

    private val elementChildren = mutableListOf<IGElement>()

    @Deprecated("should use elementChildren() instead", ReplaceWith("elementChildren"))
    override fun children(): MutableList<out Element> = elementChildren

    override fun elementChildren(): List<IGElement> = elementChildren

    override fun <T : IGElement> addElementChild(child: T): T = child.also {
        it.parent = { this }
        elementChildren.add(it)
    }

    //------------ Container.Drawable ------------\\

    private val drawableChildren = mutableListOf<IGDrawable>()

    override fun drawableChildren(): List<IGDrawable> = drawableChildren

    @Deprecated("should use addDrawableChild(child) instead", ReplaceWith("addDrawableChild(drawable)"))
    override fun <T : Drawable?> addDrawable(drawable: T): T = drawable

    override fun <T : IGDrawable> addDrawableChild(child: T): T = child.also {
        drawableChildren.add(it)
    }

    //------------ Container.Widget ------------\\

    private val widgetChildren = mutableListOf<IGWidget>()

    override fun widgetChildren(): List<IGWidget> = widgetChildren

    @Deprecated("should use addWidgetChild(child) instead", ReplaceWith("addWidgetChild"))
    override fun <T> addSelectableChild(child: T): T where T : Element, T : Selectable = child

    override fun <W : IGWidget> addWidgetChild(child: W): W = child.also {
        it.transform.parent = { this.transform }
        addDrawableChild(child)
        addElementChild(child)
        widgetChildren.add(it)
    }

    //------------ Vanilla Screen Override ------------\\

    override var pauseGame: Boolean = false

    override fun shouldPause(): Boolean = pauseGame

    override fun shouldCloseOnEsc(): Boolean = closeOnEsc

    override var closeOnEsc: Boolean = false

    override var onClose: (() -> Unit)? = null

    override fun close() {
        onClose?.invoke()
        super.close()
    }

    override var onDisplayed: (() -> Unit)? = null

    override fun onDisplayed() = onDisplayed?.invoke() ?: Unit

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        super.resize(client, width, height)
        transform.width = width.toFloat()
        transform.height = height.toFloat()
    }


    //------------ Drawable ------------\\

    override var visible: Boolean = true

    override var renderPriority: Int = 0

    @Suppress("LocalVariableName", "DuplicatedCode")
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (!visible) return
        val ctx = context.toIGDrawContext()
        val (_mouseX, _mouseY) = context.client.mousePosition
        ctx.tryRender {
            renderBackground(this, _mouseX, _mouseY, delta)
            vanillaRender(this, _mouseX, _mouseY, delta)
        }

        for (drawableChild in drawableChildren().sortedBy { it.renderPriority }) {
            ctx.tryRender(drawableChild) {
                if (drawableChild.visible) drawableChild.vanillaRender(this, _mouseX, _mouseY, delta)
            }
        }

        ctx.tryRender { renderOverlay(this, _mouseX, _mouseY, delta) }
    }

    override var renderBackground: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRenderBackground

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        renderVanillaBackground(context, mouseX, mouseY, delta)
    }

    override var render: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRender

    override fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) = Unit

    override var renderOverlay: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRenderOverlay

    override fun onRenderOverlay(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) = Unit

    //------------ Vanilla Drawable Override ------------\\

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun renderVanillaBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) =
        renderBackground(context, mouseX.toInt(), mouseY.toInt(), delta)

    override fun renderDarkening(context: DrawContext) {
        renderDarkening(context, transform.worldX.toInt(), transform.worldY.toInt(), width, height)
    }

    override fun renderInGameBackground(context: DrawContext) {
        context.renderGradientBox(transform.asWorldBox, Color(0xC0101010), Color(0xD0101010), Orientation.Vertical)
    }


    //------------ Vanilla Element Override & IGElement------------\\

    override var mouseEnter: (event: MouseEnterEvent) -> Unit = ::onMouseEnter

    override fun onMouseEnter(event: MouseEnterEvent) = Unit

    override var mouseLeave: (event: MouseLeaveEvent) -> Unit = ::onMouseLeave

    override fun onMouseLeave(event: MouseLeaveEvent) = Unit

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (active) mouseMove(MouseMoveEvent(mouseX, mouseY).layer(this.layer))
    }

    override var mouseMove: (event: MouseMoveEvent) -> Unit = ::onMouseMove

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

        for (layer in layers) {
            event.layer = layer
            for (child in elementChildren()) {
                if (child is IGWidget) {
                    val mouseOver = child.wasMouseOver
                    child.mouseMove.invoke(event)
                    if (!mouseOver && child.wasMouseOver) {
                        child.mouseEnter(MouseEnterEvent(event.x, event.y).layer(layer))
                    } else if (mouseOver && !child.wasMouseOver) {
                        child.mouseLeave(MouseLeaveEvent(event.x, event.y).layer(layer))
                    }
                } else child.mouseMove.invoke(event)
            }
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (active) mousePress(MousePressEvent(mouseX, mouseY, Mouse.fromCode(button)).layer(this.layer))
        return false
    }

    override var mousePress: (event: MousePressEvent) -> Unit = ::onMousePress

    override fun onMousePress(event: MousePressEvent) {
        wasDragging = wasMouseOver

        for (layer in layers) {
            event.layer = layer

            if (wasMouseOver) {
                focused(FocusedEvent().layer(layer))
            }

            for (child in elementChildren()) {
                child.mousePress.invoke(event)
            }
        }
    }

    override var focused: (event: FocusedEvent) -> Unit = ::onFocused

    override fun onFocused(event: FocusedEvent) {
        for (child in elementChildren()) {
            child.focused.invoke(event)
        }
        event.tryUse().onSuccess {
            isFocused = true
        }
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (active) mouseRelease(MouseReleaseEvent(mouseX, mouseY, Mouse.fromCode(button)).layer(this.layer))
        return false
    }

    override var mouseRelease: (event: MouseReleaseEvent) -> Unit = ::onMouseRelease

    override fun onMouseRelease(event: MouseReleaseEvent) {
        wasDragging = false

        for (layer in layers) {
            event.layer = layer
            for (child in elementChildren()) {
                child.mouseRelease.invoke(event)
            }
        }
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (active && wasDragging) mouseDragging(MouseDragEvent(mouseX, mouseY, Mouse.fromCode(button), deltaX, deltaY).layer(this.layer))
        return false
    }

    override var mouseDragging: (event: MouseDragEvent) -> Unit = ::onMouseDragging

    override fun onMouseDragging(event: MouseDragEvent) {
        for (layer in layers) {
            event.layer = layer
            for (child in elementChildren()) {
                if (child is IGWidget) {
                    if (child.wasDragging) child.mouseDragging.invoke(event)
                } else {
                    child.mouseDragging.invoke(event)
                }
            }
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (active) mouseScrolling(MouseScrollEvent(mouseX, mouseY, horizontalAmount, verticalAmount).layer(this.layer))
        return false
    }

    override var mouseScrolling: (event: MouseScrollEvent) -> Unit = ::onMouseScrolling

    override fun onMouseScrolling(event: MouseScrollEvent) {
        for (layer in layers) {
            event.layer = layer
            for (child in elementChildren()) {
                child.mouseScrolling.invoke(event)
            }
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (active) keyPress(KeyPressEvent(Keyboard.fromCode(keyCode), scanCode, modifiers).layer(this.layer))
        return false
    }

    override var keyPress: (event: KeyPressEvent) -> Unit = ::onKeyPress

    override fun onKeyPress(event: KeyPressEvent) {
        for (layer in layers) {
            event.layer = layer
            for (child in elementChildren()) {
                child.keyPress.invoke(event)
            }
        }
        event.tryUse {
            event.keyCode == Keyboard.ESCAPE && shouldCloseOnEsc()
        }.onSuccess {
            close()
        }
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (active) keyRelease(KeyReleaseEvent(Keyboard.fromCode(keyCode), scanCode, modifiers).layer(this.layer))
        return false
    }

    override var keyRelease: (event: KeyReleaseEvent) -> Unit = ::onKeyRelease

    override fun onKeyRelease(event: KeyReleaseEvent) {
        for (layer in layers) {
            event.layer = layer
            for (child in elementChildren()) {
                child.keyRelease(event)
            }
        }
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        if (active) charTyped.invoke(CharTypedEvent(chr, modifiers).layer(this.layer))
        return false
    }

    override var charTyped: (event: CharTypedEvent) -> Unit = ::onCharTyped

    override fun onCharTyped(event: CharTypedEvent) {
        for (layer in layers) {
            event.layer = layer
            for (child in elementChildren()) {
                child.charTyped(event)
            }
        }
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean =
        transform.isMouseOvered(mouseX, mouseY)

    override fun isFocused(): Boolean = focusedWidget == this

    override fun setFocused(focused: Boolean) {
        if (focused) {
            focusedWidget = this
        } else {
            if (focusedWidget == this) focusedWidget = null
        }
    }


    //------------ Unsupported ------------\\

    override fun setTooltip(tooltip: MutableList<OrderedText>, positioner: TooltipPositioner, focused: Boolean) = Unit


    override fun getNavigationPath(navigation: GuiNavigation?): GuiNavigationPath? =
        super<IGScreen>.getNavigationPath(navigation)

    override fun getFocusedPath(): GuiNavigationPath? =
        super<IGScreen>.getFocusedPath()

    override fun getNavigationFocus(): ScreenRect = transform.asScreenRect

}