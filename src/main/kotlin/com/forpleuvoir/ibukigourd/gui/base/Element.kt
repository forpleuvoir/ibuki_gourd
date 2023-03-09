package com.forpleuvoir.ibukigourd.gui.base

import com.forpleuvoir.ibukigourd.api.Tickable
import com.forpleuvoir.nebula.common.api.Initializable

interface Element : Tickable, Initializable {

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

	/**
	 * 渲染优先级 越高渲染层级越高
	 */
	val renderPriority: Int
		get() {
			return transform.position.z.toInt()
		}
}