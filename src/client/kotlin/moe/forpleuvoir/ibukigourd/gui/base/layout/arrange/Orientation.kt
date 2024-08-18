package moe.forpleuvoir.ibukigourd.gui.base.layout.arrange

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.render.math.copy
import moe.forpleuvoir.nebula.common.util.primitive.sumOf
import org.joml.Vector2fc

sealed interface Orientation {

    data object Vertical : Orientation {

        override fun contentSize(sizes: List<Size<Float>>): Size<Float> =
            Size(sizes.maxOf { it.width }, sizes.sumOf { it.height })

        override fun mapPositions(position: Vector2fc, sizes: List<Size<Float>>): List<Vector2fc> {
            return buildList {
                var y = position.y()
                for (rectangle in sizes) {
                    add(position.copy(y = y))
                    y += rectangle.height
                }
            }
        }
    }

    data object Horizontal : Orientation {

        override fun contentSize(sizes: List<Size<Float>>): Size<Float> =
            Size(sizes.sumOf { it.width }, sizes.maxOf { it.height })

        override fun mapPositions(position: Vector2fc, sizes: List<Size<Float>>): List<Vector2fc> {
            return buildList {
                var x = position.x()
                for (rectangle in sizes) {
                    add(position.copy(x = x))
                    x += rectangle.width
                }
            }
        }
    }

    /**
     * 排列之后的总大小
     * @param sizes [List]<[Box]>
     * @return [Size]<[Float]>
     */
    fun contentSize(sizes: List<Size<Float>>): Size<Float>

    /**
     * 计算排列之后的每一个元素的位置
     * @param position [Vector2fc]
     * @param sizes [List]<[Box]>
     */
    fun mapPositions(position: Vector2fc, sizes: List<Size<Float>>): List<Vector2fc>

    /**
     * 使用提供的转换函数计算给定位置的新位置。
     * @param position 要转换的原始位置。类型必须为 [Vector2fc]。
     * @param sizes 用于映射每个位置的[Size]列表。类型必须为 [List]<[Size]<[Float]>>。
     * @param map 转换函数。接受位置和矩形，返回新位置。类型必须为 ([Vector2fc], [Box]) -> [Vector2fc]
     * @return 转换后的新位置列表。每个新位置的类型都是 [Vector2fc]。
     */
    fun mapPositions(
        position: Vector2fc,
        sizes: List<Size<Float>>,
        map: (Vector2fc, Size<Float>) -> Vector2fc
    ): List<Vector2fc> {
        return buildList {
            mapPositions(position, sizes).forEachIndexed { index, pos ->
                add(map(pos, sizes[index]))
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