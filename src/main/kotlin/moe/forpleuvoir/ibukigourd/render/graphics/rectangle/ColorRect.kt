package moe.forpleuvoir.ibukigourd.render.graphics.rectangle

import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.math.ImmutableVector3f
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.vertex.ColorVertex
import moe.forpleuvoir.ibukigourd.render.base.vertex.colorVertex
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.nebula.common.color.ARGBColor

class ColorRect(
    override val position: Vector3<Float>,
    override val width: Float,
    override val height: Float,
    private vararg val colors: ARGBColor
) : Rectangle<ColorVertex> {

    constructor(x: Number, y: Number, z: Number, width: Number, height: Number, vararg colors: ARGBColor) : this(
        vertex(x, y, z),
        width.toFloat(),
        height.toFloat(),
        *colors
    )

    constructor(x: Number, y: Number, z: Number, size: Size<Number>, vararg colors: ARGBColor) : this(
        vertex(x, y, z),
        size.width.toFloat(),
        size.height.toFloat(),
        *colors
    )

    constructor(position: Vector3<Float>, size: Size<Float>, vararg colors: ARGBColor) : this(
        ImmutableVector3f(position.x, position.y, position.z),
        size.width,
        size.height,
        *colors
    )

    /**
     * Represents an implementation of the `vertexes` property in the `ColorRect` class.
     * This property returns an array of `ColorVertex` objects.
     *
     * @return An array of `ColorVertex` objects.
     */
    override val vertexes: Array<ColorVertex>
        get() = arrayOf(
            colorVertex(position, colors[0]),
            colorVertex(position.y(y + height), if (colors.size < 2) colors[0] else colors[1]),
            colorVertex(position.xyz(x + width, y + height), if (colors.size < 3) colors[0] else colors[2]),
            colorVertex(position.x(x + width), if (colors.size < 4) colors[0] else colors[3])
        )

}