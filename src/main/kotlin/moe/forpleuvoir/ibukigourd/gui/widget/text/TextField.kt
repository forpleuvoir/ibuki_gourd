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
import moe.forpleuvoir.ibukigourd.render.helper.batchRender
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
	val spacing: Float = SPACING,
	var shadow: Boolean = SHADOW,
	var layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
	var rightToLeft: Boolean = RIGHT_TO_LEFT,
	var color: Color = Color(text().style.color?.rgb ?: COLOR.argb),
	var backgroundColor: Color = BACKGROUND_COLOR,
	val alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
	private val textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
	width: Float? = null,
	height: Float? = null,
) : AbstractElement() {

	init {
		transform.width = width?.also { transform.fixedWidth = true } ?: 16f
		transform.height = height?.also { transform.fixedHeight = true } ?: 16f
	}

	/**
	 * 最新的文本
	 */
	protected var latestText: Text = text()

	var changed: Boolean = false

	protected val renderText: List<Text>
		get() {
			val text = text().wrapToTextLines(textRenderer, if (transform.fixedWidth) transform.width.toInt() else 0)
			if (latestText != text()) {
				changed = true
				latestText = text()
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
				if (renderText.lastIndex != index) add(rect(vertex(0f, 0f, transform.z), textRenderer.getWidth(text), textRenderer.fontHeight + spacing))
				else add(rect(vertex(0f, 0f, transform.z), textRenderer.getWidth(text), textRenderer.fontHeight))
			}
		}
		textRenderer.batchRender {
			alignment(Arrangement.Vertical).align(contentRect, list).forEachIndexed { index, vector3f ->
				renderText(renderContext.matrixStack,renderText[index], vector3f.x, vector3f.y, vector3f.z, shadow, layerType, rightToLeft, color, backgroundColor)
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
fun Element.textField(
	text: String,
	style: Style = Style.EMPTY,
	spacing: Float = SPACING,
	shadow: Boolean = SHADOW,
	layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
	rightToLeft: Boolean = RIGHT_TO_LEFT,
	color: Color = Color(style.color?.rgb ?: COLOR.argb),
	backgroundColor: Color = BACKGROUND_COLOR,
	alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
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
fun Element.textField(
	text: () -> String,
	style: Style = Style.EMPTY,
	spacing: Float = SPACING,
	shadow: Boolean = SHADOW,
	layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
	rightToLeft: Boolean = RIGHT_TO_LEFT,
	color: Color = Color(style.color?.rgb ?: COLOR.argb),
	backgroundColor: Color = BACKGROUND_COLOR,
	alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
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
fun Element.textField(
	text: () -> Text,
	spacing: Float = SPACING,
	shadow: Boolean = SHADOW,
	layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
	rightToLeft: Boolean = RIGHT_TO_LEFT,
	color: Color = Color(text().style.color?.rgb ?: COLOR.argb),
	backgroundColor: Color = BACKGROUND_COLOR,
	alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
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
fun Element.textField(
	text: Text,
	spacing: Float = SPACING,
	shadow: Boolean = SHADOW,
	layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
	rightToLeft: Boolean = RIGHT_TO_LEFT,
	color: Color = Color(text.style.color?.rgb ?: COLOR.argb),
	backgroundColor: Color = BACKGROUND_COLOR,
	alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
	textRenderer: TextRenderer = moe.forpleuvoir.ibukigourd.util.textRenderer,
	width: Float? = null,
	height: Float? = null,
): TextField = addElement(TextField({ text }, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer, width, height))