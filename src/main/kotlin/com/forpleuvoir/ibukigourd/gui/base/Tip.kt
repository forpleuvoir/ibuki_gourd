package com.forpleuvoir.ibukigourd.gui.base

import com.forpleuvoir.ibukigourd.api.Tickable
import com.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import com.forpleuvoir.ibukigourd.gui.base.element.Element
import com.forpleuvoir.ibukigourd.mod.gui.Theme.TIP.DELAY
import com.forpleuvoir.ibukigourd.mod.gui.Theme.TIP.PADDING
import com.forpleuvoir.ibukigourd.render.Drawable
import com.forpleuvoir.ibukigourd.util.text.Text
import net.minecraft.client.util.math.MatrixStack

open class Tip(
	/**
	 * 父对象的变换属性
	 */
	parent: Transform,
	/**
	 * 延迟显示时间
	 */
	val displayDelay: Int = DELAY,
	padding: Margin = PADDING,
) : AbstractElement(), Drawable, Tickable {

	init {
		transform.parent = parent
		padding(padding)
	}


	private var tickCounter: Int = 0
		set(value) {
			field = value.coerceAtMost(tickCounter)
		}

	override fun tick() {
		if (transform.parent!!.mouseHover()) {
			tickCounter++
		} else {
			tickCounter = 0
		}
	}

	override var render: (matrixStack: MatrixStack, delta: Float) -> Unit = ::onRender

	override fun onRender(matrixStack: MatrixStack, delta: Float) {
		TODO("Not yet implemented")

	}

}

fun Element.tip(displayDelay: Int = DELAY, vararg text: Text, scope: Tip.() -> Unit): Tip {
	this.tip = Tip(this.transform, displayDelay).apply(scope)
	return this.tip!!
}