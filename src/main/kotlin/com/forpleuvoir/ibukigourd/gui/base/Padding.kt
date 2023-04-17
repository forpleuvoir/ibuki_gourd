package com.forpleuvoir.ibukigourd.gui.base

data class Padding(
	val left: Float = 0.0f,
	val right: Float = 0.0f,
	val top: Float = 0.0f,
	val bottom: Float = 0.0f
) {
	constructor(left: Number, right: Number, top: Number, bottom: Number) : this(
		left.toFloat(), right.toFloat(), top.toFloat(), bottom.toFloat()
	)

	constructor(horizontal: Number = 0f, vertical: Number = 0f) : this(horizontal, horizontal, vertical, vertical)

	val width get() = left + right

	val height get() = top + bottom

}