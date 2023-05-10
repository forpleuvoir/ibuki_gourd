@file:Suppress("FunctionName", "MemberVisibilityCanBePrivate")

package com.forpleuvoir.ibukigourd.gui.widget.button

import com.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import com.forpleuvoir.ibukigourd.gui.widget.ClickableElement
import com.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.COLOR
import com.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.HEIGHT
import com.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.PADDING
import com.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.TEXTURE
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.ibukigourd.render.popScissorOffset
import com.forpleuvoir.ibukigourd.render.pushScissorOffset
import com.forpleuvoir.ibukigourd.render.renderTexture
import com.forpleuvoir.ibukigourd.render.translate
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.nebula.common.color.ARGBColor
import net.minecraft.client.util.math.MatrixStack

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

	override fun onRender(matrixStack: MatrixStack, delta: Float) {
		if (!visible) return
		matrixStack.push()
		val offset = Vector3f(0f, status(height, 0f, 0f, height), 0f)
		matrixStack.translate(offset)
		pushScissorOffset(offset)
		renderBackground(matrixStack, delta)
		super.onRender(matrixStack, delta)
		renderOverlay(matrixStack, delta)
		popScissorOffset()
		matrixStack.pop()
	}

	override fun onRenderBackground(matrixStack: MatrixStack, delta: Float) {
		renderTexture(matrixStack, this.transform, status(theme.disabled, theme.idle, theme.hovered, theme.pressed), color())
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