@file:Suppress("unused", "MemberVisibilityCanBePrivate")
@file:OptIn(ExperimentalTypeInference::class)

package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.element.MeasureSpec
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.render.arrange.PlanarAlignment
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.gui.render.context.extension.batchRenderText
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.widget.util.ScrollingAxis
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.BACKGROUND_COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.RIGHT_TO_LEFT
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.SHADOW
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.SPACING
import moe.forpleuvoir.ibukigourd.render.math.bezier.Ease
import moe.forpleuvoir.ibukigourd.render.math.bezier.SineEasing
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.McText
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.wrapToTextLines
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.pick
import moe.forpleuvoir.nebula.common.sumOf
import moe.forpleuvoir.nebula.common.util.clamp
import net.minecraft.client.font.TextRenderer
import net.minecraft.text.Style
import kotlin.experimental.ExperimentalTypeInference
import kotlin.math.min


open class TextFieldWidget(
    val text: () -> Text,
    val setting: TextFieldSetting,
    modifier: Modifier = Modifier
) : AbstractElement(modifier) {

    data class TextFieldSetting(
        var spacing: Float = SPACING,
        var shadow: Boolean = SHADOW,
        var layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
        var rightToLeft: Boolean = RIGHT_TO_LEFT,
        var backgroundColor: Color = BACKGROUND_COLOR,
        val alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
        val textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer
    )

    constructor(
        text: () -> Text,
        spacing: Float = SPACING,
        shadow: Boolean = SHADOW,
        layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
        rightToLeft: Boolean = RIGHT_TO_LEFT,
        backgroundColor: Color = BACKGROUND_COLOR,
        alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
        textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
        modifier: Modifier = Modifier
    ) : this(
        text, TextFieldSetting(spacing, shadow, layerType, rightToLeft, backgroundColor, alignment, textRenderer), modifier
    )

    fun setting(setting: TextFieldSetting.() -> Unit) = this.setting.apply(setting)

    private val textRenderer by setting::textRenderer

    /**
     * 最新的文本
     */
    protected var latestText: Text = text()

    /**
     * 当宽度不够是是否自动换行
     */
    val autoNewLine: Boolean = false

    /**
     * 是否启用文本滚动
     */
    var scrollingAxis: ScrollingAxis? = ScrollingAxis.X

    /**
     * 鼠标悬浮时启用滚动,只有当[scrollingAxis] != `null`时有效
     */
    var hoverScroller: Boolean = false

    var scrollerEasing: Ease = SineEasing::easeInOut

    /**
     * 每一tick X轴移动的距离
     */
    var xScrollerSpeed: Float = 0.5f

    var xScrollerForward: MutableList<Float> = mutableListOf()

    /**
     * 每一tick Y轴移动的距离
     */
    var yScrollerSpeed: Float = 0.5f

    var yScrollerForward: Float = 1f

    protected val textXOffset: MutableList<Float> = mutableListOf()

    protected val currentXOffset: MutableList<Float> = mutableListOf()

    protected var textYOffset: Float = 0f

    protected var currentYOffset: Float = 0f
        set(value) {
            field = value.clamp(0f, textYOffset)
        }

    protected val renderText: List<McText>
        get() {
            val text = text().wrapToTextLines(textRenderer, if (scrollingAxis == ScrollingAxis.X) transform.width.toInt() else 0)
            if (latestText != text()) {
                latestText = text()
                onChanged()
            }
            return text
        }

    override val layout: Layout = object : Layout {
        override fun Element.layout() {}
        override fun Element.measureWidth(measureSpec: MeasureSpec): Float = 0f
        override fun Element.measureHeight(measureSpec: MeasureSpec): Float = 0f
    }

    override fun onLayout() = Unit

    override fun onMeasureWidth(measureSpec: MeasureSpec): Float {
        if (measureSpec.mode == MeasureSpec.Mode.EXACTLY) {
            setMeasureWidth(measureSpec.value)
        } else {
            setMeasureWidth(min(textRenderer.getWidth(text()).toFloat(), measureSpec.value - margin.width))
        }
        return transform.width + margin.width
    }

    override fun onMeasureHeight(measureSpec: MeasureSpec): Float {
        if (measureSpec.mode == MeasureSpec.Mode.EXACTLY) {
            setMeasureHeight(measureSpec.value)
        } else {
            val currentText = text()
            val height = currentText.wrapToTextLines(textRenderer, autoNewLine.pick(contentWidth.toInt(), 0))
                .sumOf { textRenderer.getWidth(it).toFloat() + setting.spacing } - setting.spacing

            setMeasureWidth(min(height, measureSpec.value - margin.height))
        }
        return transform.height + margin.height
    }

    private fun onChanged() {
        screen().takeIf { it.isInitialized }?.let { screen ->
            screen.screenLayout()

            currentXOffset.clear()
            xScrollerForward.clear()
            textXOffset.apply {
                clear()
                for ((index, text) in renderText.withIndex()) {
                    this.add(index, (textRenderer.getWidth(text).toFloat() - transform.width).coerceAtLeast(0f))
                    currentXOffset.add(index, 0f)
                    xScrollerForward.add(index, 1f)
                }
            }

            currentYOffset = 0f
            yScrollerForward = 1f
            textYOffset = (renderText.size * (textRenderer.fontHeight + setting.spacing) - setting.spacing - transform.height).coerceAtLeast(0f)
        }
    }

    override fun onRender(renderContext: RenderContext) {
        renderContext.tryRender {
            renderBackground(this)
        }
//        renderContext.scissor(transform.asWorldBox) {
            renderText(renderContext)
//        }
        renderContext.tryRender {
            renderOverlay(this)
        }
    }

    protected fun renderText(renderContext: RenderContext) {
        val contentRect = contentBox(true)
        val renderText = renderText

        val list = buildList {
            renderText.forEachIndexed { index, text ->
                if (renderText.lastIndex != index)
                    add(Box(0f, 0f, textRenderer.getWidth(text), textRenderer.fontHeight + setting.spacing))
                else
                    add(Box(0f, 0f, textRenderer.getWidth(text), textRenderer.fontHeight))
            }
        }

        currentYOffset += renderContext.tickCounter.lastFrameDuration * yScrollerSpeed * yScrollerForward
        if (currentYOffset == textYOffset) yScrollerForward = -1f
        if (currentYOffset <= 0f) yScrollerForward = 1f

        var originYOffset = 0f

        renderContext.useMatrixStack { matrixStack ->
            matrixStack.translate(0.0f, 0.4f, 0f)
            batchRenderText {
                if (scrollingAxis != null && hoverScroller.pick(mouseHover(), true)) {//启用滚动
                    setting.alignment(Orientation.Vertical).align(contentRect, list).forEachIndexed { index, vec ->
                        if (index == 0) originYOffset = vec.y() - transform.worldTop

                        if (currentXOffset.isNotEmpty() && index in currentXOffset.indices) {
                            currentXOffset[index] =
                                (currentXOffset[index] + renderContext.tickCounter.lastFrameDuration * xScrollerSpeed * xScrollerForward[index]).clamp(
                                    0f,
                                    textXOffset[index]
                                )
                            if (currentXOffset[index] >= textXOffset[index]) xScrollerForward[index] = -1f
                            if (currentXOffset[index] <= 0f) xScrollerForward[index] = 1f
                        }

                        val originXOffset = if (textXOffset.getOrElse(index) { 0f } != 0f) vec.x() - transform.worldLeft else 0f

                        val yEasing = (scrollerEasing(currentYOffset / textYOffset) * textYOffset)
                            .let { if (it.isNaN()) 0f else it }

                        val xEasing = (currentXOffset.getOrNull(index)?.let { scrollerEasing(it / textXOffset[index]) * textXOffset[index] } ?: 0f)
                            .let { if (it.isNaN()) 0f else it }

                        text(
                            renderText[index],
                            vec.x() - xEasing - originXOffset, vec.y() - yEasing - originYOffset,
                            setting.shadow, setting.layerType, setting.rightToLeft, setting.backgroundColor
                        )
                    }
                } else {
                    setting.alignment(Orientation.Vertical).align(contentRect, list).forEachIndexed { index, vec ->
                        text(
                            renderText[index],
                            vec.x(), vec.y(), setting.shadow, setting.layerType, setting.rightToLeft, setting.backgroundColor
                        )
                    }
                }
            }
        }
    }

}

