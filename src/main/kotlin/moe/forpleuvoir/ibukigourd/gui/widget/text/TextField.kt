@file:Suppress("unused", "MemberVisibilityCanBePrivate")
@file:OptIn(ExperimentalTypeInference::class)

package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.BACKGROUND_COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.RIGHT_TO_LEFT
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.SHADOW
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.SPACING
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Alignment
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import moe.forpleuvoir.ibukigourd.render.base.math.bezier.Ease
import moe.forpleuvoir.ibukigourd.render.base.math.bezier.SineEasing
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.helper.batchRender
import moe.forpleuvoir.ibukigourd.util.text.Text
import moe.forpleuvoir.ibukigourd.util.text.literal
import moe.forpleuvoir.ibukigourd.util.text.maxWidth
import moe.forpleuvoir.ibukigourd.util.text.wrapToTextLines
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.util.clamp
import net.minecraft.client.font.TextRenderer
import net.minecraft.text.Style
import kotlin.experimental.ExperimentalTypeInference


open class TextField(
    val text: () -> Text,
    override var spacing: Float = SPACING,
    var shadow: Boolean = SHADOW,
    var layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    var rightToLeft: Boolean = RIGHT_TO_LEFT,
    var color: Color = Color(text().style.color?.rgb ?: COLOR.argb),
    var backgroundColor: Color = BACKGROUND_COLOR,
    val alignment: (Arrangement) -> Alignment = PlanarAlignment::Center,
    private val textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
    width: Float? = null,
    height: Float? = null,
) : AbstractElement() {

    /**
     * 最新的文本
     */
    protected var latestText: Text = text()
    var changed: Boolean = false

    /**
     * 是否启用文本滚动
     */
    var scrollable: Boolean = width != null || height != null

    /**
     * 鼠标悬浮时启用滚动,只有当[scrollable]为true时有效
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
    protected val renderText: List<Text>
        get() {
            val text = text().wrapToTextLines(textRenderer, if (transform.fixedWidth && !scrollable) transform.width.toInt() else 0)
            if (latestText != text()) {
                changed = true
                latestText = text()
            }
            return text
        }

    init {
        transform.width = width?.also {
            transform.fixedWidth = true
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
        } ?: 16f
        transform.height = height?.also {
            transform.fixedHeight = true
            currentYOffset = 0f
            yScrollerForward = 1f
            textYOffset = (renderText.size * (textRenderer.fontHeight + spacing) - spacing - transform.height).coerceAtLeast(0f)
        } ?: 16f
    }

    override fun init() {
        resize()
    }

    fun resize() {
        var changed = false
        if (!transform.fixedWidth) {
            transform.width = (renderText.maxWidth(textRenderer).toFloat() + padding.width).coerceAtLeast(1f)
            changed = true
        } else {
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
        }
        if (!transform.fixedHeight) {
            transform.height = (renderText.size * (textRenderer.fontHeight + spacing) - spacing + padding.height).coerceAtLeast(1f)
            changed = true
        } else {
            currentYOffset = 0f
            yScrollerForward = 1f
            textYOffset = (renderText.size * (textRenderer.fontHeight + spacing) - spacing - transform.height).coerceAtLeast(0f)
        }
        if (changed) parent().arrange()

    }

    override fun onRender(renderContext: RenderContext) {
        if (changed) {
            resize()
            changed = false
        }
        renderBackground(renderContext)
        renderContext.scissor(transform.asWorldRect) {
            renderText(renderContext)
        }
        renderOverlay(renderContext)
    }

    protected fun renderText(renderContext: RenderContext) {
        val contentRect = contentRect(true)
        val renderText = renderText

        val list = buildList {
            renderText.forEachIndexed { index, text ->
                if (renderText.lastIndex != index)
                    add(rect(vertex(0f, 0f, transform.z), textRenderer.getWidth(text), textRenderer.fontHeight + spacing))
                else
                    add(rect(vertex(0f, 0f, transform.z), textRenderer.getWidth(text), textRenderer.fontHeight))
            }
        }

        currentYOffset += renderContext.tickDelta * yScrollerSpeed * yScrollerForward
        if (currentYOffset == textYOffset) yScrollerForward = -1f
        if (currentYOffset <= 0f) yScrollerForward = 1f

        var originYOffset = 0f

        renderContext.matrixStack {
            matrixStack.translate(0.0f, 0.4f, 0f)
            textRenderer.batchRender {
                if (scrollable && if (hoverScroller) mouseHover() else true) {
                    alignment(Arrangement.Vertical).align(contentRect, list).forEachIndexed { index, vec ->

                        if (index == 0) originYOffset = vec.y - transform.worldTop

                        if (currentXOffset.isNotEmpty() && index in currentXOffset.indices) {
                            currentXOffset[index] = (currentXOffset[index] + renderContext.tickDelta * xScrollerSpeed * xScrollerForward[index]).clamp(0f, textXOffset[index])
                            if (currentXOffset[index] >= textXOffset[index]) xScrollerForward[index] = -1f
                            if (currentXOffset[index] <= 0f) xScrollerForward[index] = 1f
                        }
                        val originXOffset = if (textXOffset.getOrElse(index) { 0f } != 0f) vec.x - transform.worldLeft else 0f

                        val yEasing = (scrollerEasing(currentYOffset / textYOffset) * textYOffset)
                            .let { if (it.isNaN()) 0f else it }

                        val xEasing = (currentXOffset.getOrNull(index)?.let { scrollerEasing(it / textXOffset[index]) * textXOffset[index] } ?: 0f)
                            .let { if (it.isNaN()) 0f else it }

                        renderText(
                            renderContext.matrixStack, renderText[index],
                            vec.x - xEasing - originXOffset, vec.y - yEasing - originYOffset, vec.z,
                            shadow, layerType, rightToLeft, color, backgroundColor
                        )
                    }
                } else {
                    alignment(Arrangement.Vertical).align(contentRect, list).forEachIndexed { index, vec ->
                        renderText(
                            renderContext.matrixStack,
                            renderText[index],
                            vec.x, vec.y, vec.z, shadow, layerType, rightToLeft, color, backgroundColor
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
 * @param color Color 文本颜色
 * @param backgroundColor Color 背景颜色
 * @param alignment (Arrangement) -> Alignment 对齐方式
 * @param textRenderer TextRenderer 文本渲染器
 * @param width Float? 宽度 null -> auto,!null - value
 * @param height Float? 高度 null -> auto,!null - value
 * @return TextField
 */
fun Element.text(
    text: String,
    style: Style = Style.EMPTY,
    spacing: Float = SPACING,
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    color: Color = Color(style.color?.rgb ?: COLOR.argb),
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Arrangement) -> Alignment = PlanarAlignment::Center,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
    width: Float? = null,
    height: Float? = null,
): TextField =
    addElement(
        TextField(
            { literal(text).style { style } },
            spacing,
            shadow,
            layerType,
            rightToLeft,
            color,
            backgroundColor,
            alignment,
            textRenderer,
            width,
            height
        )
    )

