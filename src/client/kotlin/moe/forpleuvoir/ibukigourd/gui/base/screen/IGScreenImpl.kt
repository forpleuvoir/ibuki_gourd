package moe.forpleuvoir.ibukigourd.gui.base.screen

import kotlinx.coroutines.cancel
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementCustomData.name
import moe.forpleuvoir.ibukigourd.gui.base.element.IGDrawable
import moe.forpleuvoir.ibukigourd.gui.base.element.IGElement
import moe.forpleuvoir.ibukigourd.gui.base.element.findFirsInParentChain
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderGradientBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layoutable
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext.Companion.toIGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen.Companion.applyZOffset
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenUserData.bgBlurRadius
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenUserData.parentCount
import moe.forpleuvoir.ibukigourd.gui.base.screen.ScreenUserData.renderParentScreen
import moe.forpleuvoir.ibukigourd.gui.base.tip.TipHandler
import moe.forpleuvoir.ibukigourd.gui.base.tip.TipHandler.SCREEN_HOVER_TIP
import moe.forpleuvoir.ibukigourd.gui.base.toast.Toast
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetCustomData.hoverTip
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetCustomData.mouseOverCursor
import moe.forpleuvoir.ibukigourd.input.*
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig.Screen.WIDGET_TEST_OUTLINE_COLOR
import moe.forpleuvoir.ibukigourd.render.renderBlur
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.openScreen
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.*
import net.minecraft.client.gui.navigation.GuiNavigation
import net.minecraft.client.gui.navigation.GuiNavigationPath
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.TooltipPositioner
import net.minecraft.text.OrderedText
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.measureTime

abstract class IGScreenImpl : Screen(Literal("ibuki gourd screen")), IGScreen, Layout {

    //------------ IGWidget ------------\\

    override val userData: MutableMap<String, Any> = mutableMapOf()

    override val transform: Transform = Transform(Vector2f(0f, 0f), this.width.toFloat(), this.height.toFloat(), true).apply {
        subscribeSizeChange { _, (width, height) ->
            this@IGScreenImpl.width = width.toInt()
            this@IGScreenImpl.height = height.toInt()
        }
    }

    override var padding: Padding = Padding(0)

    override var margin: Margin = Margin(0)

    override val screen: () -> IGScreen? = { this }

    override var parent: () -> IGElement? = { null }

    override var parentScreen: Screen? = null

    private var _active: Boolean? = null

    override var active: Boolean
        set(value) {
            _active = value
        }
        get() {
            return _active != false
        }

    override fun clearActive() {
        _active = null
    }

    override var placeCompletion: () -> Unit = ::onPlaceCompletion

    /**
     * 鼠标是否在组件中
     */
    override val wasMouseOver: Boolean
        get() = transform.isMouseOvered(mc.mousePosition)

    override val wasMouseOverContent: Boolean
        get() = (mc.mousePosition in contentBox(true)) && mc.currentScreen == screen()

    /**
     * 组件是否在拖动中
     */
    override var wasDragging: Boolean = false
        protected set


    //------------ IGScreen ------------\\

    override var focusedWidget: MutableState<IGWidget?> = mutableStateOf(null)

    private val tasks: MutableList<() -> Unit> = mutableListOf()

    private var eventProcessing = false

    override fun execute(task: () -> Unit) {
        tasks.add(task)
    }

    private fun executeTasks() {
        if (eventProcessing) return
        tasks.forEach {
            runCatching {
                it.invoke()
            }.onFailure {
                Toast.showToast(text = Literal(it.message.toString()).withColor(Colors.RED))
                _log.warn(it)
            }
        }
        tasks.clear()
    }

    override val coroutineScope: ScreenCoroutineScope = ScreenCoroutineScope(this)

    //------------ Tickable ------------\\

    override fun tick() = eventProcessing {
        tick.invoke()
    }

    override var tick: () -> Unit = ::onTick

