package forpleuvoir.ibuki_gourd.gui.common

import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.render.RenderUtil
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.Screen


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.common

 * 文件名 PositionDrawable

 * 创建时间 2022/1/3 16:20

 * @author forpleuvoir

 */
abstract class PositionDrawable(var x: Int, var y: Int, var width: Int, var height: Int) : IPositionElement, Element, Drawable {

	var parent: Screen? = null

	init {
		parent = ScreenBase.current
	}

	override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
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

	override var onPositionChanged: ((deltaX: Int, deltaY: Int, x: Int, y: Int) -> Unit)? = null
}