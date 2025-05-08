package moe.forpleuvoir.ibukigourd.gui.base.modifier.impl

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.element.RenderPriority
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetRenderLayer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetUserData.setHoverTip
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetUserData.setMouseOverCursor
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetUserData.setMouseOverCursorMapping
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextWidget
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.input.MouseCursorMapping
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.nebula.common.color.ARGBColor
import kotlin.time.Duration

fun interface WidgetModifier : Modifier.Element {
    fun applyModify(element: IGWidget)

    override fun tryApplyModify(target: Any) {
        if (target is IGWidget) applyModify(target)
    }
}

fun interface WidgetContainerModifier : Modifier.Element {
    fun applyModify(element: WidgetContainerImpl)

    override fun tryApplyModify(target: Any) {
        if (target is WidgetContainerImpl) applyModify(target)
    }
}


//------------ Size ------------\\

/**
 * Sets the size of the widget's constraints.
 *
 * @param width The width of the widget. If null, the width will be unchanged.
 * @param height The height of the widget. If null, the height will be unchanged.
 *
 * @return The modified Modifier.
 */
fun Modifier.size(width: Float?, height: Float?) = this then WidgetModifier { widget ->
    widget.constraints = Constraints(
        width ?: widget.constraints.minWidth,
        width ?: widget.constraints.maxWidth,
        height ?: widget.constraints.minHeight,
        height ?: widget.constraints.maxHeight
    )
}

fun Modifier.size(size: Size<Float>) = this then WidgetModifier { widget ->
    widget.constraints = Constraints(size.width, size.width, size.height, size.height)
}

fun Modifier.minSize(minWidth: Float?, minHeight: Float?) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(
        minWidth = minWidth ?: widget.constraints.minWidth,
        minHeight = minHeight ?: widget.constraints.minHeight
    )
}

fun Modifier.maxSize(maxWidth: Float?, maxHeight: Float?) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(
        maxWidth = maxWidth ?: widget.constraints.maxWidth,
        maxHeight = maxHeight ?: widget.constraints.maxHeight
    )
}

/**
 * sets the width of the widget's constraints.
 *
 * @param width the width value to set
 * @return The modified modifier.
 */
fun Modifier.width(width: Float) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(minWidth = width, maxWidth = width)
}

fun Modifier.minWidth(minWidth: Float) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(minWidth = minWidth)
}

fun Modifier.maxWidth(maxWidth: Float) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(maxWidth = maxWidth)
}


/**
 * sets the height of the widget's constraints.
 *
 * @param height the height value to set
 * @return The modified modifier.
 */
fun Modifier.height(height: Float) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(minHeight = height, maxHeight = height)
}

fun Modifier.minHeight(minHeight: Float) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(minHeight = minHeight)
}

fun Modifier.maxHeight(maxHeight: Float) = this then WidgetModifier { widget ->
    widget.constraints = widget.constraints.copy(maxHeight = maxHeight)
}

//------------ Padding ------------\\

/**
 * Adds padding to the widget using the specified [padding].
 *
 * @param padding The padding to apply to the widget.
 * @return The modified [Modifier].
 */
fun Modifier.padding(padding: Padding) = this then WidgetModifier { widget ->
    widget.padding = padding
}

fun Modifier.padding(left: Number? = null, right: Number? = null, top: Number? = null, bottom: Number? = null) = this then WidgetModifier { widget ->
    widget.padding = Padding(left ?: widget.padding.left, right ?: widget.padding.right, top ?: widget.padding.top, bottom ?: widget.padding.bottom)
}

fun Modifier.padding(horizontal: Number? = null, vertical: Number? = null) = this then WidgetModifier { widget ->
    widget.padding =
        Padding(
            left = horizontal ?: widget.padding.left,
            right = horizontal ?: widget.padding.right,
            top = vertical ?: widget.padding.top,
            bottom = vertical ?: widget.padding.bottom
        )
}

fun Modifier.padding(all: Number) = this then WidgetModifier { widget ->
    widget.padding = Padding(all)
}


/**
 * Adds a margin to the widget.
 *
 * @param margin The margin to apply to the widget.
 * @return The modified Modifier object.
 */
fun Modifier.margin(margin: Margin) = this then WidgetModifier { widget ->
    widget.margin = margin
}

fun Modifier.margin(left: Number? = null, right: Number? = null, top: Number? = null, bottom: Number? = null) = this then WidgetModifier { widget ->
    widget.margin = Margin(left ?: widget.margin.left, right ?: widget.margin.right, top ?: widget.margin.top, bottom ?: widget.margin.bottom)
}

