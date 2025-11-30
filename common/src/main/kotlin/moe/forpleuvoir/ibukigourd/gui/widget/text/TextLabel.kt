package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.textRenderOffset
import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.useMatrixStack
import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.useScissor
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.hoverTip
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.util.ScrollAxis
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.mod.config.GuiConfig
import moe.forpleuvoir.ibukigourd.text.*
import moe.forpleuvoir.ibukigourd.util.math.bezier.Ease
import moe.forpleuvoir.ibukigourd.util.math.bezier.SineEasing
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.world.item.Item
import net.minecraft.world.item.TooltipFlag
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.TimeSource

class TextWidget(
    val text: State<Text>,
    val setting: Setting
) : GuiWidgetImpl() {

    constructor(
        text: State<Text>,
        horizontalAlignment: Alignment.Horizontal = Alignment.Left,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        shadow: Boolean = false,
        scrollAxis: ScrollAxis = ScrollAxis.All,
        autoNewLine: Boolean = false,
//        layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.SEE_THROUGH,
        rightToLeft: Boolean = false,
        defaultColor: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Color(0),
        font: Font = mc.font,
    ) : this(
        text,
        Setting(horizontalAlignment, verticalArrangement, shadow, scrollAxis, autoNewLine, rightToLeft, defaultColor, backgroundColor, font)
    )

    data class Setting(
        var horizontalAlignment: Alignment.Horizontal = Alignment.Left,
        var verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        var shadow: Boolean = false,
        var scrollAxis: ScrollAxis = ScrollAxis.All,
        var autoNewLine: Boolean = false,
//        var layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.SEE_THROUGH,
        var rightToLeft: Boolean = false,
        var defaultColor: ARGBColor = Colors.BLACK,
        var backgroundColor: ARGBColor = Color(0),
        var font: Font = mc.font,
        var textLabelUpdateInterval: Duration = GuiConfig.textLabelUpdateInterval
    )

    //------------ Override ------------\\

    override fun measure(constraints: Constraints): Placeable {
        val c = this.constraints.merge(constraints)
        val width = text.getValue().wrapToTextLines(if (setting.autoNewLine) (c.maxWidth - padding.width) else 0f)
            .maxOfOrNull { it.width } ?: (0f + padding.width)
        val spacing = setting.verticalArrangement.spacing
        val height = text.getValue().totalHeight(spacing, width) + padding.height
        transform.set(width.coerceIn(c.widthRange), height.coerceIn(c.heightRange))
        renderText = text.getValue().wrapToTextLines(if (setting.autoNewLine) contentWidth else 0f)
        return this
    }

    override fun onRender(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) =
        renderText(guiGraphics, delta)


    //------------ TextWidget ------------\\

    private var latestText: Text = text.getValue()

    private var mark = TimeSource.Monotonic.markNow()

    fun updateText(force: Boolean = false) {
        if (!force && mark.elapsedNow() < setting.textLabelUpdateInterval) return
        mark = TimeSource.Monotonic.markNow()
        val text = this.text.getValue()
        if (latestText != text) {
            latestText = text
            onChanged()
        }
    }

    private val textRenderer by setting::font

    private var renderText: List<McText> = text.getValue().wrapToTextLines(if (setting.autoNewLine) contentWidth else 0f)

    fun onChanged() {
        renderText = text.getValue().wrapToTextLines(if (setting.autoNewLine) contentWidth else 0f)
        if (!constraints.fixed()) {
            runCatching {
                remeasure()
            }
        }
    }

    //------------ TextScroll ------------\\

    /**
     * 是否启用文本滚动
     */
    private var scrollAxis: ScrollAxis by setting::scrollAxis

    private var tickCounter: Float = 0f

    /**
     * 鼠标悬浮时启用滚动
     */
    private var hoverScroll: ScrollAxis = ScrollAxis.None

    private var xScrollEasing: Ease = SineEasing::easeInOut

    private val xScrollRange: List<Pair<Float, Float>>
        get() = renderText.map {
            val contentBox = contentBox(false)
            val textWidth = it.width
            val minX = contentBox.right - textWidth
            val maxX = contentBox.left
            minX to maxX
        }

    private var xScrollSpeed: Float = 0.5f

    private var yScrollEasing: Ease = SineEasing::easeInOut

    private val yScrollRange: Pair<Float, Float>
        get() {
            val contentBox = contentBox(false)
            val textHeight = renderText.totalHeight(setting.verticalArrangement.spacing)
            val minY = contentBox.bottom - textHeight
            val maxY = contentBox.top
            return minY to maxY
        }

    private var yScrollSpeed: Float = 0.5f


    private fun textScrolledXPos(index: Int, x: Float): Float {
        //是否启用X滚动
        if (!scrollAxis.isX) return x
        //是否启用鼠标停留时滚动
        if (!wasMouseOver && hoverScroll.isX) return x
        val (minX, maxX) = xScrollRange[index]
        //滚动宽度
        val width = abs(maxX - minX)
        val shouldScroll = renderText[index].width > contentWidth + 2f
        if (!shouldScroll) return x
        //从min滚动到max所需要的tick
        val ticks = width / xScrollSpeed
        //滚动状态 true = forward, false = back
        val state = (tickCounter / ticks).toInt() and 1 == 0
        val xOffset = xScrollEasing(((tickCounter % ticks) / ticks).coerceIn(0f..1f)) * width
        return transform.worldLeft + padding.left - if (state) width - xOffset else xOffset
    }

    private fun textScrolledYPos(index: Int, y: Float): Float {
        //是否启用X滚动
        if (!scrollAxis.isY) return y
        //是否启用鼠标停留时滚动
        if (!wasMouseOver && hoverScroll.isY) return y
        val (minY, maxY) = yScrollRange
        //滚动宽度
        val height = abs(maxY - minY)
        val shouldScroll = renderText.totalHeight(setting.verticalArrangement.spacing) > contentHeight + 2f
        if (!shouldScroll) return y
        //从min滚动到max所需要的tick
        val ticks = height / yScrollSpeed
        //滚动状态 true = forward, false = back
        val state = (tickCounter / ticks).toInt() and 1 == 0
        val yOffset = yScrollEasing(((tickCounter % ticks) / ticks).coerceIn(0f..1f)) * height
        val top = transform.worldTop + padding.top + (index * (textRenderer.lineHeight + setting.verticalArrangement.spacing))
        return top - if (state) height - yOffset else yOffset
    }

    //------------ Render ------------\\

    private fun renderText(guiGraphics: IGGuiGraphics, delta: Float) {
        updateText()
        tickCounter += delta
        val contentBox = contentBox(true)
        val renderText = renderText
        val list = renderText.map { text ->
            if (text != renderText.last()) {
                text.size.run {
                    Size(width, this.height + setting.verticalArrangement.spacing)
                }
            } else text.size
        }
        guiGraphics {
            useScissor(transform.asWorldCoordinateBox.expandEdges(1f)) {
                useMatrixStack { pose ->
                    pose.translate(0.35f, textRenderOffset.y())
                    //------------ 开始渲染 ------------\\
                    list.map { contentBox.left + setting.horizontalAlignment.align(contentBox.width, it.width) }
                        .zip(setting.verticalArrangement.arrange(contentBox.height, list.map { it.height }).map { contentBox.top + it })
                        .forEachIndexed { index, (x, y) ->
                            pushText(
                                renderText[index],
                                textScrolledXPos(index, x),
                                textScrolledYPos(index, y),
                                shadow = setting.shadow,
                                color = setting.defaultColor,
                                backgroundColor = setting.backgroundColor,
                                font = font
                            )
                        }
                }
            }
        }
    }

    fun interface Scope : GuiScope<TextWidget> {

        fun setting(block: Setting.() -> Unit) {
            owner().setting.block()
        }

        fun scrollAxis(scrollAxis: ScrollAxis) {
            owner().scrollAxis = scrollAxis
        }

        fun hoverScroll(hoverScroll: ScrollAxis) {
            owner().hoverScroll = hoverScroll
        }

        fun xScrollSpeed(speed: Float) {
            owner().xScrollSpeed = speed
        }

        fun xScrollEasing(ease: Ease) {
            owner().xScrollEasing = ease
        }

        fun yScrollSpeed(speed: Float) {
            owner().yScrollSpeed = speed
        }

        fun yScrollEasing(ease: Ease) {
            owner().yScrollEasing = ease
        }


    }
}

