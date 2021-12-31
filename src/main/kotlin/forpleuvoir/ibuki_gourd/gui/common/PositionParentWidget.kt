package forpleuvoir.ibuki_gourd.gui.common

import forpleuvoir.ibuki_gourd.render.RenderUtil
import net.minecraft.client.gui.AbstractParentElement
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.util.math.MatrixStack
import java.util.function.Predicate


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.common

 * 文件名 PositionParentWidget

 * 创建时间 2021/12/30 17:16

 * @author forpleuvoir

 */
abstract class PositionParentWidget(var x: Int, var y: Int, var width: Int, var height: Int) : AbstractParentElement(), IPositionElement,
	Drawable, Selectable {

	var active: Boolean = true
	var visible: Boolean = true

	protected val children: MutableList<Element> by lazy { ArrayList() }
	var childrenPredicate: Predicate<Element> = Predicate { true }
	protected val drawableChildren: MutableList<Drawable> by lazy { ArrayList() }
	var drawableChildrenPredicate: Predicate<Drawable> = Predicate { true }

	override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
		if(!visible) return
		drawableChildren().forEach { it.render(matrices, mouseX, mouseY, delta) }
	}

	fun clearChildren() {
		children.clear()
		drawableChildren.clear()
	}

	override fun children(): MutableList<out Element> {
		return children.stream().filter(childrenPredicate).toList()
	}

	fun drawableChildren(): MutableList<out Drawable> {
		return drawableChildren.stream().filter(drawableChildrenPredicate).toList()
	}

	protected fun addChildren(children: Element) {
		this.children.add(children)
	}

	protected fun <T> addDrawableChild(drawableElement: T) where T : Element, T : Drawable {
		this.drawableChildren.add(drawableElement)
		this.children.add(drawableElement)
	}

	override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
		if(!active) return false
		return RenderUtil.isMouseHovered(this.x, this.y, this.width, this.height, mouseX, mouseY)
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

	override var onPositionChanged: ((deltaX: Int, deltaY: Int, x: Int, y: Int) -> Unit)? = { deltaX, deltaY, _, _ ->
		children().forEach {
			if (it is ClickableWidget) {
				it.deltaPosition(deltaX, deltaY)
			} else if (it is IPositionElement) {
				it.deltaPosition(deltaX, deltaY)
			}
		}
	}


}