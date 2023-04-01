@file:Suppress("FunctionName")

package com.forpleuvoir.ibukigourd.gui.widget.button

import com.forpleuvoir.ibukigourd.gui.widget.ClickableElement
import com.forpleuvoir.ibukigourd.render.renderTexture
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.nebula.common.color.Color
import com.forpleuvoir.nebula.common.color.Colors
import net.minecraft.client.util.math.MatrixStack
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.BUTTON_HOVERED_2 as HOVERED
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.BUTTON_IDLE_2 as IDLE
import com.forpleuvoir.ibukigourd.gui.texture.IbukiGourdTextures.BUTTON_PRESSED_2 as PRESSED

abstract class Button(
	var color: Color = Colors.WHITE,
	override var onClick: () -> NextAction = { NextAction.Continue },
	override var onRelease: () -> NextAction = { NextAction.Continue }
) : ClickableElement() {

	override fun onRender(matrixStack: MatrixStack, delta: Double) {
		if (!visible) return
		renderTexture(matrixStack, this.transform, status(IDLE, HOVERED, PRESSED), color)
		super.onRender(matrixStack, delta)
	}

}