typealias TextWidgetScope = TextWidget.Scope

typealias TextSetting = TextWidget.Setting

fun ContainerScope.Text(
    text: State<Text>,
    modifier: Modifier = Modifier,
    setting: TextSetting = TextSetting(),
    scope: TextWidgetScope.() -> Unit = {}
) = addWidgetChild(TextWidget(text, setting)) {
    modifier.foldInApply()
    TextWidgetScope { this }.scope()
}

fun ContainerScope.Text(
    text: Text,
    modifier: Modifier = Modifier,
    setting: TextSetting = TextSetting(),
    withHoverEvent: Boolean = true,
    scope: TextWidgetScope.() -> Unit = {}
): TextWidget {
    val m = if (withHoverEvent) {
        text.style.hoverEvent?.let { hoverEvent ->
            Modifier.hoverTip {
                Column {
                    when (hoverEvent.action()) {
                        HoverEvent.Action.SHOW_TEXT   -> {
                            Text((hoverEvent as HoverEvent.ShowText).value.copyToText(), withHoverEvent = false)
                        }

                        HoverEvent.Action.SHOW_ENTITY -> {
                            (hoverEvent as HoverEvent.ShowEntity).entity.tooltipLines
                                .forEach {
                                    Text(it.copyToText(), withHoverEvent = false)
                                }
                        }

                        HoverEvent.Action.SHOW_ITEM   -> {
                            (hoverEvent as HoverEvent.ShowItem).item
                                .getTooltipLines(
                                    Item.TooltipContext.EMPTY,
                                    mc.player,
                                    mc.options.advancedItemTooltips.pick(TooltipFlag.ADVANCED, TooltipFlag.NORMAL)
                                ).forEach {
                                    Text(it.copyToText(), withHoverEvent = false)
                                }
                        }
                    }
                }
            }
        }
    } else null
    return Text(stateOf(text), (m ?: Modifier).then(modifier), setting, scope)
}

@JvmName("TextString")
fun ContainerScope.Text(
    str: String,
    style: Style = Style.EMPTY,
    modifier: Modifier = Modifier,
    setting: TextSetting = TextSetting(),
    scope: TextWidgetScope.() -> Unit = {}
) = Text(Literal(str).setStyle(style), modifier, setting, true, scope)

@JvmName("TextString")
fun ContainerScope.Text(
    str: State<String>,
    style: Style = Style.EMPTY,
    modifier: Modifier = Modifier,
    setting: TextSetting = TextSetting(),
    scope: TextWidgetScope.() -> Unit = {}
) = Text(mutableStateOf(str) { Literal(it).setStyle(style) }, modifier, setting, scope)
