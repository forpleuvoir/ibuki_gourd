package com.forpleuvoir.ibukigourd.gui.base

import net.minecraft.util.Identifier


/**
 * gui 材质

 * 项目名 forpleuvoir_mc_mod

 * 包名 forpleuvoir.mc.library.gui.texture

 * 文件名 GuiTexture

 * 创建时间 2022/7/17 18:25

 * @author forpleuvoir

 */
data class GuiTexture(
	val texture: Identifier,
	val corner: Corner,
	val u: Int,
	val v: Int,
	val regionWidth: Int,
	val regionHeight: Int,
	val textureWidth: Int,
	val textureHeight: Int,
) {
	data class Corner(
		val left: Int,
		val right: Int,
		val top: Int,
		val bottom: Int,
	) {
		constructor(vertical: Int, horizontal: Int) : this(
			left = vertical,
			right = vertical,
			top = horizontal,
			bottom = horizontal
		)

		constructor(corner: Int) : this(corner, corner, corner, corner)
	}


}