/**
 * @receiver Element
 * @param text String 文本
 * @param style Style 样式
 * @param spacing Float 行间距
 * @param shadow Boolean 是否有阴影
 * @param layerType TextRenderer.TextLayerType 渲染层
 * @param rightToLeft Boolean 是否从右到左
 * @param backgroundColor Color 背景颜色
 * @param alignment (Arrangement) -> Alignment 对齐方式
 * @param textRenderer TextRenderer 文本渲染器
 * @param modifier [Modifier]
 * @return TextField
 */
fun ElementContainer.textField(
    text: String,
    style: Style = Style.EMPTY,
    spacing: Float = SPACING,
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
    modifier: Modifier = Modifier
): TextFieldWidget = addElement(
    TextFieldWidget(
        { Literal(text).styled { style } },
        spacing, shadow, layerType, rightToLeft, backgroundColor, alignment, textRenderer, modifier
    )
)

/**
 * @param text String 文本
 * @param style Style 样式
 * @param spacing Float 行间距
 * @param shadow Boolean 是否有阴影
 * @param layerType TextRenderer.TextLayerType 渲染层
 * @param rightToLeft Boolean 是否从右到左
 * @param backgroundColor Color 背景颜色
 * @param alignment (Arrangement) -> Alignment 对齐方式
 * @param textRenderer TextRenderer 文本渲染器
 * @param modifier [Modifier]
 * @return TextField
 */
