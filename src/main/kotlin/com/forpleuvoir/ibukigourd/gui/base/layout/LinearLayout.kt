package com.forpleuvoir.ibukigourd.gui.base.layout

import com.forpleuvoir.ibukigourd.gui.base.Margin
import com.forpleuvoir.ibukigourd.gui.base.element.Element
import com.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import com.forpleuvoir.ibukigourd.render.base.*
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.ibukigourd.render.base.vertex.vertex

class LinearLayout(override val elementContainer: () -> ElementContainer, private val alignment: Alignment = HorizontalAlignment.Center) : Layout {

	override fun arrange(elements: List<Element>, margin: Margin, padding: Margin): Layout.Size? {
		val alignElements = elements.filter { !it.fixed }
		if (alignElements.isEmpty()) return null

		val alignRects = alignElements.map {
			Rectangle(
				vertex(Vector3f(0f, 0f, it.transform.z)), it.transform.width + it.margin.width, it.transform.height + it.margin.height
			)
		}
		val container = elementContainer.invoke()
		val contentRect = if (container.transform.fixedSize) {
			container.contentRect(false)
		} else {
			if (alignment.vertical) {
				var height = 0f
				alignRects.forEach { height += it.height }
				Rectangle(container.contentRect(false).position, alignRects.maxWidth, height)
			} else {
				var width = 0f
				alignRects.forEach { width += it.width }
				Rectangle(container.contentRect(false).position, width, alignRects.maxHeight)
			}
		}

		alignment.align(contentRect, alignRects).forEachIndexed { index, vector3f ->
			val element = alignElements[index]
			vector3f += Vector3f(element.margin.left, element.margin.top)
			element.transform.moveTo(vector3f)
		}
		return Layout.Size(contentRect.width + padding.width, contentRect.height + padding.height)
	}

}