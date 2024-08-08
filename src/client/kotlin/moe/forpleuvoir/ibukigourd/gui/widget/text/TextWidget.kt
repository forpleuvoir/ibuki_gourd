package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderText
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.BoxAlignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.util.ScrollAxis
import moe.forpleuvoir.ibukigourd.render.math.bezier.Ease
import moe.forpleuvoir.ibukigourd.render.math.bezier.SineEasing
import moe.forpleuvoir.ibukigourd.text.*
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.client.font.TextRenderer
import kotlin.math.abs

class TextWidget(
    val text: () -> Text,
    val setting: TextSetting
) : IGWidgetImpl() {

    constructor(
        text: () -> Text,
        spacing: Float = 1f,
        shadow: Boolean = false,
        scrollAxis: ScrollAxis = ScrollAxis.All,
        autoNewLine: Boolean = false,
        layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
        rightToLeft: Boolean = false,
        backgroundColor: ARGBColor = Color(0),
        alignment: (Orientation) -> Alignment = BoxAlignment::CenterCenter,
        textRenderer: TextRenderer = mc.textRenderer,
    ) : this(text, TextSetting(spacing, shadow, scrollAxis, autoNewLine, layerType, rightToLeft, backgroundColor, alignment, textRenderer))

    data class TextSetting(
        var spacing: Float = 0f,
        var shadow: Boolean = false,
        var scrollAxis: ScrollAxis = ScrollAxis.All,
        var autoNewLine: Boolean = false,
        var layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
        var rightToLeft: Boolean = false,
        var backgroundColor: ARGBColor = Color(0),
        var alignment: (Orientation) -> Alignment = BoxAlignment::CenterCenter,
        var textRenderer: TextRenderer = mc.textRenderer,
    )

    //------------ Override ------------\\

    override fun measure(constraints: Constraints): Placeable {
        val c = this.constraints.constraint(constraints)
        val width = text().wrapToTextLines(textRenderer).maxOf { textRenderer.getWidth(it) }.toFloat() + padding.width
        val height =
            text().wrapToTextLines(textRenderer, c.maxWidth.toInt()).size * (textRenderer.fontHeight + setting.spacing) - setting.spacing + padding.height
        transform.set(width.coerceIn(c.widthRange), height.coerceIn(c.heightRange))
        return this
    }

    override fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) =
        renderText(context, delta)


    //------------ TextWidget ------------\\

    /**
     * 最新的文本
     */
    var latestText: Text = text()
        set(value) {
            field = value
            onChanged()
        }

    private val textRenderer by setting::textRenderer

    private var cachedRenderTextLines: List<McText> = text().wrapToTextLines(textRenderer, if (setting.autoNewLine) contentWidth.toInt() else 0)

    private val renderText: List<McText>
        get() {
            if (latestText != text()) {
                latestText = text()
                cachedRenderTextLines = text().wrapToTextLines(textRenderer, if (setting.autoNewLine) contentWidth.toInt() else 0)
            }
            return cachedRenderTextLines
        }

    private fun onChanged() {
        screen()?.measure(Constraints())
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
            val textHeight = renderText.totalHeight(textRenderer, setting.spacing)
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
        val shouldScroll = width > contentWidth
        if (!shouldScroll) return x
        //从min滚动到max所需要的tick
        val ticks = width / xScrollSpeed
        //滚动状态 true = forward, false = back
        val state = (tickCounter / ticks).toInt() and 1 == 0
        val xOffset = xScrollEasing(((tickCounter % ticks) / ticks).coerceIn(0f..1f)) * width
        return transform.worldLeft - if (state) width - xOffset else xOffset
    }

    private fun textScrolledYPos(index: Int, y: Float): Float {
        //是否启用X滚动
        if (!scrollAxis.isY) return y
        //是否启用鼠标停留时滚动
        if (!wasMouseOver && hoverScroll.isY) return y
        val (minY, maxY) = yScrollRange
        //滚动宽度
        val height = abs(maxY - minY)
        val shouldScroll = height > contentHeight
        if (!shouldScroll) return y
        //从min滚动到max所需要的tick
        val ticks = height / yScrollSpeed
        //滚动状态 true = forward, false = back
        val state = (tickCounter / ticks).toInt() and 1 == 0
        val yOffset = yScrollEasing(((tickCounter % ticks) / ticks).coerceIn(0f..1f)) * height
        val top = transform.worldTop + (index * (textRenderer.fontHeight + setting.spacing))
        return top - if (state) height - yOffset else yOffset
    }

    //------------ Render ------------\\

    private fun renderText(context: IGDrawContext, delta: Float) {
        tickCounter += delta
        val contentBox = contentBox(true)
        val renderText = renderText
        val list = renderText.map { text ->
            if (text != renderText.last()) {
                Size(textRenderer.getWidth(text).toFloat(), textRenderer.fontHeight + setting.spacing)
            } else
                Size(textRenderer.getWidth(text).toFloat(), textRenderer.fontHeight.toFloat())
        }

        context.useScissor(transform.asWorldBox) {
            useMatrixStack { matrixStack ->
                matrixStack.translate(0.0f, 0.4f, 0f)
                //------------ 开始渲染 ------------\\
                batchRenderText(textRenderer) {
                    setting.alignment(Orientation.Vertical).align(contentBox, list).forEachIndexed { index, vec ->
                        text(
                            renderText[index],
                            textScrolledXPos(index, vec.x()),
                            textScrolledYPos(index, vec.y()),
                            setting.shadow,
                            setting.layerType,
                            setting.rightToLeft,
                            backgroundColor = setting.backgroundColor
                        )
                    }
                }
            }
        }
    }

    companion object {
        data class TextWidgetScope(private val textWidget: TextWidget) : GuiScope<TextWidget> {

            override fun owner(): TextWidget = textWidget

            inline fun setting(block: TextSetting.() -> Unit) {
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
}


fun GuiScope<out WidgetContainer>.text(
    text: () -> Text,
    setting: TextWidget.TextSetting = TextWidget.TextSetting(),
    modifier: Modifier? = null,
    scope: TextWidget.Companion.TextWidgetScope.() -> Unit = {}
) = owner().addWidgetChild(TextWidget(text, setting)) {
    modifier?.foldIn(Unit) { _, op ->
        op.tryApplyModify(this)
    }
    TextWidget.Companion.TextWidgetScope(this).scope()
}

fun GuiScope<out WidgetContainer>.text(
    text: Text,
    setting: TextWidget.TextSetting = TextWidget.TextSetting(),
    modifier: Modifier? = null,
    scope: TextWidget.Companion.TextWidgetScope.() -> Unit = {}
) = owner().addWidgetChild(TextWidget({ text }, setting)) {
    modifier?.foldIn(Unit) { _, op ->
        op.tryApplyModify(this)
    }
    TextWidget.Companion.TextWidgetScope(this).scope()
}

fun GuiScope<out WidgetContainer>.text(
    text: String,
    setting: TextWidget.TextSetting = TextWidget.TextSetting(),
    modifier: Modifier? = null,
    scope: TextWidget.Companion.TextWidgetScope.() -> Unit = {}
) = owner().addWidgetChild(TextWidget({ Literal(text) }, setting)) {
    modifier?.foldIn(Unit) { _, op ->
        op.tryApplyModify(this)
    }
    TextWidget.Companion.TextWidgetScope(this).scope()
}

fun GuiScope<out WidgetContainer>.text(
    text: () -> Text,
    spacing: Float = 1f,
    scrollAxis: ScrollAxis = ScrollAxis.All,
    shadow: Boolean = false,
    autoNewLine: Boolean = false,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = false,
    backgroundColor: ARGBColor = Color(0),
    alignment: (Orientation) -> Alignment = BoxAlignment::CenterCenter,
    textRenderer: TextRenderer = mc.textRenderer,
    modifier: Modifier? = null,
    scope: TextWidget.Companion.TextWidgetScope.() -> Unit = {}
) = owner().addWidgetChild(TextWidget(text, spacing, shadow, scrollAxis, autoNewLine, layerType, rightToLeft, backgroundColor, alignment, textRenderer)) {
    modifier?.foldIn(Unit) { _, op ->
        op.tryApplyModify(this)
    }
    TextWidget.Companion.TextWidgetScope(this).scope()
}

fun GuiScope<out WidgetContainer>.text(
    text: Text,
    spacing: Float = 1f,
    scrollAxis: ScrollAxis = ScrollAxis.All,
    shadow: Boolean = false,
    autoNewLine: Boolean = false,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = false,
    backgroundColor: ARGBColor = Color(0),
    alignment: (Orientation) -> Alignment = BoxAlignment::CenterCenter,
    textRenderer: TextRenderer = mc.textRenderer,
    modifier: Modifier? = null,
    scope: TextWidget.Companion.TextWidgetScope.() -> Unit = {}
) = owner().addWidgetChild(TextWidget({ text }, spacing, shadow, scrollAxis, autoNewLine, layerType, rightToLeft, backgroundColor, alignment, textRenderer)) {
    modifier?.foldIn(Unit) { _, op ->
        op.tryApplyModify(this)
    }
    TextWidget.Companion.TextWidgetScope(this).scope()
}

fun GuiScope<out WidgetContainer>.text(
    text: String,
    spacing: Float = 1f,
    scrollAxis: ScrollAxis = ScrollAxis.All,
    shadow: Boolean = false,
    autoNewLine: Boolean = false,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = false,
    backgroundColor: ARGBColor = Color(0),
    alignment: (Orientation) -> Alignment = BoxAlignment::CenterCenter,
    textRenderer: TextRenderer = mc.textRenderer,
    modifier: Modifier? = null,
    scope: TextWidget.Companion.TextWidgetScope.() -> Unit = {}
) = owner().addWidgetChild(
    TextWidget(
        { Literal(text) },
        spacing,
        shadow,
        scrollAxis,
        autoNewLine,
        layerType,
        rightToLeft,
        backgroundColor,
        alignment,
        textRenderer
    )
) {
    modifier?.foldIn(Unit) { _, op ->
        op.tryApplyModify(this)
    }
    TextWidget.Companion.TextWidgetScope(this).scope()
}