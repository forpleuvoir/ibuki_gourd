package moe.forpleuvoir.ibukigourd.gui.base.screen

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.IGDrawable
import moe.forpleuvoir.ibukigourd.gui.base.element.IGElement
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.event.GUIEvent.Companion.layer
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderGradientBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layoutable
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext.Companion.toIGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.scope.ScreenScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.input.mousePosition
import moe.forpleuvoir.ibukigourd.mod.gui.GuiConfig.Screen.BG_BLUR_RADIUS
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.render.renderBlur
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.*
import net.minecraft.client.gui.navigation.GuiNavigation
import net.minecraft.client.gui.navigation.GuiNavigationPath
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.TooltipPositioner
import net.minecraft.text.OrderedText
import kotlin.time.Duration
import kotlin.time.measureTime

abstract class IGScreenImpl<S : ScreenScope<*>> : Screen(Literal("ibuki gourd screen")), IGScreen, Layout {

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

    private var _active: Boolean? = null

    override var active: Boolean
        set(value) {
            _active = value
        }
        get() {
            return _active ?: true
        }

    override fun clearActive() {
        _active = null
    }

    override var placeCompleted: () -> Unit = ::onPlaceCompleted

    override val mouseOverCursor: MouseCursor.Cursor
        get() = MouseCursor.default

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

    private var _layer: GuiLayer? = null

    override var layer: GuiLayer
        set(value) {
            _layer = value
        }
        get() {
            return _layer ?: GuiLayer.Default
        }

    override fun clearLayer() {
        _layer = null
    }

    override var layers: List<GuiLayer> = GuiLayer.defaultLayers
        internal set

    override var focusedWidget: IGWidget? = null

    private val datas: MutableMap<String, Any> = mutableMapOf()

    override fun pushData(key: String, data: Any) {
        datas[key] = data
    }

    override fun getData(key: String): Any? = datas[key]

    //------------ Tickable ------------\\

    override fun tick() {
        tick.invoke()
    }

    override var tick: () -> Unit = ::onTick

    override fun onTick() {
        super.onTick()
        updateHoveredWidget()
    }

    //------------ Measurable ------------\\

    override var parentData: Any? = null

    override var constraints: Constraints
        get() = Constraints(
            transform.width, transform.width,
            transform.height, transform.height
        )
        set(_) =
            throw UnsupportedOperationException("Default IGScreen implementation cannot set constraints")

    override val widget: IGWidget
        get() = this

    override fun layoutableChildren(): List<Layoutable> = widgetChildren()

    override var measureCompleted: () -> Unit = ::onMeasureCompleted

    override var layoutCompleted: () -> Unit = ::onLayoutCompleted

    override fun measure(constraints: Constraints): Placeable {
        return super.measure(constraints = constraints).also {
            measureCompleted()
            layout()
        }
    }

    override fun remeasure() {
        measure(constraints)
    }

    //------------ Container ------------\\

    override fun clearChildren() {
        elementChildren.clear()
        drawableChildren.clear()
        widgetChildren.clear()
    }

