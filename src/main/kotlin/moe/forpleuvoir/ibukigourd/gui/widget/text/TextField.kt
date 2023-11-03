@file:Suppress("unused", "MemberVisibilityCanBePrivate")
@file:OptIn(ExperimentalTypeInference::class)

package moe.forpleuvoir.ibukigourd.gui.widget.text

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.BACKGROUND_COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.RIGHT_TO_LEFT
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.SHADOW
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.SPACING
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.Alignment
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.helper.renderText
import moe.forpleuvoir.ibukigourd.util.text.Text
import moe.forpleuvoir.ibukigourd.util.text.literal
import moe.forpleuvoir.ibukigourd.util.text.maxWidth
import moe.forpleuvoir.ibukigourd.util.text.wrapToTextLines
import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.client.font.TextRenderer
import net.minecraft.text.Style
import kotlin.experimental.ExperimentalTypeInference


open class TextField(
    val text: () -> Text,
    val spacing: Float = SPACING.toFloat(),
    var shadow: Boolean = SHADOW,
    var layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    var rightToLeft: Boolean = RIGHT_TO_LEFT,
    var color: Color = Color(text().style.color?.rgb ?: COLOR.argb),
    var backgroundColor: Color = BACKGROUND_COLOR,
    val alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
    private val textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer
) : AbstractElement() {

    constructor(
        text: () -> String,
        style: Style = Style.EMPTY,
        spacing: Float = SPACING.toFloat(),
        shadow: Boolean = SHADOW,
        layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
        rightToLeft: Boolean = RIGHT_TO_LEFT,
        color: Color = Color(style.color?.rgb ?: COLOR.argb),
        backgroundColor: Color = BACKGROUND_COLOR,
        alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
        textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer
    ) : this({ literal(text()).style { style } }, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer)

    constructor(
        text: String,
        style: Style = Style.EMPTY,
        spacing: Float = SPACING.toFloat(),
        shadow: Boolean = SHADOW,
        layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
        rightToLeft: Boolean = RIGHT_TO_LEFT,
        color: Color = Color(style.color?.rgb ?: COLOR.argb),
        backgroundColor: Color = BACKGROUND_COLOR,
        alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
        textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer
    ) : this({ literal(text).style { style } }, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer)

    constructor(
        text: Text,
        spacing: Float = SPACING.toFloat(),
        shadow: Boolean = SHADOW,
        layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
        rightToLeft: Boolean = RIGHT_TO_LEFT,
        color: Color = Color(text.style.color?.rgb ?: COLOR.argb),
        backgroundColor: Color = BACKGROUND_COLOR,
        alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
        textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer
    ) : this({ text }, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer)

    var latestText: Pair<Int, Int> = textRenderer.getWidth(text()) to renderText.size

    var changed: Boolean = false

    protected val renderText: List<Text>
        get() {
            val text = text().wrapToTextLines(textRenderer, if (transform.fixedWidth) transform.width.toInt() else 0)
            if (latestText != textRenderer.getWidth(text()) to text.size) {
                changed = true
                latestText = textRenderer.getWidth(text()) to text.size
            }
            return text
        }

    override fun init() {
        resize()
    }

    fun resize() {
        var changed = false
        if (!transform.fixedWidth) {
            transform.width = renderText.maxWidth(textRenderer).toFloat() + padding.width
            changed = true
        }
        if (!transform.fixedHeight) {
            transform.height = renderText.size * (textRenderer.fontHeight + spacing) - spacing + padding.height
            changed = true
        }
        if (changed) parent()?.arrange()

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
        val list = buildList {
            renderText.forEachIndexed { index, text ->
                if (renderText.lastIndex != index) add(rect(vertex(0f, 0f, transform.z), textRenderer.getWidth(text), textRenderer.fontHeight + spacing))
                else add(rect(vertex(0f, 0f, transform.z), textRenderer.getWidth(text), textRenderer.fontHeight))
            }
        }
        alignment(Arrangement.Vertical).align(contentRect, list).forEachIndexed { index, vector3f ->
            textRenderer.renderText(renderContext.matrixStack, renderText[index], vector3f.x, vector3f.y, shadow, layerType, rightToLeft, color, backgroundColor)
        }
    }

}

@OverloadResolutionByLambdaReturnType
fun Element.textField(
    text: () -> String,
    style: Style = Style.EMPTY,
    spacing: Float = SPACING.toFloat(),
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    color: Color = Color(style.color?.rgb ?: COLOR.argb),
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
): TextField = addElement(TextField(text, style, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer))

fun Element.textField(
    text: String,
    style: Style = Style.EMPTY,
    spacing: Float = SPACING.toFloat(),
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    color: Color = Color(style.color?.rgb ?: COLOR.argb),
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
): TextField = addElement(TextField(text, style, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer))

@OverloadResolutionByLambdaReturnType
fun Element.textField(
    text: () -> Text,
    spacing: Float = SPACING.toFloat(),
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    color: Color = Color(text().style.color?.rgb ?: COLOR.argb),
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
): TextField = addElement(TextField(text, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer))

fun Element.textField(
    text: Text,
    spacing: Float = SPACING.toFloat(),
    shadow: Boolean = SHADOW,
    layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
    rightToLeft: Boolean = RIGHT_TO_LEFT,
    color: Color = Color(text.style.color?.rgb ?: COLOR.argb),
    backgroundColor: Color = BACKGROUND_COLOR,
    alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
    textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
): TextField = addElement(TextField(text, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer))