package moe.forpleuvoir.ibukigourd.gui.render.shape.box

import moe.forpleuvoir.ibukigourd.gui.render.Size
import moe.forpleuvoir.ibukigourd.gui.render.SizeFloat
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.render.math.toVector2fc
import moe.forpleuvoir.nebula.common.pick
import org.joml.Vector2fc
import org.joml.Vector3fc
import kotlin.math.max
import kotlin.math.min

interface Box : SizeFloat, Cloneable {

    val position: Vector2fc

    /**
     * [Box]的四个顶点
     * 0: 左上
     * 1: 左下
     * 2: 右下
     * 3: 右上
     *
     */
    val vertexes: Array<out Vector2fc>

    val topLeft: Vector2fc get() = vertexes[0]

    val bottomLeft: Vector2fc get() = vertexes[1]

    val bottomRight: Vector2fc get() = vertexes[2]

    val topRight: Vector2fc get() = vertexes[3]

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

    val center: Vector2fc get() = Vector2f(x + this.width / 2, y + this.height / 2)

    val exist: Boolean get() = this.width > 0 && this.height > 0

    fun inBox(target: Box, completeInside: Boolean): Boolean {
        if (!this.exist || !target.exist) return false
        return if (completeInside) {
            this.x - target.x >= 0
            && target.endX - this.endX >= 0
            && this.y - target.y >= 0
            && target.endY - this.endY >= 0
        } else {
            this.intersectWith(target).exist
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
     * 判断两个[Box]是否相交
     * @param target 目标[Box]
     * @return [Box] 相交时返回交集[Box]，否则返回不存在的[Box.NULL]
     */
    infix fun intersectWith(target: Box): Box {
        if (!this.exist || !target.exist) return NULL
        val startX = max(this.x, target.x)
        val startY = max(this.y, target.y)
        val endX = min(this.endX, target.endX)
        val endY = min(this.endY, target.endY)
        Box(startX, startY, endX, endY).let {
            return if (it.exist) it else NULL
        }
    }

    @Suppress("DuplicatedCode")
    companion object {

        val NULL = Box(0, 0, 0, 0)

        operator fun invoke(position: Vector2fc, width: Float, height: Float): Box = BoxImpl(position, width, height)

        operator fun invoke(position: Vector3fc, width: Float, height: Float): Box = BoxImpl(position.toVector2fc(), width, height)

        operator fun invoke(position: Vector2fc, width: Number, height: Number): Box = BoxImpl(position, width.toFloat(), height.toFloat())

        operator fun invoke(position: Vector3fc, width: Number, height: Number): Box = BoxImpl(position.toVector2fc(), width.toFloat(), height.toFloat())

        operator fun invoke(x: Number, y: Number, width: Number, height: Number): Box = BoxImpl(x, y, width, height)

        operator fun invoke(startX: Float, startY: Float, endX: Float, endY: Float): Box =
            BoxImpl(startX, startY, endX - startX, endY - startY)

        operator fun invoke(x: Number, y: Number, size: Size<Number>): Box = BoxImpl(x, y, size)

        operator fun invoke(position: Vector2fc, size: Size<Float>): Box = BoxImpl(position, size)

        operator fun invoke(position: Vector3fc, size: Size<Float>): Box = BoxImpl(position.toVector2fc(), size)

        /**
         * 只判断矩形的位置与大小
         * @param box Rectangle
         * @param other Rectangle
         * @return Boolean
         */
        fun equals(box: Box, other: Box): Boolean {
            if (box.position != other.position) return false
            return Size.equals(box, other)
        }

        fun intersection(rects: Iterable<Box>): Box {
            var temp = rects.first()
            rects.forEach {
                if (!equals(it, rects.first())) {
                    temp = it intersectWith temp
                }
                if (!temp.exist) return NULL
            }
            return temp
        }
    }

}


