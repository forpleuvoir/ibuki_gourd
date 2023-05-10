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

@Suppress("MemberVisibilityCanBePrivate")
class LinearLayout(
	override val elementContainer: () -> ElementContainer,
	private val alignment: Alignment = PlanarAlignment.Center(Vertical)
) : Layout {

	constructor(
		elementContainer: () -> ElementContainer,
		arrangement: Arrangement = Vertical,
		alignment: (Arrangement) -> Alignment = PlanarAlignment::Center
	) : this(elementContainer, alignment(arrangement))

	override fun arrange(elements: List<Element>, margin: Margin, padding: Margin): Size<Float>? {
		val alignElements = elements.filter { !it.fixed }
		if (alignElements.isEmpty()) return null

		val alignRects = alignRects(alignElements)

		val container = elementContainer()
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
