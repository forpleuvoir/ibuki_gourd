@file:Suppress("FunctionName", "MemberVisibilityCanBePrivate")

package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.widget.ClickableElement
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.COLOR
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.HEIGHT
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.PADDING
import moe.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.TEXTURE
import moe.forpleuvoir.ibukigourd.render.RenderContext
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.renderTexture
import moe.forpleuvoir.ibukigourd.render.translate
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.nebula.common.color.ARGBColor

open class Button(
	var color: () -> ARGBColor = { COLOR },
	override var onClick: () -> NextAction = { NextAction.Continue },
	override var onRelease: () -> NextAction = { NextAction.Continue },
	val height: Float = HEIGHT.toFloat(),
	var theme: ButtonTheme = TEXTURE
) : ClickableElement() {
	init {
		transform.width = 16f
		transform.height = 16f
		padding(PADDING)
	}

	override fun onRender(renderContext: RenderContext) {
		if (!visible) return
		renderContext.matrixStack {
			val offset = Vector3f(0f, status(height, 0f, 0f, height), 0f)
			matrixStack.translate(offset)
			scissorOffset(offset) {
				renderBackground(this)
				super.onRender(this)
				renderOverlay(this)
			}
		}
	}

	override fun onRenderBackground(renderContext: RenderContext) {
		renderTexture(renderContext.matrixStack, this.transform, status(theme.disabled, theme.idle, theme.hovered, theme.pressed), color())
	}

}

fun ElementContainer.button(
	color: () -> ARGBColor = { COLOR },
	onClick: () -> NextAction = { NextAction.Continue },
	onRelease: () -> NextAction = { NextAction.Continue },
	height: Float = HEIGHT.toFloat(),
	theme: ButtonTheme = TEXTURE,
	scope: Button.() -> Unit = {}
): Button = addElement(Button(color, onClick, onRelease, height, theme).apply(scope))
