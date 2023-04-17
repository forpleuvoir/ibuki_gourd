package com.forpleuvoir.ibukigourd.gui.base.layout

import com.forpleuvoir.ibukigourd.gui.base.Element
import com.forpleuvoir.ibukigourd.gui.base.Margin
import com.forpleuvoir.ibukigourd.gui.base.Padding
import com.forpleuvoir.ibukigourd.render.base.Alignment
import com.forpleuvoir.ibukigourd.render.base.HorizontalAlignment
import com.forpleuvoir.ibukigourd.render.base.Rectangle
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.ibukigourd.render.base.vertex.vertex

class LinearLayout(
	override val element: Element,
	private val alignment: Alignment = HorizontalAlignment.Center
) : Layout {

	override fun arrange(elements: List<Element>, margin: Margin, padding: Padding): Layout.Size? {
		val alignElements = elements.filter { !it.fixed }
		if (alignElements.isEmpty()) return null
		val alignRects = alignElements.map {
			Rectangle(
				vertex(Vector3f(0f, 0f, it.transform.z)),
				it.transform.width + it.margin.width,
				it.transform.height + it.margin.height
			)
		}
		val contentRect = element.contentRect(false)
		alignment.align(contentRect, alignRects).forEachIndexed { index, vector3f ->
			val element = alignElements[index]
			vector3f += Vector3f(element.margin.left, element.margin.top)
			element.transform.moveTo(vector3f)
		}
		return Layout.Size(contentRect.width + padding.width, contentRect.height + padding.height)
	}

}