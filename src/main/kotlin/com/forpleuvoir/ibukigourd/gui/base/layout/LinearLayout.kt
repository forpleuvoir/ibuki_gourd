package com.forpleuvoir.ibukigourd.gui.base.layout

import com.forpleuvoir.ibukigourd.gui.base.Margin
import com.forpleuvoir.ibukigourd.gui.base.element.Element
import com.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import com.forpleuvoir.ibukigourd.render.base.Alignment
import com.forpleuvoir.ibukigourd.render.base.Arrangement
import com.forpleuvoir.ibukigourd.render.base.Arrangement.Vertical
import com.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import com.forpleuvoir.ibukigourd.render.base.Size
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.ibukigourd.render.base.rectangle.rect
import com.forpleuvoir.ibukigourd.render.base.vertex.vertex

@Suppress("MemberVisibilityCanBePrivate")
class LinearLayout(
	override val elementContainer: () -> ElementContainer,
	val arrangement: Arrangement = Vertical,
	private val alignment: (Arrangement) -> Alignment = PlanarAlignment::Center
) : Layout {

	override fun arrange(elements: List<Element>, margin: Margin, padding: Margin): Size<Float>? {
		val alignment = alignment(arrangement)
		val alignElements = elements.filter { !it.fixed }
		if (alignElements.isEmpty()) return null

		val alignRects = alignElements.map {
			rect(
				vertex(0f, 0f, it.transform.z), it.transform.width + it.margin.width, it.transform.height + it.margin.height
			)
		}

		val container = elementContainer.invoke()
		val size = alignment.arrangement.contentSize(alignRects)
		val contentRect = when {
			//固定高度和宽度
			container.transform.fixedWidth && container.transform.fixedHeight -> {
				container.contentRect(false)
			}
			//固定宽度 不固定高度
			container.transform.fixedWidth && !container.transform.fixedHeight -> {
				rect(container.contentRect(false).position, container.transform.width, size.height)
			}
			//不固定宽度 固定高度
			!container.transform.fixedWidth && container.transform.fixedHeight -> {
				rect(container.contentRect(false).position, size.width, container.transform.height)
			}
			//不固定宽度 不固定高度
			else -> {
				rect(container.contentRect(false).position, size)
			}
		}
		alignment.align(contentRect, alignRects).forEachIndexed { index, vector3f ->
			val element = alignElements[index]
			element.transform.translateTo(vector3f + Vector3f(element.margin.left, element.margin.top))
		}
		return Size.create(contentRect.width + padding.width, contentRect.height + padding.height)
	}

}
