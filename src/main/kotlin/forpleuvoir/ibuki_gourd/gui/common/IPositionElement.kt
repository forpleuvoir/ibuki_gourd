package forpleuvoir.ibuki_gourd.gui.common

import net.minecraft.client.gui.widget.ClickableWidget


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.common

 * 文件名 IPostionElement

 * 创建时间 2021/12/25 10:56

 * @author forpleuvoir

 */
interface IPositionElement {
	/**
	 * 修改位置
	 * @param x Int
	 * @param y Int
	 */
	fun setPosition(x: Int, y: Int)

	/**
	 * 增量修改位置
	 * @param deltaX Int
	 * @param deltaY Int
	 */
	fun deltaPosition(deltaX: Int, deltaY: Int)

	/**
	 * 位置变化之后调用
	 *
	 */
	var onPositionChanged: ((deltaX: Int, deltaY: Int, x: Int, y: Int) -> Unit)?
}

fun ClickableWidget.setPosition(x: Int, y: Int) {
	val deltaX = x - this.x
	val deltaY = y - this.y
	this.x = x
	this.y = y
	if (this is IPositionElement)
		onPositionChanged?.invoke(deltaX, deltaY, x, y)
}

fun ClickableWidget.deltaPosition(deltaX: Int, deltaY: Int) {
	this.x += deltaX
	this.y += deltaY
	if (this is IPositionElement)
		onPositionChanged?.invoke(deltaX, deltaY, x, y)
}