    override fun onTick() {
        super.onTick()
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

    override var measureCompletion: () -> Unit = ::onMeasureCompletion

    override var layoutCompletion: () -> Unit = ::onLayoutCompletion

    override fun measure(constraints: Constraints): Placeable {
        return super.measure(constraints = constraints).also {
            measureCompletion()
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

    override fun flat(): List<IGWidget> {
        return (widgetChildren().flatMap { if (it is WidgetContainer) it.flat() else listOf(it) } + this)
    }

    override fun clearWidgetChildren() {
        elementChildren.removeAll { it is IGWidget }
        drawableChildren.removeAll { it is IGWidget }
        widgetChildren.clear()
    }

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

    override fun swapWidgetChildren(index1: Int, index2: Int) {
        val temp = widgetChildren[index2]
        widgetChildren[index2] = widgetChildren[index1]
        widgetChildren[index1] = temp
    }

    //------------ Vanilla Screen Override ------------\\

    override var pauseGame: Boolean = false

    override fun shouldPause(): Boolean = pauseGame

    override fun shouldCloseOnEsc(): Boolean = closeOnEsc

    override var closeOnEsc: Boolean = true

    override var onClose: (() -> Unit)? = null

    override fun close() {
        if (client?.currentScreen != this) return
        InputHandler.releaseAll()
        onClose?.invoke()
        TipHandler.popTip(SCREEN_HOVER_TIP)
        MouseCursor.clear()
        coroutineScope.cancel()
        client?.setScreen(parentScreen)
    }

    override var onDisplayed: (() -> Unit)? = null

    override fun onDisplayed() = onDisplayed?.invoke() ?: Unit

    override var onResize: ((MinecraftClient, Int, Int) -> Unit)? = null

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        onResize?.invoke(client, width, height)
        transform.set(width.toFloat(), height.toFloat())
        remeasure()
    }

    override var onFirstInit: ((MinecraftClient, Int, Int) -> Unit)? = null

    override fun init(client: MinecraftClient, width: Int, height: Int) {
        onFirstInit?.invoke(client, width, height)
        transform.set(width.toFloat(), height.toFloat())
        super.init(client, width, height)
    }

    override var onInit: (() -> Unit)? = null

    override fun init() {
        onInit?.invoke()
        TipHandler.popTip(SCREEN_HOVER_TIP)
        recompose()
    }

    override fun clearAndInit() {
        if (screenInitialized) return
        super.clearAndInit()
    }

    override fun recompose() {
        clearWidgetChildren()
        compose()
        remeasure()
    }

    //------------ Drawable ------------\\

    private var _visible: Boolean? = null

    override var visible: Boolean
        set(value) {
            _visible = value
        }
        get() {
            return _visible != false
        }

    override fun clearVisible() {
        _visible = null
    }

    override var renderPriority: Int = 0

    var latestRenderTime: Duration = Duration.ZERO
        protected set

    private var cursorSupplier: () -> MouseCursor = { MouseCursor.default }

    override var hoveredWidget: MutableState<IGWidget?> = mutableStateOf<IGWidget?>(null).apply {
        subscribe {
            var currentNode: IGElement? = it
            while (currentNode != null) {
                if (currentNode is IGWidget) {
                    if (currentNode.mouseOverCursor != null) break
                    if (currentNode is IGScreen) break
                }
                currentNode = currentNode.parent()
            }
            cursorSupplier = { (currentNode as? IGWidget)?.mouseOverCursor ?: MouseCursor.default }

            it?.let hoverTip@{ widget ->
                widget.findFirsInParentChain { it is IGWidget && it.hoverTip != null }
                    ?.let { hoveredWidget ->
                        hoveredWidget as IGWidget
                        TipHandler.pushTip(SCREEN_HOVER_TIP, { hoveredWidget.transform }, hoveredWidget.hoverTip!!)
                        return@hoverTip
                    }
                TipHandler.popTip(SCREEN_HOVER_TIP)
            }
        }
    }

    private fun updateHoveredWidget() {
        val widget = hoveredWidget()
        if (widget != null) {
            if (hoveredWidget.getValue() != widget) {
                hoveredWidget.setValue(widget)
            }
            return
        }
        hoveredWidget.setValue(null)
    }

    @Suppress("LocalVariableName", "DuplicatedCode")
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (!visible) return

        latestRenderTime = measureTime {

            if (mc.currentScreen == this) {
                updateHoveredWidget()
                MouseCursor.current = cursorSupplier()
            } else {
                hoveredWidget.setValue(null)
                focusedWidget.setValue(null)
            }

            val ctx = context.toIGDrawContext()

            val (_mouseX, _mouseY) = context.client.mousePosition
            parentCount
            applyZOffset {

                renderBackground(ctx, _mouseX, _mouseY, delta)

                render.invoke(ctx, _mouseX, _mouseY, delta)

                drawableChildren().sortedBy { it.renderPriority }.foreachWithIterator { drawableChild ->
                    if (drawableChild.visible) drawableChild.vanillaRender(ctx, _mouseX, _mouseY, delta)
                }

                renderOverlay(ctx, _mouseX, _mouseY, delta)

                ctx.renderAfterRendering()
            }
        }
    }

    override var renderBackground: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRenderBackground

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        if (client!!.world == null) {
            this.renderPanoramaBackground(context, delta)
        }
        if (renderParentScreen) {
            parentScreen?.render(context, mouseX.toInt(), mouseY.toInt(), delta)
        }
        renderBlur(bgBlurRadius, delta)
    }