fun TextField(
    text: String,
    style: Style = Style.EMPTY,
    spacing: Float = SPACING,
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
    modifier: Modifier = Modifier
): TextFieldWidget = TextFieldWidget(
    { Literal(text).styled { style } },
    spacing, shadow, layerType, rightToLeft, backgroundColor, alignment, textRenderer, modifier
)


/**
 * @receiver ElementContainer
 * @param text () -> String 文本
 * @param style Style 样式
 * @param spacing Float 行间距
 * @param shadow Boolean 是否有阴影
 * @param layerType TextRenderer.TextLayerType 渲染层
 * @param rightToLeft Boolean 是否从右到左
 * @param backgroundColor Color 背景颜色
 * @param alignment (Arrangement) -> Alignment 对齐方式
 * @param textRenderer TextRenderer 文本渲染器
 * @param modifier [Modifier]
 * @return TextField
 */
@OverloadResolutionByLambdaReturnType
fun ElementContainer.textField(
    text: () -> String,
    style: Style = Style.EMPTY,
    spacing: Float = SPACING,
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
    modifier: Modifier = Modifier
): TextFieldWidget = addElement(
    TextFieldWidget(
        { Literal(text()).styled { style } },
        spacing, shadow, layerType, rightToLeft, backgroundColor, alignment, textRenderer, modifier
    )
)

/**
 * @receiver ElementContainer
 * @param text () -> String 文本
 * @param style Style 样式
 * @param spacing Float 行间距
 * @param shadow Boolean 是否有阴影
 * @param layerType TextRenderer.TextLayerType 渲染层
 * @param rightToLeft Boolean 是否从右到左
 * @param backgroundColor Color 背景颜色
 * @param alignment (Arrangement) -> Alignment 对齐方式
 * @param textRenderer TextRenderer 文本渲染器
 * @param modifier [Modifier]
 * @return TextField
 */