fun Modifier.margin(horizontal: Number? = null, vertical: Number? = null) = this then WidgetModifier { widget ->
    widget.margin =
        Margin(left = horizontal ?: widget.margin.left, right = horizontal ?: widget.margin.right, top = vertical ?: widget.margin.top, bottom = vertical ?: widget.margin.bottom)
}

fun Modifier.margin(all: Number) = this then WidgetModifier { widget ->
    widget.margin = Margin(all)
}

//------------ Render ------------\\

fun Modifier.renderPriority(priority: Int) = this then WidgetModifier { widget ->
    widget.renderPriority = priority
}

fun Modifier.renderPriority(priority: State<Int>) = this then WidgetModifier { widget ->
    widget.renderPriority = priority.getValue()
    priority.subscribe {
        widget.renderPriority = it
    }
}

fun Modifier.visible(state: Boolean) = this then WidgetModifier { widget ->
    widget.visible = state
}

fun Modifier.visible(state: State<Boolean>) = this then WidgetModifier { widget ->
    widget.visible = state.getValue()
    state.subscribe {
        widget.visible = it
    }
}

fun Modifier.addRenderLayer(priority: Int, action: IGWidget.(IGDrawContext, Float, Float, Float) -> Unit) = this then WidgetModifier { widget ->
    widget.addRenderLayer(priority, WidgetRenderLayer(widget, action))
}

fun Modifier.preRenderHandler(action: IGWidget.(IGDrawContext, Float, Float, Float) -> Unit) =
    addRenderLayer(RenderPriority.PRE_HANDLER, action)

fun Modifier.renderBackground(action: IGWidget.(IGDrawContext, Float, Float, Float) -> Unit) =
    addRenderLayer(RenderPriority.BACKGROUND, action)

fun Modifier.render(action: IGWidget.(IGDrawContext, Float, Float, Float) -> Unit) =
    addRenderLayer(RenderPriority.DEFAULT, action)

fun Modifier.renderOverlay(action: IGWidget.(IGDrawContext, Float, Float, Float) -> Unit) =
    addRenderLayer(RenderPriority.OVERLAY, action)

fun Modifier.postRenderHandler(action: IGWidget.(IGDrawContext, Float, Float, Float) -> Unit) =
    addRenderLayer(RenderPriority.POST_HANDLER, action)


fun Modifier.disableRenderLayer(priority: Int) = this then WidgetModifier { widget ->
    widget.removeRenderLayer(priority)
}

fun Modifier.disablePreRenderHandler() = disableRenderLayer(RenderPriority.PRE_HANDLER)

fun Modifier.disableRenderBackground() = disableRenderLayer(RenderPriority.BACKGROUND)

fun Modifier.disableRender() = disableRenderLayer(RenderPriority.DEFAULT)

fun Modifier.disableRenderOverlay() = disableRenderLayer(RenderPriority.OVERLAY)

fun Modifier.disablePostRenderHandler() = disableRenderLayer(RenderPriority.POST_HANDLER)

//------------ GuiContext ------------\\


//------------ Measure ------------\\

fun Modifier.measureCompletion(action: IGWidget.() -> Unit) = this then WidgetModifier { widget ->
    widget.measureCompletion = { widget.action() }
}

//------------ Layout ------------\\

fun Modifier.layoutCompleted(action: WidgetContainerImpl.() -> Unit) = this then WidgetContainerModifier { widget ->
    widget.layoutCompletion = { widget.action() }
}

//------------ Misc ------------\\

fun Modifier.active(state: Boolean) = this then WidgetModifier { widget ->
    widget.active = state
}

fun Modifier.active(state: State<Boolean>) = this then WidgetModifier { widget ->
    widget.active = state.getValue()
    state.subscribe {
        widget.active = it
    }
}

fun Modifier.placeCompletion(action: IGWidget.() -> Unit) = this then WidgetModifier { widget ->
    widget.placeCompletion = { action(widget) }
}

//------------ CustomData ------------\\

fun <W : IGWidget> Modifier.mouseOverCursor(mapping: MouseCursorMapping<W>) = this then WidgetModifier { widget ->
    runCatching {
        @Suppress("UNCHECKED_CAST")
        (widget as? W)?.setMouseOverCursorMapping(mapping)
    }
}

fun Modifier.mouseOverCursor(cursor: MouseCursor) = this then WidgetModifier { widget ->
    widget.setMouseOverCursor(cursor)
}

//------------ HoverText ------------\\

fun Modifier.hoverTip(
    settings: Tip.Setting = Tip.DefaultSetting,
    modifier: Modifier = Modifier,
    content: BoxScope.() -> Unit
) = this then WidgetModifier { widget ->
    widget.setHoverTip(Tip(settings, Tip.DefaultModifier.then(modifier), content))
}

