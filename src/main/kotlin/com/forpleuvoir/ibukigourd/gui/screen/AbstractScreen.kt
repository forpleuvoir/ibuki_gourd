package com.forpleuvoir.ibukigourd.gui.screen

import com.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import com.forpleuvoir.ibukigourd.input.KeyCode
import com.forpleuvoir.ibukigourd.input.Keyboard
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.util.math.MatrixStack

abstract class AbstractScreen(
	width: Int = mc.window.scaledWidth,
	height: Int = mc.window.scaledHeight
) : AbstractElement(), Screen {

	init {
		this.transform.fixedSize = true
		this.transform.width = width.toFloat()
		this.transform.height = height.toFloat()
	}


	override var parentScreen: Screen? = null

	override var pauseGame: Boolean = false

	override var shouldCloseOnEsc: Boolean = true

	override var close: () -> Unit = ::onClose

	override fun onRender(matrixStack: MatrixStack, delta: Float) {
		renderBackground.invoke(matrixStack, delta)
		super.onRender(matrixStack, delta)
		renderOverlay.invoke(matrixStack, delta)
	}

	abstract var renderBackground: (matrixStack: MatrixStack, delta: Float) -> Unit

	abstract var renderOverlay: (matrixStack: MatrixStack, delta: Float) -> Unit

	override fun onClose() {
		ScreenManager.setScreen(parentScreen)
	}

	override fun onResize(width: Int, height: Int) {
		this.transform.width = width.toFloat()
		this.transform.height = height.toFloat()
		layout.arrange(this.elements, this.margin, this.padding)
	}

	override var resize: (width: Int, height: Int) -> Unit = ::onResize

	override fun onKeyPress(keyCode: KeyCode): NextAction {
		if (keyCode == Keyboard.ESCAPE && shouldCloseOnEsc) {
			close()
			return NextAction.Continue
		}
		return NextAction.Cancel
	}


}