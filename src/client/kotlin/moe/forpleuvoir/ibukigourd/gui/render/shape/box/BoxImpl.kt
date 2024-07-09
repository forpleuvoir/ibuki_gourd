package moe.forpleuvoir.ibukigourd.gui.render.shape.box

import moe.forpleuvoir.ibukigourd.gui.render.Size
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.render.math.copy
import org.joml.Vector2fc

class BoxImpl(
    override val position: Vector2fc,
    override val width: Float,
    override val height: Float
) : Box {

    constructor(x: Number, y: Number, width: Number, height: Number) : this(Vector2f(x, y), width.toFloat(), height.toFloat())

    constructor(x: Number, y: Number, size: Size<Number>) : this(Vector2f(x, y), size.width.toFloat(), size.height.toFloat())

    constructor(position: Vector2fc, size: Size<Float>) : this(position, size.width, size.height)

    override val vertexes: Array<out Vector2fc> by lazy {
        arrayOf(
            position,
            position.copy(y = y + height),
            position.copy(x + width, y + height),
            position.copy(x + width)
        )
    }

}