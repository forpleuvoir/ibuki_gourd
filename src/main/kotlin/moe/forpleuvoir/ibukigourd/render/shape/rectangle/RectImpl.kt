package moe.forpleuvoir.ibukigourd.render.shape.rectangle

import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.math.copy
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import org.joml.Vector3fc

class RectImpl(
    override val position: Vector3fc,
    override val width: Float,
    override val height: Float
) : Rectangle {

    constructor(x: Number, y: Number, z: Number, width: Number, height: Number) : this(vertex(x, y, z), width.toFloat(), height.toFloat())

    constructor(x: Number, y: Number, z: Number, size: Size<Number>) : this(vertex(x, y, z), size.width.toFloat(), size.height.toFloat())

    constructor(position: Vector3fc, size: Size<Float>) : this(position, size.width, size.height)

    override val vertexes: Array<Vector3fc>
        get() = arrayOf(
            position,
            position.copy(y = y + height),
            position.copy(x + width, y + height),
            position.copy(x + width)
        )

}