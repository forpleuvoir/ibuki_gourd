package com.forpleuvoir.ibukigourd.gui.base

import com.forpleuvoir.ibukigourd.gui.base.layout.Layout
import com.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import com.forpleuvoir.ibukigourd.input.KeyCode
import com.forpleuvoir.ibukigourd.input.Mouse
import com.forpleuvoir.ibukigourd.render.base.Rectangle
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.ibukigourd.render.base.vertex.vertex
import com.forpleuvoir.ibukigourd.util.NextAction
import net.minecraft.client.util.math.MatrixStack

@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractElement : Element {
	override fun init() {
		for (e in elements) e.init.invoke()
		layout.arrange(this.elements, margin, padding)?.let {
			this.transform.width = it.width
			this.transform.height = it.height
		}
	}

	override var init: () -> Unit = ::init

	final override val transform = Transform()

	override var active = true
		set(value) {
			if (!value) elementTree.asSequence().filter { it.active }.forEach { it.active = false }
			field = value
		}

	override var fixed: Boolean = false

	override val layout: Layout
		get() = LinearLayout(this)

	override var margin: Margin = Margin()
		protected set

	override var padding: Padding = Padding()
		protected set

	override fun margin(margin: Number) {
		this.margin = Margin(margin, margin)
	}

	override fun margin(margin: Margin) {
		this.margin = margin
	}

	override fun margin(left: Number, right: Number, top: Number, bottom: Number) {
		this.margin = Margin(left, right, top, bottom)
	}

	override fun padding(padding: Number) {
		this.padding = Padding(padding, padding)
	}

	override fun padding(padding: Padding) {
		this.padding = padding
	}

	override fun padding(left: Number, right: Number, top: Number, bottom: Number) {
		this.padding = Padding(left, right, top, bottom)
	}

	override fun contentRect(isWorld: Boolean): Rectangle {
		val top = if (isWorld) transform.worldTop + padding.top else padding.top
		val bottom = if (isWorld) transform.worldBottom + padding.bottom else transform.height + padding.bottom
		val left = if (isWorld) transform.worldLeft + padding.left else padding.left
		val right = if (isWorld) transform.worldRight + padding.right else transform.width + padding.right
		return Rectangle(
			vertex(Vector3f(top, left, transform.z)), right - left, bottom - top
		)
	}

	protected val elements = ArrayList<Element>()

	override val elementTree: List<Element> get() = elements

	override val renderTree get() = elementTree.sortedBy { it.renderPriority }

	override val handleTree get() = elementTree.sortedByDescending { it.priority }


	override fun <T : Element> addElement(element: T): T {
		elements.add(element)
		element.transform.parent = this.transform
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
		element.transform.parent = null
		return elements.remove(element)
	}

	override fun removeElement(index: Int) {
		elements.removeAt(index).transform.parent = null
	}

	override fun tick() {
		if (!active) return
		for (element in handleTree) element.tick.invoke()
	}

	override var tick: () -> Unit = ::tick

	override fun onRender(matrixStack: MatrixStack, delta: Float) {
		if (!visible) return
		for (element in renderTree) element.render(matrixStack, delta)
	}

	override var render: (matrixStack: MatrixStack, delta: Float) -> Unit = ::onRender

	override fun onMouseMove(mouseX: Number, mouseY: Number) {
		if (!active) return
		for (element in handleTree) element.mouseMove(mouseX, mouseY)
	}

	override var mouseMove: (mouseX: Number, mouseY: Number) -> Unit = ::onMouseMove

	override fun onMouseClick(mouseX: Number, mouseY: Number, button: Mouse): NextAction {
		if (!active) return NextAction.Continue
		for (element in handleTree) {
			if (element.mouseClick(mouseX, mouseY, button) == NextAction.Cancel)
				return NextAction.Cancel
		}
		return NextAction.Continue
	}

	override var mouseClick: (mouseX: Number, mouseY: Number, button: Mouse) -> NextAction = ::onMouseClick

	override fun onMouseRelease(mouseX: Number, mouseY: Number, button: Mouse): NextAction {
		if (!active) return NextAction.Continue
		for (element in handleTree) {
			if (element.mouseRelease(mouseX, mouseY, button) == NextAction.Cancel) return NextAction.Cancel
		}
		return NextAction.Continue
	}

	override var mouseRelease: (mouseX: Number, mouseY: Number, button: Mouse) -> NextAction = ::onMouseRelease

	override fun onMouseDragging(mouseX: Number, mouseY: Number, button: Mouse, deltaX: Number, deltaY: Number): NextAction {
		if (!active) return NextAction.Continue
		for (element in handleTree) {
			if (element.mouseDragging(mouseX, mouseY, button, deltaX, deltaY) == NextAction.Cancel) return NextAction.Cancel
		}
		return NextAction.Continue
	}

	override var mouseDragging: (mouseX: Number, mouseY: Number, button: Mouse, deltaX: Number, deltaY: Number) -> NextAction =
		::onMouseDragging

	override fun onMouseScrolling(mouseX: Number, mouseY: Number, amount: Number): NextAction {
		if (!active) return NextAction.Continue
		for (element in handleTree) {
			if (element.mouseScrolling(mouseX, mouseY, amount) == NextAction.Cancel) return NextAction.Cancel
		}
		return NextAction.Continue
	}

	override var mouseScrolling: (mouseX: Number, mouseY: Number, amount: Number) -> NextAction = ::onMouseScrolling

	override fun onKeyPress(keyCode: KeyCode): NextAction {
		if (!active) return NextAction.Continue
		for (element in handleTree) {
			if (element.keyPress(keyCode) == NextAction.Cancel) return NextAction.Cancel
		}
		return NextAction.Continue
	}

	override var keyPress: (keyCode: KeyCode) -> NextAction = ::onKeyPress

	override fun onKeyRelease(keyCode: KeyCode): NextAction {
		if (!active) return NextAction.Continue
		for (element in handleTree) {
			if (element.keyRelease(keyCode) == NextAction.Cancel) return NextAction.Cancel
		}
		return NextAction.Continue
	}

	override var keyRelease: (keyCode: KeyCode) -> NextAction = ::onKeyRelease

	override fun onCharTyped(chr: Char): NextAction {
		if (!active) return NextAction.Continue
		for (element in handleTree) {
			if (element.charTyped(chr) == NextAction.Cancel) return NextAction.Cancel
		}
		return NextAction.Continue
	}

	override var charTyped: (chr: Char) -> NextAction = ::onCharTyped
}