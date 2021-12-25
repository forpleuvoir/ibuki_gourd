package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.gui.button.ButtonBase
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.ParentElement
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.util.math.MatrixStack
import java.util.function.Supplier


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.widget

 * 文件名 WidgetSliderNumberParentElement

 * 创建时间 2021/12/25 12:13

 * @author forpleuvoir

 */
abstract class WidgetSliderNumberParentElement(x: Int, y: Int, width: Int, height: Int, number: Supplier<Number>, minValue: Number, maxValue: Number) :
	WidgetSliderNumber(x, y, width, height, number, minValue, maxValue),ParentElement,IPositionElement {

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
				it.deltaPosition(deltaX, deltaY)
			} else if (it is IPositionElement) {
				it.deltaPosition(deltaX, deltaY)
			}
		}
	}

	override fun setPosition(x: Int, y: Int) {
		val deltaX = x - this.x
		val deltaY = y - this.y
		this.x = x
		this.y = y
		onPositionChanged?.invoke(deltaX, deltaY, x, y)
	}

	override fun deltaPosition(deltaX: Int, deltaY: Int) {
		this.x += deltaX
		this.y += deltaY
		onPositionChanged?.invoke(deltaX, deltaY, this.x, this.y)
	}

	protected fun renderChildren(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		drawableChildren.forEach { it.render(matrices, mouseX, mouseY, delta) }
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		return if (!super<WidgetSliderNumber>.mouseClicked(mouseX, mouseY, button))
			super<ParentElement>.mouseClicked(mouseX, mouseY, button)
		else true
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		return if (!super<WidgetSliderNumber>.mouseReleased(mouseX, mouseY, button))
			return super<ParentElement>.mouseReleased(mouseX, mouseY, button)
		else true
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		return if (!super<WidgetSliderNumber>.mouseDragged(mouseX, mouseY, button,deltaX, deltaY))
			return super<ParentElement>.mouseDragged(mouseX, mouseY, button,deltaX, deltaY)
		else true
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		return if (super<WidgetSliderNumber>.keyPressed(keyCode, scanCode, modifiers))
			return super<ParentElement>.keyPressed(keyCode, scanCode, modifiers)
		else true
	}


	override fun changeFocus(lookForwards: Boolean): Boolean {
		return super<WidgetSliderNumber>.changeFocus(lookForwards)
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