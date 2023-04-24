package com.forpleuvoir.ibukigourd.gui.base.element

import com.forpleuvoir.ibukigourd.gui.base.Tip
import com.forpleuvoir.ibukigourd.input.KeyCode
import com.forpleuvoir.ibukigourd.input.Mouse
import com.forpleuvoir.ibukigourd.util.NextAction
import net.minecraft.client.util.math.MatrixStack

@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractElement : Element, AbstractElementContainer() {

	override var active = true
		set(value) {
			if (!value) elementTree.asSequence().filter { it.active }.forEach { it.active = false }
			field = value
		}

	override var fixed: Boolean = false

	override var tip: Tip? = null

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