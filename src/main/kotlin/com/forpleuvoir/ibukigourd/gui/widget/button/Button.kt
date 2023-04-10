@file:Suppress("FunctionName")

package com.forpleuvoir.ibukigourd.gui.widget.button

import com.forpleuvoir.ibukigourd.gui.base.Element
import com.forpleuvoir.ibukigourd.gui.widget.ClickableElement
import com.forpleuvoir.ibukigourd.render.renderTexture
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.nebula.common.color.Color
import com.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.util.math.MatrixStack
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.BUTTON_DISABLED_2 as DISABLED
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.BUTTON_HOVERED_2 as HOVERED
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.BUTTON_IDLE_2 as IDLE
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.BUTTON_PRESSED_2 as PRESSED

open class Button(
	var color: Color = Colors.WHITE,
	override var onClick: () -> NextAction = { NextAction.Continue },
	override var onRelease: () -> NextAction = { NextAction.Continue }
) : ClickableElement() {

	override fun onRender(matrixStack: MatrixStack, delta: Double) {
		if (!visible) return
		renderBackground(matrixStack, delta)
		super.onRender(matrixStack, delta)
	}

	var renderBackground = { matrixStack: MatrixStack, delta: Double ->
		renderTexture(matrixStack, this.transform, status(DISABLED, IDLE, HOVERED, PRESSED), color)
	}

}

fun Element.button(
	color: Color = Colors.WHITE,
	onClick: () -> NextAction = { NextAction.Continue },
	onRelease: () -> NextAction = { NextAction.Continue },
	scope: Element.() -> Unit
): Button = addElement(Button(color, onClick, onRelease).apply(scope))
