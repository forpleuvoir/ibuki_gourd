package com.forpleuvoir.ibukigourd.gui.base

import com.forpleuvoir.ibukigourd.api.Tickable
import com.forpleuvoir.nebula.common.api.Initializable

interface Element : Tickable, Initializable {

	/**
	 * 基础属性变换
	 */
	val transform: Transform

	var active: Boolean



}