/**
 * @receiver Element
 * @param text () -> String 文本
 * @param style Style 样式
 * @param spacing Float 行间距
 * @param shadow Boolean 是否有阴影
 * @param layerType TextRenderer.TextLayerType 渲染层
 * @param rightToLeft Boolean 是否从右到左
 * @param color Color 文本颜色
 * @param backgroundColor Color 背景颜色
 * @param alignment (Arrangement) -> Alignment 对齐方式
 * @param textRenderer TextRenderer 文本渲染器
 * @param width Float? 宽度 null -> auto,!null - value
 * @param height Float? 高度 null -> auto,!null - value
 * @return TextField
 */
@OverloadResolutionByLambdaReturnType
fun Element.text(
    text: () -> String,
    style: Style = Style.EMPTY,
    spacing: Float = SPACING,
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    color: Color = Color(style.color?.rgb ?: COLOR.argb),
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Arrangement) -> Alignment = PlanarAlignment::Center,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
    width: Float? = null,
    height: Float? = null,
): TextField =
    addElement(
        TextField(
            { literal(text()).style { style } },
            spacing,
            shadow,
            layerType,
            rightToLeft,
            color,
            backgroundColor,
            alignment,
            textRenderer,
            width,
            height
        )
    )

/**
 * @receiver Element
 * @param text () -> Text 文本
 * @param spacing Float 行间距
 * @param shadow Boolean 是否有阴影
 * @param layerType TextRenderer.TextLayerType 渲染层
 * @param rightToLeft Boolean 是否从右到左
 * @param color Color 文本颜色
 * @param backgroundColor Color 背景颜色
 * @param alignment (Arrangement) -> Alignment 对齐方式
 * @param textRenderer TextRenderer 文本渲染器
 * @param width Float? 宽度 null -> auto,!null - value
 * @param height Float? 高度 null -> auto,!null - value
 * @return TextField
 */
@OverloadResolutionByLambdaReturnType
fun Element.text(
    text: () -> Text,
    spacing: Float = SPACING,
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    color: Color = Color(text().style.color?.rgb ?: COLOR.argb),
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Arrangement) -> Alignment = PlanarAlignment::Center,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
    width: Float? = null,
    height: Float? = null,
): TextField = addElement(TextField(text, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer, width, height))

/**
 * @receiver Element
 * @param text Text 文本
 * @param spacing Float 行间距
 * @param shadow Boolean 是否有阴影
 * @param layerType TextRenderer.TextLayerType 渲染层
 * @param rightToLeft Boolean 是否从右到左
 * @param color Color 文本颜色
 * @param backgroundColor Color 背景颜色
 * @param alignment (Arrangement) -> Alignment 对齐方式
 * @param textRenderer TextRenderer 文本渲染器
 * @param width Float? 宽度 null -> auto,!null - value
 * @param height Float? 高度 null -> auto,!null - value
 * @return TextField
 */
fun Element.text(
    text: Text,
    spacing: Float = SPACING,
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    color: Color = Color(text.style.color?.rgb ?: COLOR.argb),
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Arrangement) -> Alignment = PlanarAlignment::Center,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
    width: Float? = null,
    height: Float? = null,
): TextField = addElement(TextField({ text }, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer, width, height))