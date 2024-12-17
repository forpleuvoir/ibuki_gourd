package moe.forpleuvoir.ibukigourd.gui.widget.text

import kotlinx.coroutines.delay
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderText
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.util.ScrollAxis
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
import moe.forpleuvoir.nebula.common.util.defaultLaunch
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.text.Style
import kotlin.math.abs

class TextWidget(
    val text: State<Text>,
    val setting: Setting
) : IGWidgetImpl() {

    constructor(
        text: State<Text>,
        horizontalAlignment: Alignment.Horizontal = Alignment.Left,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        shadow: Boolean = false,
        scrollAxis: ScrollAxis = ScrollAxis.All,
        autoNewLine: Boolean = false,
        layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
        rightToLeft: Boolean = false,
        defaultColor: ARGBColor = Colors.BLACK,
        backgroundColor: ARGBColor = Color(0),
        textRenderer: TextRenderer = mc.textRenderer,
    ) : this(
        text,
        Setting(horizontalAlignment, verticalArrangement, shadow, scrollAxis, autoNewLine, layerType, rightToLeft, defaultColor, backgroundColor, textRenderer)
    )

    data class Setting(
        var horizontalAlignment: Alignment.Horizontal = Alignment.Left,
        var verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        var shadow: Boolean = false,
        var scrollAxis: ScrollAxis = ScrollAxis.All,
        var autoNewLine: Boolean = false,
        var layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
        var rightToLeft: Boolean = false,
        var defaultColor: ARGBColor = Colors.BLACK,
        var backgroundColor: ARGBColor = Color(0),
        var textRenderer: TextRenderer = mc.textRenderer,
    )

    //------------ Override ------------\\

    override fun measure(constraints: Constraints): Placeable {
        val c = this.constraints.constraintAs(constraints)
        val width = text.getValue().wrapToTextLines(textRenderer).maxOf { textRenderer.getWidth(it) }.toFloat() + padding.width
        val spacing = setting.verticalArrangement.spacing
        val height = text.getValue().wrapToTextLines(
            textRenderer, if (setting.autoNewLine) (width - padding.width).toInt() else 0
        ).size * (textRenderer.fontHeight + spacing) - spacing + padding.height
        transform.set(width.coerceIn(c.widthRange), height.coerceIn(c.heightRange))
        return this
    }

    override fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) =
        renderText(context, delta)


    //------------ TextWidget ------------\\

    init {
//        text.subscribe { onChanged() }
        padding = Padding(horizontal = 0f, vertical = 1f)
        margin = Margin(horizontal = 0f, vertical = -1f)
    }


    private var latestText: Text = text.getValue()

    private fun updateText() {
        val text = this.text.getValue()
        if (latestText != text) {
            latestText = text
            onChanged()
        }
    }

    private val textRenderer by setting::textRenderer

    private var renderText: List<McText> = text.getValue().wrapToTextLines(textRenderer, if (setting.autoNewLine) contentWidth.toInt() else 0)

    fun onChanged() {
        renderText = text.getValue().wrapToTextLines(textRenderer, if (setting.autoNewLine) contentWidth.toInt() else 0)
        if (!constraints.fixed()) {
            defaultLaunch {
                delay(1)
                screen()?.remeasure()
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
            val textWidth = textRenderer.getWidth(it)
            val minX = contentBox.right - textWidth
            val maxX = contentBox.left
            minX to maxX
        }

    private var xScrollSpeed: Float = 0.5f

    private var yScrollEasing: Ease = SineEasing::easeInOut

    private val yScrollRange: Pair<Float, Float>
        get() {
            val contentBox = contentBox(false)
            val textHeight = renderText.totalHeight(textRenderer, setting.verticalArrangement.spacing)
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
        val shouldScroll = textRenderer.getWidth(renderText[index]) > contentWidth
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
        val shouldScroll = renderText.totalHeight(textRenderer, setting.verticalArrangement.spacing) > contentHeight
        if (!shouldScroll) return y
        //从min滚动到max所需要的tick
        val ticks = height / yScrollSpeed
        //滚动状态 true = forward, false = back
        val state = (tickCounter / ticks).toInt() and 1 == 0
        val yOffset = yScrollEasing(((tickCounter % ticks) / ticks).coerceIn(0f..1f)) * height
        val top = transform.worldTop + padding.top + (index * (textRenderer.fontHeight + setting.verticalArrangement.spacing))
        return top - if (state) height - yOffset else yOffset
    }

    //------------ Render ------------\\

    private fun renderText(context: IGDrawContext, delta: Float) {
        updateText()
        tickCounter += delta
        val contentBox = contentBox(true)
        val renderText = renderText
        val list = renderText.map { text ->
            if (text != renderText.last()) {
                Size(textRenderer.getWidth(text).toFloat(), textRenderer.fontHeight + setting.verticalArrangement.spacing)
            } else
                Size(textRenderer.getWidth(text).toFloat(), textRenderer.fontHeight.toFloat())
        }
        context.useScissor(transform.asWorldCoordinateBox) {
            useMatrixStack { matrixStack ->
                matrixStack.translate(0.0f, 0.4f, 0f)
                //------------ 开始渲染 ------------\\
                batchRenderText(textRenderer) {
                    list.map { contentBox.left + setting.horizontalAlignment.align(contentBox.width, it.width) }
                        .zip(setting.verticalArrangement.arrange(contentBox.height, list.map { it.height }).map { contentBox.top + it })
                        .forEachIndexed { index, (x, y) ->
                            pushText(
                                renderText[index],
                                textScrolledXPos(index, x),
                                textScrolledYPos(index, y),
                                setting.shadow,
                                setting.layerType,
                                color = setting.defaultColor,
                                backgroundColor = setting.backgroundColor,
                                LightmapTextureManager.MAX_LIGHT_COORDINATE,
                                setting.rightToLeft
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

fun WidgetContainerScope.TextLabel(
    text: State<Text>,
    modifier: Modifier = Modifier,
    setting: TextSetting = TextSetting(),
    scope: TextWidgetScope.() -> Unit = {}
) = addWidgetChild(TextWidget(text, setting)) {
    modifier.foldInApply()
    TextWidgetScope { this }.scope()
}

fun WidgetContainerScope.TextLabel(
    text: Text,
    modifier: Modifier = Modifier,
    setting: TextSetting = TextSetting(),
    scope: TextWidgetScope.() -> Unit = {}
) = TextLabel(stateOf(text), modifier, setting, scope)

@JvmName("TextString")
fun WidgetContainerScope.TextLabel(
    str: String,
    style: Style = Style.EMPTY,
    modifier: Modifier = Modifier,
    setting: TextSetting = TextSetting(),
    scope: TextWidgetScope.() -> Unit = {}
) = TextLabel(Literal(str).setStyle(style), modifier, setting, scope)

@JvmName("TextString")
fun WidgetContainerScope.TextLabel(
    str: State<String>,
    style: Style = Style.EMPTY,
    modifier: Modifier = Modifier,
    setting: TextSetting = TextSetting(),
    scope: TextWidgetScope.() -> Unit = {}
) = TextLabel(mutableStateOf(str) { Literal(it).setStyle(style) }, modifier, setting, scope)
