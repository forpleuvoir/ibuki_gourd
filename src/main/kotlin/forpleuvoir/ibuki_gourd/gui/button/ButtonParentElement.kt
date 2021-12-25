package forpleuvoir.ibuki_gourd.gui.button

import forpleuvoir.ibuki_gourd.config.options.ConfigHotkey
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.ParentElement
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.button

 * 文件名 ButtonParentElement

 * 创建时间 2021/12/25 10:40

 * @author forpleuvoir

 */
abstract class ButtonParentElement(
	x: Int,
	y: Int,
	width: Int,
	height: Int,
	message: Text,
	onButtonPress: ((ButtonParentElement) -> Unit)? = null
) :
	ButtonBase<ButtonParentElement>(x, y, width, height, message, onButtonPress), ParentElement {


	private var dragging: Boolean = false

	private var focused: Element? = null

	private val children: MutableList<ClickableWidget> = ArrayList()
	protected val drawableChildren: MutableList<Drawable> by lazy { ArrayList() }

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(matrices, mouseX, mouseY, delta)
		renderChildren(matrices, mouseX, mouseY, delta)
	}

	override var onPositionChanged: ((Int, Int, Int, Int) -> Unit)? = { deltaX, deltaY, _, _ ->
		children().forEach {
			if (it is ClickableWidget) {
				it.x += deltaX
				it.y += deltaY
			}
		}
	}

	protected fun renderChildren(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		drawableChildren.forEach { it.render(matrices, mouseX, mouseY, delta) }
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		return if (!super<ButtonBase>.mouseClicked(mouseX, mouseY, button))
			super<ParentElement>.mouseClicked(mouseX, mouseY, button)
		else true
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		return if (!super<ButtonBase>.mouseReleased(mouseX, mouseY, button))
			return super<ParentElement>.mouseReleased(mouseX, mouseY, button)
		else true
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		return if (!super<ButtonBase>.mouseDragged(mouseX, mouseY, button,deltaX, deltaY))
			return super<ParentElement>.mouseDragged(mouseX, mouseY, button,deltaX, deltaY)
		else true
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		return if (super<ButtonBase>.keyPressed(keyCode, scanCode, modifiers))
		return super<ParentElement>.keyPressed(keyCode, scanCode, modifiers)
		else true
	}


	override fun changeFocus(lookForwards: Boolean): Boolean {
		return super<ButtonBase>.changeFocus(lookForwards)
	}

	override fun children(): MutableList<out Element> {
		return children
	}

	override fun isDragging(): Boolean {
		return dragging
	}

	override fun setDragging(dragging: Boolean) {
		this.dragging = dragging
	}

	override fun getFocused(): Element? {
		return focused
	}

	override fun setFocused(focused: Element?) {
		this.focused = focused
	}

	protected fun addChildren(children: ClickableWidget) {
		this.children.add(children)
	}

	protected fun <T> addDrawableChild(drawableElement: T) where T : ClickableWidget, T : Drawable {
		this.drawableChildren.add(drawableElement)
		this.children.add(drawableElement)
	}

}