@JvmName("hoverTextState")
fun Modifier.hoverText(
    text: State<Text>,
    settings: Tip.Setting = Tip.DefaultSetting,
    modifier: Modifier = Modifier
) = hoverTip(settings, modifier) {
    TextLabel(text, setting = TextWidget.Setting(autoNewLine = true))
}

fun Modifier.hoverText(
    text: Text,
    settings: Tip.Setting = Tip.DefaultSetting,
    modifier: Modifier = Modifier
) = hoverTip(settings, modifier) {
    TextLabel(text, setting = TextWidget.Setting(autoNewLine = true))
}

fun Modifier.hoverText(
    text: Text,
    showDelay: Duration = Tip.DefaultSetting.showDelay,
    hideDelay: Duration = Tip.DefaultSetting.hideDelay,
    fadeInDuration: Duration = Tip.DefaultSetting.fadeInDuration,
    fadeInOffset: Float = Tip.DefaultSetting.fadeInOffset,
    optionalDirection: List<Direction> = Tip.DefaultSetting.optionalDirection,
    backgroundColor: ARGBColor = Tip.DefaultSetting.backgroundColor,
    modifier: Modifier = Modifier
) = hoverTip(Tip.Setting(showDelay, hideDelay, fadeInDuration, fadeInOffset, optionalDirection, backgroundColor), modifier) {
    TextLabel(text, setting = TextWidget.Setting(autoNewLine = true))
}

@JvmName("hoverTextState")
fun Modifier.hoverText(
    text: State<Text>,
    showDelay: Duration = Tip.DefaultSetting.showDelay,
    hideDelay: Duration = Tip.DefaultSetting.hideDelay,
    fadeInDuration: Duration = Tip.DefaultSetting.fadeInDuration,
    fadeInOffset: Float = Tip.DefaultSetting.fadeInOffset,
    optionalDirection: List<Direction> = Tip.DefaultSetting.optionalDirection,
    backgroundColor: ARGBColor = Tip.DefaultSetting.backgroundColor,
    modifier: Modifier = Modifier
) = hoverTip(Tip.Setting(showDelay, hideDelay, fadeInDuration, fadeInOffset, optionalDirection, backgroundColor), modifier) {
    TextLabel(text, setting = TextWidget.Setting(autoNewLine = true))
}

@JvmName("hoverTextString")
fun Modifier.hoverText(
    text: State<String>,
    settings: Tip.Setting = Tip.DefaultSetting,
    modifier: Modifier = Modifier
) = hoverTip(settings, modifier) {
    TextLabel(text, setting = TextWidget.Setting(autoNewLine = true))
}

fun Modifier.hoverText(
    text: String,
    settings: Tip.Setting = Tip.DefaultSetting,
    modifier: Modifier = Modifier
) = hoverTip(settings, modifier) {
    TextLabel(text, setting = TextWidget.Setting(autoNewLine = true))
}

fun Modifier.hoverText(
    text: String,
    showDelay: Duration = Tip.DefaultSetting.showDelay,
    hideDelay: Duration = Tip.DefaultSetting.hideDelay,
    fadeInDuration: Duration = Tip.DefaultSetting.fadeInDuration,
    fadeInOffset: Float = Tip.DefaultSetting.fadeInOffset,
    optionalDirection: List<Direction> = Tip.DefaultSetting.optionalDirection,
    backgroundColor: ARGBColor = Tip.DefaultSetting.backgroundColor,
    modifier: Modifier = Modifier
) = hoverTip(Tip.Setting(showDelay, hideDelay, fadeInDuration, fadeInOffset, optionalDirection, backgroundColor), modifier) {
    TextLabel(text, setting = TextWidget.Setting(autoNewLine = true))
}

fun Modifier.hoverText(
    text: State<String>,
    showDelay: Duration = Tip.DefaultSetting.showDelay,
    hideDelay: Duration = Tip.DefaultSetting.hideDelay,
    fadeInDuration: Duration = Tip.DefaultSetting.fadeInDuration,
    fadeInOffset: Float = Tip.DefaultSetting.fadeInOffset,
    optionalDirection: List<Direction> = Tip.DefaultSetting.optionalDirection,
    backgroundColor: ARGBColor = Tip.DefaultSetting.backgroundColor,
    modifier: Modifier = Modifier
) = hoverTip(Tip.Setting(showDelay, hideDelay, fadeInDuration, fadeInOffset, optionalDirection, backgroundColor), modifier) {
    TextLabel(text, setting = TextWidget.Setting(autoNewLine = true))
}