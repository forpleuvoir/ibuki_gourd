package com.forpleuvoir.ibukigourd.gui.base

import com.forpleuvoir.ibukigourd.api.Tickable
import com.forpleuvoir.ibukigourd.gui.base.layout.Layout
import com.forpleuvoir.ibukigourd.input.KeyCode
import com.forpleuvoir.ibukigourd.input.Mouse
import com.forpleuvoir.ibukigourd.render.Drawable
import com.forpleuvoir.ibukigourd.render.base.Rectangle
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.nebula.common.api.Initializable
import net.minecraft.client.util.math.MatrixStack

@Suppress("unused", "KDocUnresolvedReference")
interface Element : Drawable, Tickable, Initializable {

	/**
	 * 基础属性变换
	 */
	val transform: Transform

	/**
	 * 是否为激活的元素
	 */
	var active: Boolean

	/**
	 * 处理优先级 越高越优先处理
	 */
	val priority: Int
		get() = transform.position.z.toInt()

	/**
	 * 固定元素，不会受到布局排列方法 [com.forpleuvoir.ibukigourd.gui.base.layout.Layout.arrange] 的位置调整
	 */
	val fixed: Boolean

	val margin: Margin

	val padding: Padding

	fun margin(margin: Number)

	fun margin(margin: Margin)

	fun margin(left: Number = this.margin.left, right: Number = this.margin.right, top: Number = this.margin.top, bottom: Number = this.margin.bottom)

	fun padding(padding: Number)

	fun padding(padding: Padding)

	fun padding(left: Number = this.padding.left, right: Number = this.padding.right, top: Number = this.padding.top, bottom: Number = this.padding.bottom)

	val layout: Layout

	/**
	 * 内容矩形
	 * @param isWorld Boolean
	 * @return Rectangle
	 */
	fun contentRect(isWorld: Boolean): Rectangle

	/**
	 * 渲染优先级 越高渲染层级越高
	 */
	override val renderPriority: Int
		get() = priority

	/**
	 * 子元素
	 */
	val elementTree: List<Element>

	val renderTree: List<Element>

	val handleTree: List<Element>

	fun <T : Element> addElement(element: T): T

	fun preElement(element: Element): Element?

	fun nextElement(element: Element): Element?

	fun elementIndexOf(element: Element): Int

	fun removeElement(element: Element): Boolean

	fun removeElement(index: Int)


	/**
	 *
	 * 以下方法和对应作为对象的高阶函数
	 *
	 * 应该是高阶函数作为被系统内部调用的方法
	 *
	 * 普通方法由子类实现
	 *
	 * 如果在DSL场景需要给对应方法添加代码，只需要给对应的高阶函数重新赋值
	 *
	 * 子类重写方法,调用者调用高阶函数
	 *
	 * 例:
	 *
	 * render={matrixStack,delta ->
	 *
	 *     onRender(matrixStack,delta)
	 *     code
	 * }
	 *
	 */
	var tick: () -> Unit

	override fun tick() {}

	var init: () -> Unit

	/**
	 * 渲染元素
	 * @param matrixStack MatrixStack
	 * @param delta Float 距离上一帧数渲染时间
	 */
	override var render: (matrixStack: MatrixStack, delta: Float) -> Unit

	/**
	 * 渲染元素
	 * @param matrixStack MatrixStack
	 * @param delta Float 距离上一帧数渲染时间
	 */
	override fun onRender(matrixStack: MatrixStack, delta: Float)

	/**
	 * 鼠标移动
	 * @param mouseX Number
	 * @param mouseY Number
	 */
	var mouseMove: (mouseX: Number, mouseY: Number) -> Unit

	/**
	 * 鼠标移动
	 * @param mouseX Number
	 * @param mouseY Number
	 */
	fun onMouseMove(mouseX: Number, mouseY: Number) {}

	/**
	 * 鼠标点击
	 * @param button Mouse
	 * @param mouseX Number
	 * @param mouseY Number
	 * @return 是否处理之后的同类操作
	 */
	var mouseClick: (mouseX: Number, mouseY: Number, button: Mouse) -> NextAction

	/**
	 * 鼠标点击
	 * @param button Mouse
	 * @param mouseX Number
	 * @param mouseY Number
	 * @return 是否处理之后的同类操作
	 */
	fun onMouseClick(mouseX: Number, mouseY: Number, button: Mouse): NextAction = NextAction.Cancel

	/**
	 * 鼠标释放
	 * @param button Mouse
	 * @param mouseX Number
	 * @param mouseY Number
	 * @return 是否处理之后的同类操作
	 */
	var mouseRelease: (mouseX: Number, mouseY: Number, button: Mouse) -> NextAction

	/**
	 * 鼠标释放
	 * @param button Mouse
	 * @param mouseX Number
	 * @param mouseY Number
	 * @return 是否处理之后的同类操作
	 */
	fun onMouseRelease(mouseX: Number, mouseY: Number, button: Mouse): NextAction = NextAction.Cancel

	/**
	 * 鼠标拖动
	 * @param mouseX Number
	 * @param mouseY Number
	 * @param button Mouse
	 * @param deltaX Number
	 * @param deltaY Number
	 * @return 是否处理之后的同类操作
	 */
	var mouseDragging: (mouseX: Number, mouseY: Number, button: Mouse, deltaX: Number, deltaY: Number) -> NextAction

	/**
	 * 鼠标拖动
	 * @param mouseX Number
	 * @param mouseY Number
	 * @param button Mouse
	 * @param deltaX Number
	 * @param deltaY Number
	 * @return 是否处理之后的同类操作
	 */
	fun onMouseDragging(mouseX: Number, mouseY: Number, button: Mouse, deltaX: Number, deltaY: Number): NextAction =
		NextAction.Cancel

	/**
	 * 鼠标滚动
	 * @param mouseX Number
	 * @param mouseY Number
	 * @param amount Number
	 * @return 是否处理之后的同类操作
	 */
	var mouseScrolling: (mouseX: Number, mouseY: Number, amount: Number) -> NextAction

	/**
	 * 鼠标滚动
	 * @param mouseX Number
	 * @param mouseY Number
	 * @param amount Number
	 * @return 是否处理之后的同类操作
	 */
	fun onMouseScrolling(mouseX: Number, mouseY: Number, amount: Number): NextAction = NextAction.Cancel

	/**
	 * 按键按下
	 * @param keyCode KeyCode
	 * @return 是否处理之后的同类操作
	 */
	var keyPress: (keyCode: KeyCode) -> NextAction

	/**
	 * 按键按下
	 * @param keyCode KeyCode
	 * @return 是否处理之后的同类操作
	 */
	fun onKeyPress(keyCode: KeyCode): NextAction = NextAction.Cancel

	/**
	 * 按键释放
	 * @param keyCode KeyCode
	 * @return 是否处理之后的同类操作
	 */
	var keyRelease: (keyCode: KeyCode) -> NextAction

	/**
	 * 按键释放
	 * @param keyCode KeyCode
	 * @return 是否处理之后的同类操作
	 */
	fun onKeyRelease(keyCode: KeyCode): NextAction = NextAction.Cancel

	/**
	 * 字符输入
	 * @param chr Char
	 * @return 是否处理之后的同类操作
	 */
	var charTyped: (chr: Char) -> NextAction

	/**
	 * 字符输入
	 * @param chr Char
	 * @return 是否处理之后的同类操作
	 */
	fun onCharTyped(chr: Char): NextAction = NextAction.Cancel
}