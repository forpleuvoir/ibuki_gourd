@file:Suppress("FunctionName")

package com.forpleuvoir.ibukigourd.gui.widget.button

import com.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import com.forpleuvoir.ibukigourd.gui.widget.ClickableElement
import com.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.COLOR
import com.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.HEIGHT
import com.forpleuvoir.ibukigourd.mod.gui.Theme.BUTTON.PADDING
import com.forpleuvoir.ibukigourd.render.renderTexture
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.nebula.common.color.Color
import net.minecraft.client.util.math.MatrixStack
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.BUTTON_DISABLED_2 as DISABLED
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.BUTTON_HOVERED_2 as HOVERED
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.BUTTON_IDLE_2 as IDLE
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.BUTTON_PRESSED_2 as PRESSED

open class Button(
	var color: Color = COLOR,
	override var onClick: () -> NextAction = { NextAction.Continue },
	override var onRelease: () -> NextAction = { NextAction.Continue }
) : ClickableElement() {
	init {
		transform.width = 16f
		transform.height = 16f
		padding(PADDING)
	}

	override fun onRender(matrixStack: MatrixStack, delta: Float) {
		if (!visible) return
		val height = status(HEIGHT, 0.0, 0.0, HEIGHT)
		matrixStack.push()
		matrixStack.translate(0.0, height, 0.0)
		renderBackground(matrixStack, delta)
		super.onRender(matrixStack, delta)
		matrixStack.pop()
	}

	override fun onRenderBackground(matrixStack: MatrixStack, delta: Float) {
		renderTexture(matrixStack, this.transform, status(DISABLED, IDLE, HOVERED, PRESSED), color)
	}

}

fun ElementContainer.button(
	color: Color = COLOR,
	onClick: () -> NextAction = { NextAction.Continue },
	onRelease: () -> NextAction = { NextAction.Continue },
	scope: Button.() -> Unit = {}
): Button = addElement(Button(color, onClick, onRelease).apply(scope))