    protected fun renderBlur(radius: Float, delta: Float) {
        client!!.gameRenderer.renderBlur(radius, delta)
        client!!.framebuffer.beginWrite(false)
    }

    override var render: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRender

    override fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) = Unit

    override var renderOverlay: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRenderOverlay

    override fun onRenderOverlay(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        hoveredWidget.getValue()?.let { widget ->
            WIDGET_TEST_OUTLINE_COLOR
                .takeIf { it.alpha > 0 }
                ?.let { context.batchRenderBox { pushBoxOutline(widget.transform, it) } }
        }

    }

    //------------ Vanilla Drawable Override ------------\\

    override fun renderDarkening(context: DrawContext) {
        renderDarkening(context, transform.worldX.toInt(), transform.worldY.toInt(), width, height)
    }

    @Suppress("MemberVisibilityCanBePrivate", "NOTHING_TO_INLINE", "unused")
    protected inline fun renderVanillaBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) =
        renderBackground(context, mouseX.toInt(), mouseY.toInt(), delta)

    override fun renderInGameBackground(context: DrawContext) {
        context.renderGradientBox(transform.asWorldCoordinateBox, Color(0xC0101010), Color(0xD0101010), Orientation.Vertical)
    }


    //------------ Vanilla Element Override & IGElement------------\\

    @OptIn(ExperimentalContracts::class)
    private inline fun <T> eventProcessing(action: () -> T): T {
        contract {
            callsInPlace(action, InvocationKind.EXACTLY_ONCE)
        }
        eventProcessing = true
        val result = action()
        eventProcessing = false
        executeTasks()
        return result
    }

    override var mouseEnter: (event: MouseEnterEvent) -> Unit = ::onMouseEnter

    override fun onMouseEnter(event: MouseEnterEvent) = Unit

    override var mouseLeave: (event: MouseLeaveEvent) -> Unit = ::onMouseLeave

    override fun onMouseLeave(event: MouseLeaveEvent) = Unit

    override fun mouseMoved(mouseX: Double, mouseY: Double) = eventProcessing {
        if (active) mouseMove(MouseMoveEvent(mouseX.toFloat(), mouseY.toFloat()))
    }

    override var mouseMove: (event: MouseMoveEvent) -> Unit = ::onMouseMove

    @Suppress("DuplicatedCode")
    override fun onMouseMove(event: MouseMoveEvent) {
        //判断鼠标是否在组件内
        if (event.position in transform.asWorldCoordinateBox) {
            //如果之前的[wasMouseOver]状态为False,则更新状态并且触发[MouseEnterEvent]
            if (!wasMouseOver) {
                mouseEnter(MouseEnterEvent(event.x, event.y))
            }
        } else {
            //如果之前的[wasMouseOver]状态为True,则更新状态并触发[MouseLeaveEvent]
            if (wasMouseOver) {
                mouseLeave(MouseLeaveEvent(event.x, event.y))
            }
        }

        elementChildren().foreachWithIterator {
            if (it.active) it.mouseMove.invoke(event)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean = eventProcessing {
        if (active) mousePress(MousePressEvent(mouseX.toFloat(), mouseY.toFloat(), Mouse.fromCode(button)))
        false
    }

    override var mousePress: (event: MousePressEvent) -> Unit = ::onMousePress

    override fun onMousePress(event: MousePressEvent) {
        wasDragging = wasMouseOver

        if (wasMouseOver) {
            focused(FocusedEvent())
        }

        elementChildren().foreachWithIterator {
            if (it.active) it.mousePress.invoke(event)
        }
    }

    override var focused: (event: FocusedEvent) -> Unit = ::onFocused

    override fun onFocused(event: FocusedEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.focused.invoke(event)
        }

        event.tryUse().onSuccess {
            isFocused = true
        }
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean = eventProcessing {
        if (active) {
            parentScreen?.mouseReleased(mouseX, mouseY, button)
            mouseRelease(MouseReleaseEvent(mouseX.toFloat(), mouseY.toFloat(), Mouse.fromCode(button)))
        }
        return false
    }

    override var mouseRelease: (event: MouseReleaseEvent) -> Unit = ::onMouseRelease

    override fun onMouseRelease(event: MouseReleaseEvent) {
        wasDragging = false

        elementChildren().foreachWithIterator {
            if (it.active) it.mouseRelease.invoke(event)
        }
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean = eventProcessing {
        if (active && wasDragging) mouseDragging(
            MouseDragEvent(
                mouseX.toFloat(),
                mouseY.toFloat(),
                Mouse.fromCode(button),
                deltaX.toFloat(),
                deltaY.toFloat()
            )
        )
        return false
    }

    override var mouseDragging: (event: MouseDragEvent) -> Unit = ::onMouseDragging

    override fun onMouseDragging(event: MouseDragEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.mouseDragging.invoke(event)
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean = eventProcessing {
        if (active) mouseScrolling(MouseScrollEvent(mouseX.toFloat(), mouseY.toFloat(), verticalAmount.toFloat(), horizontalAmount.toFloat()))
        return false
    }

    override var mouseScrolling: (event: MouseScrollEvent) -> Unit = ::onMouseScrolling

    override fun onMouseScrolling(event: MouseScrollEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.mouseScrolling.invoke(event)
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean = eventProcessing {
        if (active) keyPress(KeyPressEvent(Keyboard.fromCode(keyCode), scanCode, modifiers))
        return true
    }

    override var keyPress: (event: KeyPressEvent) -> Unit = ::onKeyPress

    override fun onKeyPress(event: KeyPressEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.keyPress.invoke(event)
        }
        event.tryUse(event.keyCode == Keyboard.ESCAPE && shouldCloseOnEsc())
            .onSuccess { close() }
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean = eventProcessing {
        if (active) {
            parentScreen?.keyReleased(keyCode, scanCode, modifiers)
            keyRelease(KeyReleaseEvent(Keyboard.fromCode(keyCode), scanCode, modifiers))
        }
        return false
    }

    override var keyRelease: (event: KeyReleaseEvent) -> Unit = ::onKeyRelease

    override fun onKeyRelease(event: KeyReleaseEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.keyRelease.invoke(event)
        }
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean = eventProcessing {
        if (active) charTyped.invoke(CharTypedEvent(chr, modifiers))
        return false
    }

    override var charTyped: (event: CharTypedEvent) -> Unit = ::onCharTyped

    override fun onCharTyped(event: CharTypedEvent) {
        elementChildren().foreachWithIterator {
            if (it.active) it.charTyped.invoke(event)
        }
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean =
        transform.isMouseOvered(mouseX, mouseY)

    override fun isFocused(): Boolean = focusedWidget.getValue() == this

    override fun setFocused(focused: Boolean) {
        if (focused) {
            focusedWidget.setValue(this)
        } else {
            if (focusedWidget.getValue() == this) focusedWidget.setValue(null)
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
        return this.name + "@${hashCode()}"
    }

    companion object {
        val _log = logger("Screen")

        inline fun <T> Iterable<T>.foreachWithIterator(action: (T) -> Unit) {
            val iterator = this.iterator()
            runCatching {
                while (iterator.hasNext()) {
                    action(iterator.next())
                }
            }.onFailure {
                _log.error(it)
            }
        }

        @Suppress("NOTHING_TO_INLINE")
        inline fun <T : Screen> T.open(): T = openScreen(this)

        @Suppress("NOTHING_TO_INLINE")
        inline fun <T : IGScreenImpl> T.open(parentScreen: Screen?): T = openScreen(this).apply { this.parentScreen = parentScreen }

    }
}