    override fun remove(child: Element) {
        if (child is IGElement) {
            elementChildren.remove(child)
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

    override fun clearWidgetChildren() = widgetChildren.clear()

    @Deprecated("should use addWidgetChild(child) instead", ReplaceWith("addWidgetChild"))
    override fun <T> addSelectableChild(child: T): T where T : Element, T : Selectable = child

    override fun <W : IGWidget> addWidgetChild(child: W): W = child.also {
        it.transform.parent = { this.transform }
        addDrawableChild(child)
        addElementChild(child)
        widgetChildren.add(it)
    }

    override fun <W : IGWidget> setWidgetChildren(index: Int, child: W): W = child.also {
        it.transform.parent = { this.transform }
        it.parent = { this }
        val old = widgetChildren[index]
        val di = drawableChildren.indexOf(old)
        val ei = elementChildren.indexOf(old)
        widgetChildren[index] = it
        drawableChildren[di] = it
        elementChildren[ei] = it
    }

    override fun removeWidgetChild(child: IGWidget): Boolean {
        return elementChildren.remove(child) ||
                drawableChildren.remove(child) ||
                widgetChildren.remove(child)
    }

    override fun removeWidgetChildAt(index: Int): IGWidget? {
        val old = widgetChildren[index]
        val di = drawableChildren.indexOf(old)
        val ei = elementChildren.indexOf(old)
        elementChildren.removeAt(ei)
        drawableChildren.removeAt(di)
        return widgetChildren.removeAt(index)
    }

    //------------ Vanilla Screen Override ------------\\

    override var pauseGame: Boolean = false

    override fun shouldPause(): Boolean = pauseGame

    override fun shouldCloseOnEsc(): Boolean = closeOnEsc

    override var closeOnEsc: Boolean = true

    override var onClose: (() -> Unit)? = null

    override fun close() {
        onClose?.invoke()
        MouseCursor.clear()
        super.close()
    }

    override var onDisplayed: (() -> Unit)? = null

    override fun onDisplayed() = onDisplayed?.invoke() ?: Unit

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        transform.set(width.toFloat(), height.toFloat())
        remeasure()
    }

    override fun init(client: MinecraftClient, width: Int, height: Int) {
        transform.set(width.toFloat(), height.toFloat())
        super.init(client, width, height)
    }

    override fun init() {
        scope.content()
        remeasure()
    }

    abstract fun S.content()

    abstract val scope: S

    //------------ Drawable ------------\\

    private var _visible: Boolean? = null

    override var visible: Boolean
        set(value) {
            _visible = value
        }
        get() {
            return _visible ?: true
        }

    override fun clearVisible() {
        _visible = null
    }

    override var renderPriority: Int = 0

    var latestRenderTime: Duration = Duration.ZERO
        protected set


    override var hoveredWidget: IGWidget? = null

    private fun updateHoveredWidget() {
        for (layer in layers) {
            val widget = hoveredWidget(layer)
            if (widget != null) {
                hoveredWidget = widget
                return
            }
        }
        hoveredWidget = null
    }

    @Suppress("LocalVariableName", "DuplicatedCode")
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (!visible) return
        //更新鼠标样式
        MouseCursor.current = hoveredWidget?.mouseOverCursor ?: MouseCursor.default
        latestRenderTime = measureTime {
            val ctx = context.toIGDrawContext()

            val (_mouseX, _mouseY) = context.client.mousePosition
            renderBackground(ctx, _mouseX, _mouseY, delta)
            render.invoke(ctx, _mouseX, _mouseY, delta)
            for (index in layers.lastIndex downTo 0) {
                ctx.layer = layers[index]
                for (drawableChild in drawableChildren().sortedBy { it.renderPriority }) {
                    if (drawableChild.visible) drawableChild.vanillaRender(ctx, _mouseX, _mouseY, delta)
                }
            }

            renderOverlay(ctx, _mouseX, _mouseY, delta)

            ctx.render()
        }
    }

