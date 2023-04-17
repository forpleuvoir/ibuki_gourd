package com.forpleuvoir.ibukigourd.render

import net.minecraft.client.util.math.MatrixStack

interface Drawable {

	/**
	 * 可见
	 */
	val visible: Boolean get() = true

	/**
	 * Z轴偏移 越低越先渲染
	 */
	val renderPriority: Int get() = 0

	/**
	 * 渲染
	 */
	val render: (matrixStack: MatrixStack, delta: Float) -> Unit

	/**
	 * 渲染
	 * @param matrixStack MatrixStack
	 * @param delta Float
	 */
	fun onRender(matrixStack: MatrixStack, delta: Float)

}