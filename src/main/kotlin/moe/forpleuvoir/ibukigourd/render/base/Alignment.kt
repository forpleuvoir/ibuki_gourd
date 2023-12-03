@file:Suppress("DuplicatedCode", "unused")

package moe.forpleuvoir.ibukigourd.render.base

import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.base.rectangle.rect
import moe.forpleuvoir.nebula.common.sumOf

interface Alignment {

    /**
     * 排列方式
     */
    val arrangement: Arrangement

    /**
     * 计算对齐之后的位置信息
     * @param parent 外部矩形
     * @param rectangles 内部需要被对齐的矩形大小
     * @return [Vector3f]对齐之后的位置信息,应该为内部矩形的左上角顶点
     */
    fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>>

    fun align(parent: Rectangle<Vector3<Float>>, rectangle: Rectangle<Vector3<Float>>): Vector3<Float> = align(parent, listOf(rectangle))[0]

}

enum class Arrangement {

    Vertical {
        override fun contentSize(rectangles: List<Rectangle<Vector3<Float>>>): Size<Float> =
            Size.create(rectangles.maxOf { it.width }, rectangles.sumOf { it.height })

        override fun calcPosition(position: Vector3<Float>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
            return buildList {
                var y = position.y
                for (rectangle in rectangles) {
                    add(position.y(y))
                    y += rectangle.height
                }
            }
        }
    },
    Horizontal {
        override fun contentSize(rectangles: List<Rectangle<Vector3<Float>>>): Size<Float> =
            Size.create(rectangles.sumOf { it.width }, rectangles.maxOf { it.height })

        override fun calcPosition(position: Vector3<Float>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
            return buildList {
                var x = position.x
                for (rectangle in rectangles) {
                    add(position.x(x))
                    x += rectangle.width
                }
            }
        }
    };

    inline fun <R> switch(vertical: (Arrangement) -> R, horizontal: (Arrangement) -> R): R {
        return when (this) {
            Vertical   -> vertical(this)
            Horizontal -> horizontal(this)
        }
    }

    fun <R> switch(vertical: R, horizontal: R): R {
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
    abstract fun contentSize(rectangles: List<Rectangle<Vector3<Float>>>): Size<Float>

    /**
     * 计算排列之后的每一个元素的位置
     * @param position Vector3<Float>
     * @param rectangles List<Rectangle>
     */
    abstract fun calcPosition(position: Vector3<Float>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>>

    /**
     * 使用提供的转换函数计算给定位置的新位置。
     * @param position 要转换的原始位置。类型必须为 Vector3<Float>。
     * @param rectangles 用于映射每个位置的矩形列表。类型必须为 List<Rectangle<Vector3<Float>>>。
     * @param map 转换函数。接受位置和矩形，返回新位置。类型必须为 (Vector3<Float>, Rectangle<Vector3<Float>>) -> Vector3<Float>
     * @return 转换后的新位置列表。每个新位置的类型都是 Vector3<Float>。
     */
    fun calcPosition(
        position: Vector3<Float>,
        rectangles: List<Rectangle<Vector3<Float>>>,
        map: (Vector3<Float>, Rectangle<Vector3<Float>>) -> Vector3<Float>
    ): List<Vector3<Float>> {
        return buildList {
            calcPosition(position, rectangles).forEachIndexed { index, pos ->
                add(map(pos, rectangles[index]))
            }
        }
    }

}


sealed class PlanarAlignment(override val arrangement: Arrangement = Arrangement.Vertical) : Alignment {
    class TopLeft(arrangement: Arrangement = Arrangement.Vertical) : PlanarAlignment(arrangement) {
        override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
            return arrangement.calcPosition(parent.position, rectangles)
        }
    }

    class TopCenter(arrangement: Arrangement = Arrangement.Vertical) : PlanarAlignment(arrangement) {
        override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
            return arrangement.switch(
                arrangement.calcPosition(parent.position, rectangles) { pos, rect -> pos.x(x = parent.center.x - rect.halfWidth) },
                arrangement.calcPosition(parent.position.x(parent.center.x - arrangement.contentSize(rectangles).halfWidth), rectangles)
            )
        }
    }

