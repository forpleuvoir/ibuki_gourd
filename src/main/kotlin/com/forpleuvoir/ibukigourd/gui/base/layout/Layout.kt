package com.forpleuvoir.ibukigourd.gui.base.layout

import com.forpleuvoir.ibukigourd.gui.base.Margin
import com.forpleuvoir.ibukigourd.gui.base.element.Element
import com.forpleuvoir.ibukigourd.gui.base.element.ElementContainer

interface Layout {

	val elementContainer: () -> ElementContainer

	/**
	 * 排列子元素
	 * @param elements List<Element>
	 * @param margin Margin
	 * @param padding Padding
	 * @return [Size] 排列完元素之后计算出的高度和宽度,如果为空则没有任何元素参与排列
	 */
	fun arrange(elements: List<Element>, margin: Margin, padding: Margin): Size?

	data class Size(val width: Float, val height: Float)

}