package moe.forpleuvoir.ibukigourd.render.shape.rectangle

import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.math.copy
import moe.forpleuvoir.ibukigourd.render.base.vertex.ColoredVertex
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.nebula.common.color.ARGBColor
import org.joml.Vector3fc

class ColoredRect(
    override val position: Vector3fc,
    override val width: Float,
    override val height: Float,
    private vararg val colors: ARGBColor
) : Rectangle {

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

    constructor(position: Vector3fc, size: Size<Float>, vararg colors: ARGBColor) : this(
        position,
        size.width,
        size.height,
        *colors
    )

    override val vertexes: Array<ColoredVertex>
        get() = arrayOf(
            ColoredVertex(position, colors[0]),
            ColoredVertex(position.copy(y = y + height), if (colors.size < 2) colors[0] else colors[1]),
            ColoredVertex(position.copy(x + width, y + height), if (colors.size < 3) colors[0] else colors[2]),
            ColoredVertex(position.copy(x + width), if (colors.size < 4) colors[0] else colors[3])
        )

    override val topLeft: ColoredVertex get() = vertexes[0]

    override val bottomLeft: ColoredVertex get() = vertexes[1]

    override val bottomRight: ColoredVertex get() = vertexes[2]

    override val topRight: ColoredVertex get() = vertexes[3]

}