package com.forpleuvoir.ibukigourd.gui.base

class Padding(
    var left: Float = 0.0f,
    var right: Float = 0.0f,
    var top: Float = 0.0f,
    var bottom: Float = 0.0f
) {
    constructor(left: Number, right: Number, top: Number, bottom: Number) : this(
        left.toFloat(), right.toFloat(), top.toFloat(), bottom.toFloat()
    )

    constructor(horizontal: Number = 0f, vertical: Number = 0f) : this(horizontal, horizontal, vertical, vertical)
}