    override var renderBackground: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRenderBackground

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        if (client!!.world == null) {
            this.renderPanoramaBackground(context, delta)
        }
        renderBlur(BG_BLUR_RADIUS, delta)
        renderDarkening(context)
    }

    protected fun renderBlur(radius: Float, delta: Float) {
        client!!.gameRenderer.renderBlur(radius, delta)
        client!!.framebuffer.beginWrite(false)
    }

    override var render: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRender

    override fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) = Unit

    override var renderOverlay: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRenderOverlay

    override fun onRenderOverlay(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) = Unit

    //------------ Vanilla Drawable Override ------------\\

    override fun renderDarkening(context: DrawContext) {
        renderDarkening(context, transform.worldX.toInt(), transform.worldY.toInt(), width, height)
    }

    @Suppress("MemberVisibilityCanBePrivate", "NOTHING_TO_INLINE")
    protected inline fun renderVanillaBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) =
        renderBackground(context, mouseX.toInt(), mouseY.toInt(), delta)

    override fun renderInGameBackground(context: DrawContext) {
        context.renderGradientBox(transform.asWorldBox, Color(0xC0101010), Color(0xD0101010), Orientation.Vertical)
    }


    //------------ Vanilla Element Override & IGElement------------\\

    //    override fun hoveredWidget(layer: GuiLayer): IGWidget? {
    //
    //        return super.hoveredWidget(layer)
    //    }


    override var mouseEnter: (event: MouseEnterEvent) -> Unit = ::onMouseEnter

    override fun onMouseEnter(event: MouseEnterEvent) = Unit

    override var mouseLeave: (event: MouseLeaveEvent) -> Unit = ::onMouseLeave

    override fun onMouseLeave(event: MouseLeaveEvent) = Unit

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (active) mouseMove(MouseMoveEvent(mouseX.toFloat(), mouseY.toFloat()).layer(this.layer))
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
                if (!child.active) continue
                else child.mouseMove.invoke(event)

            }
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (active) mousePress(MousePressEvent(mouseX.toFloat(), mouseY.toFloat(), Mouse.fromCode(button)).layer(this.layer))
        return false
    }

    override var mousePress: (event: MousePressEvent) -> Unit = ::onMousePress

    override fun onMousePress(event: MousePressEvent) {
        wasDragging = wasMouseOver

        for (layer in layers) {
            event.layer(layer)

            if (wasMouseOver) {
                focused(FocusedEvent().layer(layer))
            }

            for (child in elementChildren()) {
                if (child.active) child.mousePress.invoke(event)
            }
        }
    }

    override var focused: (event: FocusedEvent) -> Unit = ::onFocused

    override fun onFocused(event: FocusedEvent) {
        for (layer in layers) {
            event.layer(layer)
            for (child in elementChildren()) {
                if (child.active) child.focused.invoke(event)
            }
        }
        event.tryUse().onSuccess {
            isFocused = true
        }

    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (active) mouseRelease(MouseReleaseEvent(mouseX.toFloat(), mouseY.toFloat(), Mouse.fromCode(button)).layer(this.layer))
        return false
    }

    override var mouseRelease: (event: MouseReleaseEvent) -> Unit = ::onMouseRelease

    override fun onMouseRelease(event: MouseReleaseEvent) {
        wasDragging = false

        for (layer in layers) {
            event.layer = layer
            for (child in elementChildren()) {
                if (child.active) child.mouseRelease.invoke(event)
            }
        }
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (active && wasDragging) mouseDragging(
            MouseDragEvent(
                mouseX.toFloat(),
                mouseY.toFloat(),
                Mouse.fromCode(button),
                deltaX.toFloat(),
                deltaY.toFloat()
            ).layer(this.layer)
        )
        return false
    }

    override var mouseDragging: (event: MouseDragEvent) -> Unit = ::onMouseDragging

    override fun onMouseDragging(event: MouseDragEvent) {
        for (layer in layers) {
            event.layer = layer
            for (child in elementChildren()) {
                if (child.active) child.mouseDragging.invoke(event)
            }
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (active) mouseScrolling(MouseScrollEvent(mouseX.toFloat(), mouseY.toFloat(), verticalAmount.toFloat(), horizontalAmount.toFloat()).layer(this.layer))
        return false
    }

    override var mouseScrolling: (event: MouseScrollEvent) -> Unit = ::onMouseScrolling

    override fun onMouseScrolling(event: MouseScrollEvent) {
        for (layer in layers) {
            event.layer = layer
            for (child in elementChildren()) {
                if (child.active) child.mouseScrolling.invoke(event)
            }
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (active) keyPress(KeyPressEvent(Keyboard.fromCode(keyCode), scanCode, modifiers).layer(this.layer))
        return true
    }

    override var keyPress: (event: KeyPressEvent) -> Unit = ::onKeyPress

    override fun onKeyPress(event: KeyPressEvent) {
        for (layer in layers) {
            event.layer = layer
            for (child in elementChildren()) {
                if (child.active) child.keyPress.invoke(event)
            }
        }
        event.tryUse(event.keyCode == Keyboard.ESCAPE && shouldCloseOnEsc())
            .onSuccess { close() }
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
                if (child.active) child.keyRelease(event)
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
                if (child.active) child.charTyped(event)
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

    override fun toString(): String {
        return (this::class.simpleName ?: "Screen") + "@${hashCode()}"
    }

}
