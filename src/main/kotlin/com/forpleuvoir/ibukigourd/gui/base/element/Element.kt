package com.forpleuvoir.ibukigourd.gui.base.element

import com.forpleuvoir.ibukigourd.api.Tickable
import com.forpleuvoir.ibukigourd.gui.base.Transform
import com.forpleuvoir.ibukigourd.gui.screen.Screen
import com.forpleuvoir.ibukigourd.gui.tip.Tip
import com.forpleuvoir.ibukigourd.input.KeyCode
import com.forpleuvoir.ibukigourd.input.Mouse
import com.forpleuvoir.ibukigourd.render.Drawable
import com.forpleuvoir.ibukigourd.util.NextAction
import net.minecraft.client.util.math.MatrixStack

@Suppress("unused", "KDocUnresolvedReference")
interface Element : ElementContainer, Drawable, Tickable {

	/**
	 * 基础属性变换
	 */
	override val transform: Transform

	/**
	 * 父元素
	 */
	var parent: () -> Element?

	/**
	 *  当前元素所在的Screen
	 */
	val screen: () -> Screen?

	/**
	 * 是否为激活的元素
	 */
	val active: Boolean

	/**
	 * 处理优先级 越高越优先处理
	 */
	val priority: Int
		get() = transform.position.z.toInt()

	/**
	 * 固定元素，不会受到布局排列方法 [com.forpleuvoir.ibukigourd.gui.base.layout.Layout.arrange] 的位置调整
	 */
	val fixed: Boolean

	var tip: Tip?

	/**
	 * 渲染优先级 越高渲染层级越高
	 */
	override val renderPriority: Int
		get() = priority


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
	 * 渲染背景
	 * @param matrixStack MatrixStack
	 * @param delta Float
	 */
	var renderBackground: (matrixStack: MatrixStack, delta: Float) -> Unit

	/**
	 * 渲染背景
	 * @param matrixStack MatrixStack
	 * @param delta Float
	 */
	fun onRenderBackground(matrixStack: MatrixStack, delta: Float)

	/**
	 * 渲染覆盖层
	 * @param matrixStack MatrixStack
	 * @param delta Float
	 */
	var renderOverlay: (matrixStack: MatrixStack, delta: Float) -> Unit

	/**
	 * 渲染覆盖层
	 * @param matrixStack MatrixStack
	 * @param delta Float
	 */
	fun onRenderOverlay(matrixStack: MatrixStack, delta: Float)


	/**
	 * 鼠标移动
	 * @param mouseX Float
	 * @param mouseY Float
	 */
	var mouseMove: (mouseX: Float, mouseY: Float) -> Unit

	/**
	 * 鼠标移动
	 * @param mouseX Float
	 * @param mouseY Float
	 */
	fun onMouseMove(mouseX: Float, mouseY: Float) {}

	/**
	 * 鼠标点击
	 * @param button Mouse
	 * @param mouseX Float
	 * @param mouseY Float
	 * @return 是否处理之后的同类操作
	 */
	var mouseClick: (mouseX: Float, mouseY: Float, button: Mouse) -> NextAction

	/**
	 * 鼠标点击
	 * @param button Mouse
	 * @param mouseX Float
	 * @param mouseY Float
	 * @return 是否处理之后的同类操作
	 */
	fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction = NextAction.Cancel

	/**
	 * 鼠标释放
	 * @param button Mouse
	 * @param mouseX Float
	 * @param mouseY Float
	 * @return 是否处理之后的同类操作
	 */
	var mouseRelease: (mouseX: Float, mouseY: Float, button: Mouse) -> NextAction

	/**
	 * 鼠标释放
	 * @param button Mouse
	 * @param mouseX Float
	 * @param mouseY Float
	 * @return 是否处理之后的同类操作
	 */
	fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse): NextAction = NextAction.Cancel

	/**
	 * 鼠标是否为拖拽中
	 */
	var dragging: Boolean

	/**
	 * 鼠标拖动
	 * @param mouseX Float
	 * @param mouseY Float
	 * @param button Mouse
	 * @param deltaX Float
	 * @param deltaY Float
	 * @return 是否处理之后的同类操作
	 */
	var mouseDragging: (mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float) -> NextAction

	/**
	 * 鼠标拖动
	 * @param mouseX Float
	 * @param mouseY Float
	 * @param button Mouse
	 * @param deltaX Float
	 * @param deltaY Float
	 * @return 是否处理之后的同类操作
	 */
	fun onMouseDragging(mouseX: Float, mouseY: Float, button: Mouse, deltaX: Float, deltaY: Float): NextAction =
		NextAction.Cancel

	/**
	 * 鼠标滚动
	 * @param mouseX Float
	 * @param mouseY Float
	 * @param amount Float
	 * @return 是否处理之后的同类操作
	 */
	var mouseScrolling: (mouseX: Float, mouseY: Float, amount: Float) -> NextAction

	/**
	 * 鼠标滚动
	 * @param mouseX Float
	 * @param mouseY Float
	 * @param amount Float
	 * @return 是否处理之后的同类操作
	 */
	fun onMouseScrolling(mouseX: Float, mouseY: Float, amount: Float): NextAction = NextAction.Cancel

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