package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex

abstract class AbstractElementContainer : ElementContainer, Element {


	override var init: () -> Unit = ::init

	final override val transform = Transform()

	override var layout: Layout = LinearLayout({ this })
		set(value) {
			field = value
			arrange()
		}

	final override var margin: Margin = Margin()
		protected set

	final override var padding: Margin = Margin()
		protected set

	protected val elements = ArrayList<Element>()

	val arrangeElements: List<Element> get() = elements.filter { !it.fixed }

	override val elementTree: List<Element> get() = elements.filter { it != this.tip }

	override val renderTree get() = elementTree.sortedBy { it.renderPriority }

	override val handleTree get() = elementTree.sortedByDescending { it.priority }

	override fun init() {
		for (e in elements) e.init.invoke()
		arrange()
	}

	override fun arrange() {
		layout.arrange(this.elements, margin, padding)?.let {
			if (!transform.fixedWidth) {
				this.transform.width = it.width
				parent()?.arrange()
			}
			if (!transform.fixedHeight) {
				this.transform.height = it.height
				parent()?.arrange()
			}
		}
	}

	final override fun margin(margin: Number) {
		this.margin = Margin(margin, margin)
	}

	final override fun margin(margin: Margin) {
		this.margin = margin
	}

	final override fun margin(left: Number, right: Number, top: Number, bottom: Number) {
		this.margin = Margin(left, right, top, bottom)
	}

	final override fun padding(padding: Number) {
		this.padding = Margin(padding, padding)
	}

	final override fun padding(padding: Margin) {
		this.padding = padding
	}

	final override fun padding(left: Number, right: Number, top: Number, bottom: Number) {
		this.padding = Margin(left, right, top, bottom)
	}

	override fun contentRect(isWorld: Boolean): Rectangle<Vector3<Float>> {
		val top = if (isWorld) transform.worldTop + padding.top else padding.top
		val bottom = if (isWorld) transform.worldBottom - padding.bottom else transform.height - padding.bottom
		val left = if (isWorld) transform.worldLeft + padding.left else padding.left
		val right = if (isWorld) transform.worldRight - padding.right else transform.width - padding.right
		return rect(
			vertex(left, top, if (isWorld) transform.worldZ else transform.z), right - left, bottom - top
		)
	}

	override fun <T : Element> addElement(element: T): T {
		if (elements.contains(element)) return element
		elements.add(element)
		element.transform.parent = { this.transform }
		element.parent = { this }
		return element
	}

	override fun preElement(element: Element): Element? {
		val indexOf = elements.indexOf(element)
		if (indexOf < 1) return null
		return elements[indexOf - 1]
	}

	override fun nextElement(element: Element): Element? {
		val indexOf = elements.indexOf(element)
		if (indexOf != -1 && indexOf < elements.size - 1) return null
		return elements[indexOf + 1]
	}

	override fun elementIndexOf(element: Element): Int = elements.indexOf(element)
	override fun removeElement(element: Element): Boolean {
		element.transform.parent = { null }
		element.parent = { null }
		return elements.remove(element)
	}

	override fun removeElement(index: Int) {
		elements.removeAt(index).apply {
			transform.parent = { null }
			parent = { null }
		}
	}
}