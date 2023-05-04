@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.forpleuvoir.ibukigourd.gui.widget.text

import com.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import com.forpleuvoir.ibukigourd.gui.base.element.Element
import com.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.BACKGROUND_COLOR
import com.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.COLOR
import com.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.RIGHT_TO_LEFT
import com.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.SHADOW
import com.forpleuvoir.ibukigourd.mod.gui.Theme.TEXT.SPACING
import com.forpleuvoir.ibukigourd.render.base.Alignment
import com.forpleuvoir.ibukigourd.render.base.Arrangement
import com.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import com.forpleuvoir.ibukigourd.render.base.rectangle.rect
import com.forpleuvoir.ibukigourd.render.base.vertex.vertex
import com.forpleuvoir.ibukigourd.render.disableScissor
import com.forpleuvoir.ibukigourd.render.enableScissor
import com.forpleuvoir.ibukigourd.render.renderText
import com.forpleuvoir.ibukigourd.util.text.Text
import com.forpleuvoir.ibukigourd.util.text.literal
import com.forpleuvoir.ibukigourd.util.text.maxWidth
import com.forpleuvoir.ibukigourd.util.text.wrapToTextLines
import com.forpleuvoir.nebula.common.color.Color
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Style
import java.util.function.Supplier


open class TextField(
	val text: () -> Text,
	val spacing: Float = SPACING.toFloat(),
	var shadow: Boolean = SHADOW,
	var layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
	var rightToLeft: Boolean = RIGHT_TO_LEFT,
	var color: Color = Color(text().style.color?.rgb ?: COLOR.argb),
	var backgroundColor: Color = BACKGROUND_COLOR,
	val alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
	private val textRenderer: TextRenderer = com.forpleuvoir.ibukigourd.util.textRenderer
) : AbstractElement() {

	constructor(
		text: Supplier<String>,
		style: Style = Style.EMPTY,
		spacing: Float = SPACING.toFloat(),
		shadow: Boolean = SHADOW,
		layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
		rightToLeft: Boolean = RIGHT_TO_LEFT,
		color: Color = Color(style.color?.rgb ?: COLOR.argb),
		backgroundColor: Color = BACKGROUND_COLOR,
		alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
		textRenderer: TextRenderer = com.forpleuvoir.ibukigourd.util.textRenderer
	) : this({ literal(text.get()).style { style } }, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer)

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
		textRenderer: TextRenderer = com.forpleuvoir.ibukigourd.util.textRenderer
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
		textRenderer: TextRenderer = com.forpleuvoir.ibukigourd.util.textRenderer
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
		if (!transform.fixedWidth) {
			transform.width = renderText.maxWidth(textRenderer).toFloat() + padding.width
			parent()?.arrange()
		}
		if (!transform.fixedHeight) {
			transform.height = renderText.size * (textRenderer.fontHeight + spacing) - spacing + padding.height
			parent()?.arrange()
		}

	}

	override fun onRender(matrixStack: MatrixStack, delta: Float) {
		if (changed) {
			resize()
			changed = false
		}
		enableScissor(transform, matrixStack.peek().positionMatrix)
		renderBackground(matrixStack, delta)
		renderText(matrixStack, delta)
		renderOverlay(matrixStack, delta)
		disableScissor()
	}

	@Suppress("UNUSED_PARAMETER")
	protected fun renderText(matrixStack: MatrixStack, delta: Float) {
		val contentRect = contentRect(true)
		val list = buildList {
			renderText.forEachIndexed { index, text ->
				if (renderText.lastIndex != index)
					add(rect(vertex(0f, 0f, transform.z), textRenderer.getWidth(text), textRenderer.fontHeight + spacing))
				else add(rect(vertex(0f, 0f, transform.z), textRenderer.getWidth(text), textRenderer.fontHeight))
			}
		}
		alignment(Arrangement.Vertical).align(contentRect, list).forEachIndexed { index, vector3f ->
			textRenderer.renderText(matrixStack, renderText[index], vector3f.x, vector3f.y, shadow, layerType, rightToLeft, color, backgroundColor)
		}
	}

}

inline fun Element.textField(
	text: Supplier<String>,
	style: Style = Style.EMPTY,
	spacing: Float = SPACING.toFloat(),
	shadow: Boolean = SHADOW,
	layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
	rightToLeft: Boolean = RIGHT_TO_LEFT,
	color: Color = Color(style.color?.rgb ?: COLOR.argb),
	backgroundColor: Color = BACKGROUND_COLOR,
	noinline alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
	textRenderer: TextRenderer = com.forpleuvoir.ibukigourd.util.textRenderer,
	scope: TextField.() -> Unit = {}
): TextField = addElement(TextField(text, style, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer).apply(scope))

inline fun Element.textField(
	text: String,
	style: Style = Style.EMPTY,
	spacing: Float = SPACING.toFloat(),
	shadow: Boolean = SHADOW,
	layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
	rightToLeft: Boolean = RIGHT_TO_LEFT,
	color: Color = Color(style.color?.rgb ?: COLOR.argb),
	backgroundColor: Color = BACKGROUND_COLOR,
	noinline alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
	textRenderer: TextRenderer = com.forpleuvoir.ibukigourd.util.textRenderer,
	scope: TextField.() -> Unit = {}
): TextField = addElement(TextField(text, style, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer).apply(scope))

inline fun Element.textField(
	noinline text: () -> Text,
	spacing: Float = SPACING.toFloat(),
	shadow: Boolean = SHADOW,
	layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
	rightToLeft: Boolean = RIGHT_TO_LEFT,
	color: Color = Color(text().style.color?.rgb ?: COLOR.argb),
	backgroundColor: Color = BACKGROUND_COLOR,
	noinline alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
	textRenderer: TextRenderer = com.forpleuvoir.ibukigourd.util.textRenderer,
	scope: TextField.() -> Unit = {}
): TextField = addElement(TextField(text, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer).apply(scope))

inline fun Element.textField(
	text: Text,
	spacing: Float = SPACING.toFloat(),
	shadow: Boolean = SHADOW,
	layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
	rightToLeft: Boolean = RIGHT_TO_LEFT,
	color: Color = Color(text.style.color?.rgb ?: COLOR.argb),
	backgroundColor: Color = BACKGROUND_COLOR,
	noinline alignment: (Arrangement) -> Alignment = PlanarAlignment::CenterLeft,
	textRenderer: TextRenderer = com.forpleuvoir.ibukigourd.util.textRenderer,
	scope: TextField.() -> Unit = {}
): TextField = addElement(TextField(text, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer).apply(scope))