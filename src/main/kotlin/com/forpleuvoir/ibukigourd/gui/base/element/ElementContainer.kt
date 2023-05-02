package com.forpleuvoir.ibukigourd.gui.base.element

import com.forpleuvoir.ibukigourd.gui.base.Margin
import com.forpleuvoir.ibukigourd.gui.base.Transform
import com.forpleuvoir.ibukigourd.gui.base.layout.Layout
import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import com.forpleuvoir.nebula.common.api.Initializable

interface ElementContainer : Initializable {

	var init: () -> Unit

	/**
	 * 基础属性变换
	 */
	val transform: Transform

	fun arrange()

	/**
	 * 子元素
	 */
	val elementTree: List<Element>

	val renderTree: List<Element>

	val handleTree: List<Element>

	fun <T : Element> addElement(element: T): T

	fun preElement(element: Element): Element?

	fun nextElement(element: Element): Element?

	fun elementIndexOf(element: Element): Int

	fun removeElement(element: Element): Boolean

	fun removeElement(index: Int)

	val margin: Margin

	val padding: Margin

	fun margin(margin: Number)

	fun margin(margin: Margin)

	fun margin(left: Number = this.margin.left, right: Number = this.margin.right, top: Number = this.margin.top, bottom: Number = this.margin.bottom)

	fun padding(padding: Number)

	fun padding(padding: Margin)

	fun padding(left: Number = this.padding.left, right: Number = this.padding.right, top: Number = this.padding.top, bottom: Number = this.padding.bottom)

	var layout: Layout

	/**
	 * 内容矩形
	 * @param isWorld Boolean
	 * @return Rectangle
	 */
	fun contentRect(isWorld: Boolean): Rectangle<Vector3<Float>>

}