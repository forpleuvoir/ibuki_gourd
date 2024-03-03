package moe.forpleuvoir.ibukigourd.render.shape.rectangle

import moe.forpleuvoir.ibukigourd.render.base.Dimension
import moe.forpleuvoir.ibukigourd.render.base.DimensionFloat
import moe.forpleuvoir.ibukigourd.render.base.vertex.ColoredVertex
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.pick
import org.joml.Vector3fc
import kotlin.math.max
import kotlin.math.min

interface Rectangle : DimensionFloat, Cloneable {

    val position: Vector3fc

    /**
     * 矩形的四个顶点
     * 0: 左上
     * 1: 左下
     * 2: 右下
     * 3: 右上
     *
     */
    val vertexes: Array<out Vector3fc>

    val topLeft: Vector3fc get() = vertexes[0]

    val bottomLeft: Vector3fc get() = vertexes[1]

    val bottomRight: Vector3fc get() = vertexes[2]

    val topRight: Vector3fc get() = vertexes[3]

    override val width: Float

    override val height: Float

    val top: Float get() = position.y()

    val bottom: Float get() = position.y() + height

    val left: Float get() = position.x()

    val right: Float get() = position.x() + width

    val x: Float get() = position.x()

    val endX: Float get() = x + width

    val y: Float get() = position.y()

    val endY: Float get() = y + height

    val z: Float get() = position.z()

    val center: Vector3fc get() = vertex(x + this.width / 2, y + this.height / 2, z)

    val exist: Boolean get() = this.width > 0 && this.height > 0

    fun inRect(target: Rectangle, completeInside: Boolean): Boolean {
        if (!this.exist || !target.exist) return false
        return if (completeInside) {
            this.x - target.x >= 0
            && target.endX - this.endX >= 0
            && this.y - target.y >= 0
            && target.endY - this.endY >= 0
        } else {
            this.intersection(target).exist
        }
    }

    fun contains(x: Float, y: Float): Boolean {
        return if (this.exist)
            x in this.top..this.bottom && y in this.left..this.right
        else false
    }

    operator fun contains(vector3fc: Vector3fc): Boolean {
        return this.exist.pick(
            vector3fc.x() in this.top..this.bottom && vector3fc.y() in this.left..this.right,
            false
        )
    }

    /**
     * 判断两个矩形是否相交
     * @param target Rectangle 目标矩形
     * @return [Rectangle] 相交时返回交集矩形，否则返回不存在的矩形[NULL]
     */
    infix fun intersection(target: Rectangle): Rectangle {
        if (!this.exist || !target.exist) return NULL
        val startX = max(this.x, target.x)
        val startY = max(this.y, target.y)
        val endX = min(this.endX, target.endX)
        val endY = min(this.endY, target.endY)
        rect(startX, startY, endX, endY).let {
            return if (it.exist) it else NULL
        }
    }

    @Suppress("DuplicatedCode")
    companion object {

        val NULL = rect(0, 0, 0, 0)

        /**
         * 只判断矩形的位置与大小
         * @param rectangle Rectangle
         * @param other Rectangle
         * @return Boolean
         */
        fun equals(rectangle: Rectangle, other: Rectangle): Boolean {
            if (rectangle.position != other.position) return false
            if (rectangle.width != other.width) return false
            return rectangle.height == other.height
        }

        fun intersection(rects: Iterable<Rectangle>): Rectangle {
            var temp = rects.first()
            rects.forEach {
                if (!equals(it, rects.first())) {
                    temp = it intersection temp
                }
                if (!temp.exist) return NULL
            }
            return temp
        }
    }

}

fun rect(position: Vector3fc, width: Float, height: Float): Rectangle = RectImpl(position, width, height)

fun rect(position: Vector3fc, width: Number, height: Number): Rectangle = RectImpl(position, width.toFloat(), height.toFloat())

fun rect(x: Number, y: Number, z: Number, width: Number, height: Number): Rectangle = RectImpl(x, y, z, width, height)

fun rect(startX: Number, startY: Number, endX: Number, endY: Number): Rectangle =
    RectImpl(startX, startY, 0f, endX.toFloat() - startX.toFloat(), endY.toFloat() - startY.toFloat())

fun rect(x: Number, y: Number, z: Number, dimension: Dimension<Number>): Rectangle = RectImpl(x, y, z, dimension)

fun rect(position: Vector3fc, dimension: Dimension<Float>): Rectangle = RectImpl(position, dimension)

fun coloredRect(coloredVertex: ColoredVertex, width: Float, height: Float): ColoredRect =
    ColoredRect(coloredVertex, width, height, coloredVertex.color, coloredVertex.color, coloredVertex.color, coloredVertex.color)

fun coloredRect(position: Vector3fc, width: Float, height: Float, vararg colors: ARGBColor): ColoredRect =
    ColoredRect(position, width, height, *colors)

fun coloredRect(x: Number, y: Number, z: Number, width: Number, height: Number, vararg colors: ARGBColor): ColoredRect =
    ColoredRect(x, y, z, width, height, *colors)

fun coloredRect(x: Number, y: Number, z: Number, dimension: Dimension<Float>, vararg colors: ARGBColor): ColoredRect =
    ColoredRect(x, y, z, dimension.width, dimension.height, *colors)

fun coloredRect(rect: Rectangle, vararg colors: ARGBColor): ColoredRect =
    ColoredRect(rect.x, rect.y, rect.z, rect.width, rect.height, *colors)

fun coloredRect(position: Vector3fc, dimension: Dimension<Float>, vararg colors: ARGBColor): ColoredRect = ColoredRect(position, dimension, *colors)