package com.forpleuvoir.ibukigourd.gui.base

import com.forpleuvoir.ibukigourd.api.Tickable
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
	val displayDelay: Int = 12,
	/**
	 * 提示文本
	 */
	vararg text: Text
) : Drawable, Tickable {

	val transform: Transform = Transform(parent = parent)

	val hoverTexts: List<Text> = arrayListOf(*text)

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

	override val render: (matrixStack: MatrixStack, delta: Double) -> Unit = ::onRender

	override fun onRender(matrixStack: MatrixStack, delta: Double) {
		TODO("Not yet implemented")
	}
}