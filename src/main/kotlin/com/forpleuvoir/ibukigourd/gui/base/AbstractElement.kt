package com.forpleuvoir.ibukigourd.gui.base

import com.forpleuvoir.ibukigourd.util.NextAction
import net.minecraft.client.util.math.MatrixStack

abstract class AbstractElement : Element {
	override fun init() {}

	override val transform: Transform = Transform()

	override var active: Boolean = true

	override var tick: () -> Unit = ::tick

	override var render: (matrixStack: MatrixStack, delta: Double) -> Unit = ::onRender

	override var mouseMove: (mouseX: Number, mouseY: Number) -> Unit = ::onMouseMove

	override var mouseClick: (mouseX: Number, mouseY: Number, button: Int) -> NextAction = ::onMouseClick

	override var mouseRelease: (mouseX: Number, mouseY: Number, button: Int) -> NextAction = ::onMouseRelease

	override var mouseDragging: (mouseX: Number, mouseY: Number, button: Int, deltaX: Number, deltaY: Number) -> NextAction =
		::onMouseDragging

	override var mouseScrolling: (mouseX: Number, mouseY: Number, amount: Number) -> NextAction = ::onMouseScrolling

	override var keyPress: (keyCode: Int, modifiers: Int) -> NextAction = ::onKeyPress

	override var keyRelease: (keyCode: Int, modifiers: Int) -> NextAction = ::onKeyRelease

	override var charTyped: (chr: Char, modifiers: Int) -> NextAction = ::onCharTyped
}