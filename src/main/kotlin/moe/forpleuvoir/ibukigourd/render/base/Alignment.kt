@file:Suppress("DuplicatedCode", "unused")

package moe.forpleuvoir.ibukigourd.render.base

import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.math.copy
import moe.forpleuvoir.ibukigourd.render.shape.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.shape.rectangle.rect
import moe.forpleuvoir.nebula.common.sumOf
import org.joml.Vector3fc

interface Alignment {

    /**
     * 排列方式
     */
    val orientation: Orientation

    /**
     * 计算对齐之后的位置信息
     * @param parent 外部矩形
     * @param rectangles 内部需要被对齐的矩形大小
     * @return [Vector3f]对齐之后的位置信息,应该为内部矩形的左上角顶点
     */
    fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc>

    fun align(parent: Rectangle, rectangle: Rectangle): Vector3fc = align(parent, listOf(rectangle))[0]

}

enum class Orientation {

    Vertical {
        override fun contentSize(rectangles: List<Rectangle>): Dimension<Float> =
            Dimension.of(rectangles.maxOf { it.width }, rectangles.sumOf { it.height })

        override fun calcPosition(position: Vector3fc, rectangles: List<Rectangle>): List<Vector3fc> {
            return buildList {
                var y = position.y()
                for (rectangle in rectangles) {
                    add(position.copy(y = y))
                    y += rectangle.height
                }
            }
        }
    },
    Horizontal {
        override fun contentSize(rectangles: List<Rectangle>): Dimension<Float> =
            Dimension.of(rectangles.sumOf { it.width }, rectangles.maxOf { it.height })

        override fun calcPosition(position: Vector3fc, rectangles: List<Rectangle>): List<Vector3fc> {
            return buildList {
                var x = position.x()
                for (rectangle in rectangles) {
                    add(position.copy(x = x))
                    x += rectangle.width
                }
            }
        }
    };

    inline fun <R> choose(vertical: (Orientation) -> R, horizontal: (Orientation) -> R): R {
        return when (this) {
            Vertical   -> vertical(this)
            Horizontal -> horizontal(this)
        }
    }

    fun <R> choose(vertical: R, horizontal: R): R {
        return when (this) {
            Vertical   -> vertical
            Horizontal -> horizontal
        }
    }

    /**
     * 排列之后的总大小
     * @param rectangles List<Rectangle>
     * @return Size<Float>
     */
    abstract fun contentSize(rectangles: List<Rectangle>): Dimension<Float>

    /**
     * 计算排列之后的每一个元素的位置
     * @param position Vector3<Float>
     * @param rectangles List<Rectangle>
     */
    abstract fun calcPosition(position: Vector3fc, rectangles: List<Rectangle>): List<Vector3fc>

    /**
     * 使用提供的转换函数计算给定位置的新位置。
     * @param position 要转换的原始位置。类型必须为 Vector3<Float>。
     * @param rectangles 用于映射每个位置的矩形列表。类型必须为 List<Rectangle<Vector3<Float>>>。
     * @param map 转换函数。接受位置和矩形，返回新位置。类型必须为 (Vector3<Float>, Rectangle<Vector3<Float>>) -> Vector3<Float>
     * @return 转换后的新位置列表。每个新位置的类型都是 Vector3<Float>。
     */
    fun calcPosition(
        position: Vector3fc,
        rectangles: List<Rectangle>,
        map: (Vector3fc, Rectangle) -> Vector3fc
    ): List<Vector3fc> {
        return buildList {
            calcPosition(position, rectangles).forEachIndexed { index, pos ->
                add(map(pos, rectangles[index]))
            }
        }
    }

}


sealed class PlanarAlignment(override val orientation: Orientation = Orientation.Vertical) : Alignment {
    class TopLeft(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            return orientation.calcPosition(parent.position, rectangles)
        }
    }

    class TopCenter(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            return orientation.choose(
                orientation.calcPosition(parent.position, rectangles) { pos, rect -> pos.copy(x = parent.center.x() - rect.halfWidth) },
                orientation.calcPosition(parent.position.copy(x = parent.center.x() - orientation.contentSize(rectangles).halfWidth), rectangles)
            )
        }
    }

    class TopRight(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            return orientation.choose(
                orientation.calcPosition(parent.position, rectangles) { pos, rect -> pos.copy(x = parent.right - rect.width) },
                orientation.calcPosition(parent.position.copy(x = parent.right - orientation.contentSize(rectangles).width), rectangles)
            )
        }
    }

    class CenterLeft(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            val size = orientation.contentSize(rectangles)
            val y = parent.center.y() - size.halfHeight
            val x = parent.left
            val rect = rect(parent.position.copy(x = x, y = y), size)
            return orientation.choose(
                orientation.calcPosition(rect.position, rectangles),
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(y = rect.center.y() - r.halfHeight) }
            )
        }
    }

    class Center(orientation: Orientation = Orientation.Vertical) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            val size = orientation.contentSize(rectangles)
            val y = parent.center.y() - size.halfHeight
            val x = parent.center.x() - size.halfWidth
            val rect = rect(parent.position.copy(x = x, y = y), size)
            return orientation.choose(
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(x = rect.center.x() - r.halfWidth) },
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(y = rect.center.y() - r.halfHeight) }
            )
        }
    }

    class CenterRight(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            val size = orientation.contentSize(rectangles)
            val y = parent.center.y() - size.halfHeight
            val x = parent.right - size.width
            val rect = rect(parent.position.copy(x = x, y = y), size)
            return orientation.choose(
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(x = rect.right - r.width) },
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(y = rect.center.y() - r.halfHeight) }
            )
        }
    }

    class BottomLeft(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            val size = orientation.contentSize(rectangles)
            val y = parent.bottom - size.height
            val x = parent.left
            val rect = rect(parent.position.copy(x = x, y = y), size)
            return orientation.choose(
                orientation.calcPosition(rect.position, rectangles),
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(y = rect.bottom - r.height) }
            )
        }
    }

    class BottomCenter(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            val size = orientation.contentSize(rectangles)
            val y = parent.bottom - size.height
            val x = parent.center.x() - size.halfWidth
            val rect = rect(parent.position.copy(x = x, y = y), size)
            return orientation.choose(
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(x = rect.center.x() - r.halfWidth) },
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(y = rect.bottom - r.height) }
            )
        }
    }

    class BottomRight(orientation: Orientation) : PlanarAlignment(orientation) {
        override fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc> {
            val size = orientation.contentSize(rectangles)
            val y = parent.bottom - size.height
            val x = parent.right - size.width
            val rect = rect(parent.position.copy(x = x, y = y), size)
            return orientation.choose(
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(x = rect.right - r.width) },
                orientation.calcPosition(rect.position, rectangles) { pos, r -> pos.copy(y = rect.bottom - r.height) }
            )
        }
    }
}
