package com.forpleuvoir.ibukigourd.gui.screen

import com.forpleuvoir.ibukigourd.gui.base.element.Element
import com.forpleuvoir.ibukigourd.gui.tip.TipHandler

interface Screen : Element, TipHandler {

	/**
	 * 上一级屏幕
	 */
	var parentScreen: Screen?

	/**
	 * 打开时是否需要暂停游戏，在服务器中无效
	 */
	val pauseGame: Boolean

	/**
	 * 是否需要在按下ESC之后关闭当前屏幕
	 */
	val shouldCloseOnEsc: Boolean

	/**
	 * 重新调整屏幕大小
	 */
	var resize: (width: Int, height: Int) -> Unit

	/**
	 * 重新调整屏幕大小
	 * @param width Int
	 * @param height Int
	 */
	fun onResize(width: Int, height: Int)

	var close: () -> Unit

	fun onClose()

}