@OverloadResolutionByLambdaReturnType
fun TextField(
    text: () -> String,
    style: Style = Style.EMPTY,
    spacing: Float = SPACING,
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
    modifier: Modifier = Modifier
): TextFieldWidget = TextFieldWidget(
    { Literal(text()).styled { style } },
    spacing, shadow, layerType, rightToLeft, backgroundColor, alignment, textRenderer, modifier
)


/**
 * @receiver ElementContainer
 * @param text () -> Text 文本
 * @param spacing Float 行间距
 * @param shadow Boolean 是否有阴影
 * @param layerType TextRenderer.TextLayerType 渲染层
 * @param rightToLeft Boolean 是否从右到左
 * @param backgroundColor Color 背景颜色
 * @param alignment (Arrangement) -> Alignment 对齐方式
 * @param textRenderer TextRenderer 文本渲染器
 * @param modifier [Modifier]
 * @return TextField
 */
@OverloadResolutionByLambdaReturnType
fun ElementContainer.textField(
    text: () -> Text,
    spacing: Float = SPACING,
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
    modifier: Modifier = Modifier
): TextFieldWidget = addElement(TextFieldWidget(text, spacing, shadow, layerType, rightToLeft, backgroundColor, alignment, textRenderer, modifier))

/**
 * @receiver ElementContainer
 * @param text () -> Text 文本
 * @param spacing Float 行间距
 * @param shadow Boolean 是否有阴影
 * @param layerType TextRenderer.TextLayerType 渲染层
 * @param rightToLeft Boolean 是否从右到左
 * @param backgroundColor Color 背景颜色
 * @param alignment (Arrangement) -> Alignment 对齐方式
 * @param textRenderer TextRenderer 文本渲染器
 * @param modifier [Modifier]
 * @return TextField
 */
@OverloadResolutionByLambdaReturnType
fun TextField(
    text: () -> Text,
    spacing: Float = SPACING,
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
    modifier: Modifier = Modifier
): TextFieldWidget = TextFieldWidget(text, spacing, shadow, layerType, rightToLeft, backgroundColor, alignment, textRenderer, modifier)

/**
 * @receiver ElementContainer
 * @param text Text 文本
 * @param spacing Float 行间距
 * @param shadow Boolean 是否有阴影
 * @param layerType TextRenderer.TextLayerType 渲染层
 * @param rightToLeft Boolean 是否从右到左
 * @param backgroundColor Color 背景颜色
 * @param alignment (Arrangement) -> Alignment 对齐方式
 * @param textRenderer TextRenderer 文本渲染器
 * @param modifier [Modifier]
 * @return TextField
 */
fun ElementContainer.textField(
    text: Text,
    spacing: Float = SPACING,
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
    modifier: Modifier = Modifier
): TextFieldWidget =
    addElement(TextFieldWidget({ text }, spacing, shadow, layerType, rightToLeft, backgroundColor, alignment, textRenderer, modifier))

/**
 * @receiver ElementContainer
 * @param text Text 文本
 * @param spacing Float 行间距
 * @param shadow Boolean 是否有阴影
 * @param layerType TextRenderer.TextLayerType 渲染层
 * @param rightToLeft Boolean 是否从右到左
 * @param backgroundColor Color 背景颜色
 * @param alignment (Arrangement) -> Alignment 对齐方式
 * @param textRenderer TextRenderer 文本渲染器
 * @param modifier [Modifier]
 * @return TextField
 */
fun TextField(
    text: Text,
    spacing: Float = SPACING,
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Orientation) -> Alignment = PlanarAlignment::Center,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
    modifier: Modifier = Modifier
): TextFieldWidget = TextFieldWidget({ text }, spacing, shadow, layerType, rightToLeft, backgroundColor, alignment, textRenderer, modifier)