package moe.forpleuvoir.ibukigourd.render.shape.rectangle

import moe.forpleuvoir.ibukigourd.render.base.Dimension
import moe.forpleuvoir.ibukigourd.render.base.math.copy
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import org.joml.Vector3fc

class RectImpl(
    override val position: Vector3fc,
    override val width: Float,
    override val height: Float
) : Rectangle {

    constructor(x: Number, y: Number, z: Number, width: Number, height: Number) : this(vertex(x, y, z), width.toFloat(), height.toFloat())

    constructor(x: Number, y: Number, z: Number, dimension: Dimension<Number>) : this(vertex(x, y, z), dimension.width.toFloat(), dimension.height.toFloat())

    constructor(position: Vector3fc, dimension: Dimension<Float>) : this(position, dimension.width, dimension.height)

    override val vertexes: Array<Vector3fc>
        get() = arrayOf(
            position,
            position.copy(y = y + height),
            position.copy(x + width, y + height),
            position.copy(x + width)
        )

}