package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex

interface Layout {

	val elementContainer: () -> ElementContainer

	/**
	 * 排列子元素
	 * @param elements List<Element>
	 * @param margin Margin
	 * @param padding Padding
	 * @return [Size] 排列完元素之后计算出的高度和宽度,如果为空则没有任何元素参与排列
	 */
	fun arrange(elements: List<Element>, margin: Margin, padding: Margin): Size<Float>?

	fun alignRects(elements: List<Element>): List<Rectangle<Vector3<Float>>> {
		val alignElements = elements.filter { !it.fixed }
		return alignElements.map {
			rect(
				vertex(0f, 0f, it.transform.z), it.transform.width + it.margin.width, it.transform.height + it.margin.height
			)
		}
	}

}