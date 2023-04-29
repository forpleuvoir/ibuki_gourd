@file:Suppress("MemberVisibilityCanBePrivate")

package com.forpleuvoir.ibukigourd.gui.screen

import com.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import com.forpleuvoir.ibukigourd.gui.base.element.Element
import com.forpleuvoir.ibukigourd.gui.tip.Tip
import com.forpleuvoir.ibukigourd.input.KeyCode
import com.forpleuvoir.ibukigourd.input.Keyboard
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.util.math.MatrixStack

abstract class AbstractScreen(
	width: Int = mc.window.scaledWidth,
	height: Int = mc.window.scaledHeight
) : AbstractElement(), Screen {

	override val screen: Screen get() = this

	override var parent: Element
		get() = this
		set(@Suppress("UNUSED_PARAMETER") value) {}

	init {
		this.transform.fixedWidth = true
		this.transform.fixedHeight = true
		this.transform.width = width.toFloat()
		this.transform.height = height.toFloat()
	}

	override val tipList: MutableSet<Tip> = HashSet()

	override var parentScreen: Screen? = null

	override var pauseGame: Boolean = false

	override var shouldCloseOnEsc: Boolean = true

	override var close: () -> Unit = ::onClose

	override fun onRender(matrixStack: MatrixStack, delta: Float) {
		if (!visible) return
		renderBackground.invoke(matrixStack, delta)
		for (element in renderTree) element.render(matrixStack, delta)
		tipList.sortedBy { it.transform.worldZ }.forEach {
			if (it.visible) it.render.invoke(matrixStack, delta)
		}
		renderOverlay.invoke(matrixStack, delta)
	}

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