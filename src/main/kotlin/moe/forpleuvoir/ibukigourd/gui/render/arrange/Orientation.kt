package moe.forpleuvoir.ibukigourd.gui.render.arrange

import moe.forpleuvoir.ibukigourd.gui.render.Size
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.copy
import moe.forpleuvoir.nebula.common.sumOf
import org.joml.Vector2fc

sealed interface Orientation {

    open class Vertical private constructor() : Orientation {

        companion object : Vertical()

        override fun contentSize(boxes: List<Box>): Size<Float> =
            Size(boxes.maxOf { it.width }, boxes.sumOf { it.height })

        override fun calcPosition(position: Vector2fc, boxes: List<Box>): List<Vector2fc> {
            return buildList {
                var y = position.y()
                for (rectangle in boxes) {
                    add(position.copy(y = y))
                    y += rectangle.height
                }
            }
        }
    }

    open class Horizontal private constructor() : Orientation {

        companion object : Horizontal()

        override fun contentSize(boxes: List<Box>): Size<Float> =
            Size(boxes.sumOf { it.width }, boxes.maxOf { it.height })

        override fun calcPosition(position: Vector2fc, boxes: List<Box>): List<Vector2fc> {
            return buildList {
                var x = position.x()
                for (rectangle in boxes) {
                    add(position.copy(x = x))
                    x += rectangle.width
                }
            }
        }
    }

    /**
     * 排列之后的总大小
     * @param boxes [List]<[Box]>
     * @return [Size]<[Float]>
     */
    abstract fun contentSize(boxes: List<Box>): Size<Float>

    /**
     * 计算排列之后的每一个元素的位置
     * @param position Vector3<Float>
     * @param boxes List<Rectangle>
     */
    abstract fun calcPosition(position: Vector2fc, boxes: List<Box>): List<Vector2fc>

    /**
     * 使用提供的转换函数计算给定位置的新位置。
     * @param position 要转换的原始位置。类型必须为 Vector3<Float>。
     * @param boxes 用于映射每个位置的矩形列表。类型必须为 List<Rectangle<Vector3<Float>>>。
     * @param map 转换函数。接受位置和矩形，返回新位置。类型必须为 (Vector3fc, Rectangle) -> Vector3<Float>
     * @return 转换后的新位置列表。每个新位置的类型都是 Vector3<Float>。
     */
    fun calcPosition(
        position: Vector2fc,
        boxes: List<Box>,
        map: (Vector2fc, Box) -> Vector2fc
    ): List<Vector2fc> {
        return buildList {
            calcPosition(position, boxes).forEachIndexed { index, pos ->
                add(map(pos, boxes[index]))
            }
        }
    }

}

inline fun <R> Orientation.peek(vertical: (Orientation.Vertical) -> R, horizontal: (Orientation.Horizontal) -> R): R {
    return when (this) {
        is Orientation.Vertical   -> vertical(this)
        is Orientation.Horizontal -> horizontal(this)
    }
}

fun <R> Orientation.peek(vertical: R, horizontal: R): R {
    return when (this) {
        is Orientation.Vertical   -> vertical
        is Orientation.Horizontal -> horizontal
    }
}