    class TopRight(arrangement: Arrangement = Arrangement.Vertical) : PlanarAlignment(arrangement) {
        override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
            return arrangement.switch(
                arrangement.calcPosition(parent.position, rectangles) { pos, rect -> pos.x(x = parent.right - rect.width) },
                arrangement.calcPosition(parent.position.x(parent.right - arrangement.contentSize(rectangles).width), rectangles)
            )
        }
    }

    class CenterLeft(arrangement: Arrangement = Arrangement.Vertical) : PlanarAlignment(arrangement) {
        override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
            val size = arrangement.contentSize(rectangles)
            val y = parent.center.y - size.halfHeight
            val x = parent.left
            val rect = rect(parent.position.xyz(x, y), size)
            return arrangement.switch(
                arrangement.calcPosition(rect.position, rectangles),
                arrangement.calcPosition(rect.position, rectangles) { pos, r -> pos.y(rect.center.y - r.halfHeight) }
            )
        }
    }

    class Center(arrangement: Arrangement = Arrangement.Vertical) : PlanarAlignment(arrangement) {
        override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
            val size = arrangement.contentSize(rectangles)
            val y = parent.center.y - size.halfHeight
            val x = parent.center.x - size.halfWidth
            val rect = rect(parent.position.xyz(x, y), size)
            return arrangement.switch(
                arrangement.calcPosition(rect.position, rectangles) { pos, r -> pos.x(rect.center.x - r.halfWidth) },
                arrangement.calcPosition(rect.position, rectangles) { pos, r -> pos.y(rect.center.y - r.halfHeight) }
            )
        }
    }

    class CenterRight(arrangement: Arrangement) : PlanarAlignment(arrangement) {
        override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
            val size = arrangement.contentSize(rectangles)
            val y = parent.center.y - size.halfHeight
            val x = parent.right - size.width
            val rect = rect(parent.position.xyz(x, y), size)
            return arrangement.switch(
                arrangement.calcPosition(rect.position, rectangles) { pos, r -> pos.x(rect.right - r.width) },
                arrangement.calcPosition(rect.position, rectangles) { pos, r -> pos.y(rect.center.y - r.halfHeight) }
            )
        }
    }

    class BottomLeft(arrangement: Arrangement) : PlanarAlignment(arrangement) {
        override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
            val size = arrangement.contentSize(rectangles)
            val y = parent.bottom - size.height
            val x = parent.left
            val rect = rect(parent.position.xyz(x, y), size)
            return arrangement.switch(
                arrangement.calcPosition(rect.position, rectangles),
                arrangement.calcPosition(rect.position, rectangles) { pos, r -> pos.y(rect.center.y - r.halfHeight) }
            )
        }
    }

    class BottomCenter(arrangement: Arrangement) : PlanarAlignment(arrangement) {
        override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
            val size = arrangement.contentSize(rectangles)
            val y = parent.bottom - size.height
            val x = parent.center.x - size.halfWidth
            val rect = rect(parent.position.xyz(x, y), size)
            return arrangement.switch(
                arrangement.calcPosition(rect.position, rectangles) { pos, r -> pos.x(rect.center.x - r.halfWidth) },
                arrangement.calcPosition(rect.position, rectangles) { pos, r -> pos.y(rect.center.y - r.halfHeight) }
            )
        }
    }

    class BottomRight(arrangement: Arrangement) : PlanarAlignment(arrangement) {
        override fun align(parent: Rectangle<Vector3<Float>>, rectangles: List<Rectangle<Vector3<Float>>>): List<Vector3<Float>> {
            val size = arrangement.contentSize(rectangles)
            val y = parent.bottom - size.height
            val x = parent.right - size.width
            val rect = rect(parent.position.xyz(x, y), size)
            return arrangement.switch(
                arrangement.calcPosition(rect.position, rectangles) { pos, r -> pos.x(rect.right - r.width) },
                arrangement.calcPosition(rect.position, rectangles) { pos, r -> pos.y(rect.center.y - r.halfHeight) }
            )
        }
    }
}
