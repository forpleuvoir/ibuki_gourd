package moe.forpleuvoir.ibukigourd.gui.render.shape.box

import moe.forpleuvoir.ibukigourd.gui.render.Size
import moe.forpleuvoir.ibukigourd.gui.render.vertex.ColoredVertex
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.render.math.copy
import moe.forpleuvoir.ibukigourd.render.math.toVector2fc
import moe.forpleuvoir.ibukigourd.render.math.toVector3fc
import moe.forpleuvoir.nebula.common.color.ARGBColor
import org.joml.Vector2fc

class ColoredBox(
    override val position: Vector2fc,
    override val width: Float,
    override val height: Float,
    private vararg val colors: ARGBColor
) : Box {

    constructor(x: Number, y: Number, width: Number, height: Number, vararg colors: ARGBColor) : this(
        Vector2f(x, y),
        width.toFloat(),
        height.toFloat(),
        *colors
    )

    constructor(x: Number, y: Number, size: Size<Number>, vararg colors: ARGBColor) : this(
        Vector2f(x, y),
        size.width.toFloat(),
        size.height.toFloat(),
        *colors
    )

    constructor(position: Vector2fc, size: Size<Float>, vararg colors: ARGBColor) : this(
        position,
        size.width,
        size.height,
        *colors
    )

    constructor(coloredVertex: ColoredVertex, width: Float, height: Float) : this(
        coloredVertex.toVector2fc(),
        width,
        height,
        coloredVertex.color,
        coloredVertex.color,
        coloredVertex.color,
        coloredVertex.color
    )

    constructor(x: Number, y: Number, size: Size<Float>, vararg colors: ARGBColor) : this(x, y, size.width, size.height, *colors)

    constructor(box: Box, vararg colors: ARGBColor) : this(box.x, box.y, box.width, box.height, *colors)

    val coloredVertexes: Array<ColoredVertex> by lazy {
        position.toVector3fc().let { position ->
            arrayOf(
                ColoredVertex(position, colors[0]),
                ColoredVertex(position.copy(y = y + height), if (colors.size < 2) colors[0] else colors[1]),
                ColoredVertex(position.copy(x + width, y + height), if (colors.size < 3) colors[0] else colors[2]),
                ColoredVertex(position.copy(x + width), if (colors.size < 4) colors[0] else colors[3])
            )
        }
    }

    override val vertexes: Array<out Vector2fc> = coloredVertexes.map { it.toVector2fc() }.toTypedArray()

    override val topLeft: Vector2fc get() = vertexes[0]

    override val bottomLeft: Vector2fc get() = vertexes[1]

    override val bottomRight: Vector2fc get() = vertexes[2]

    override val topRight: Vector2fc get() = vertexes[3]

}
