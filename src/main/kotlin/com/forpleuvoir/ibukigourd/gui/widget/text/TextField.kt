@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.forpleuvoir.ibukigourd.gui.widget.text

import com.forpleuvoir.ibukigourd.gui.base.AbstractElement
import com.forpleuvoir.ibukigourd.gui.base.Element
import com.forpleuvoir.ibukigourd.render.base.Alignment
import com.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import com.forpleuvoir.ibukigourd.render.base.Rectangle
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
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


open class TextField(
	val text: Text,
	val spacing: Float = 1f,
	var shadow: Boolean = true,
	var layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
	var rightToLeft: Boolean = false,
	var color: Color = Color(text.style.color?.rgb ?: 0xFFFFFF),
	var backgroundColor: Color = Color(0),
	val alignment: Alignment = PlanarAlignment.CenterLeft,
	private val textRenderer: TextRenderer = com.forpleuvoir.ibukigourd.util.textRenderer
) : AbstractElement() {

	constructor(
		text: String,
		style: Style = Style.EMPTY,
		spacing: Float = 1f,
		shadow: Boolean = true,
		layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
		rightToLeft: Boolean = false,
		color: Color = Color(style.color?.rgb ?: 0xFFFFFF),
		backgroundColor: Color = Color(0),
		alignment: Alignment = PlanarAlignment.CenterLeft,
		textRenderer: TextRenderer = com.forpleuvoir.ibukigourd.util.textRenderer
	) : this(literal(text).style { style }, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer)

	protected val renderText: List<Text> get() = text.wrapToTextLines(textRenderer, transform.width.toInt())

	override fun init() {
		if (transform.width == 0.0f) {
			transform.width = renderText.maxWidth(textRenderer).toFloat() + padding.width
		}
		if (transform.height == 0.0f) {
			transform.height = renderText.size * (textRenderer.fontHeight + spacing) - spacing + padding.height
		}
	}

	override fun onRender(matrixStack: MatrixStack, delta: Float) {
		enableScissor(transform)
		renderText(matrixStack, delta)
		disableScissor()
	}

	@Suppress("UNUSED_PARAMETER")
	protected fun renderText(matrixStack: MatrixStack, delta: Float) {
		val contentRect = contentRect(true)
		val list = buildList {
			renderText.forEachIndexed { index, text ->
				if (renderText.lastIndex != index)
					add(Rectangle(vertex(Vector3f(0f, 0f, transform.z)), textRenderer.getWidth(text), textRenderer.fontHeight + spacing))
				else add(Rectangle(vertex(Vector3f(0f, 0f, transform.z)), textRenderer.getWidth(text), textRenderer.fontHeight))
			}
		}
		alignment.align(contentRect, list).forEachIndexed { index, vector3f ->
			textRenderer.renderText(matrixStack, renderText[index], vector3f.x, vector3f.y, shadow, layerType, rightToLeft, color, backgroundColor)
		}
	}

}

inline fun Element.textField(
	text: String,
	style: Style = Style.EMPTY,
	spacing: Float = 1f,
	shadow: Boolean = true,
	layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
	rightToLeft: Boolean = false,
	color: Color = Color(style.color?.rgb ?: 0xFFFFFF),
	backgroundColor: Color = Color(0),
	alignment: Alignment = PlanarAlignment.CenterLeft,
	textRenderer: TextRenderer = com.forpleuvoir.ibukigourd.util.textRenderer,
	scope: TextField.() -> Unit = {}
): TextField = addElement(TextField(text, style, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer).apply(scope))

inline fun Element.textField(
	text: Text,
	spacing: Float = 1f,
	shadow: Boolean = true,
	layerType: TextRenderer.TextLayerType = TextRenderer.TextLayerType.NORMAL,
	rightToLeft: Boolean = false,
	color: Color = Color(text.style.color?.rgb ?: 0xFFFFFF),
	backgroundColor: Color = Color(0),
	alignment: Alignment = PlanarAlignment.CenterLeft,
	textRenderer: TextRenderer = com.forpleuvoir.ibukigourd.util.textRenderer,
	scope: TextField.() -> Unit = {}
): TextField = addElement(TextField(text, spacing, shadow, layerType, rightToLeft, color, backgroundColor, alignment, textRenderer).apply(scope))