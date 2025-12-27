package moe.forpleuvoir.ibukigourd.gui.base.render.shape.box

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat
import moe.forpleuvoir.ibukigourd.input.MousePosition
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.math.asVector2fc
import moe.forpleuvoir.nebula.common.util.primitive.either
import net.minecraft.client.gui.navigation.ScreenRectangle
import org.joml.Vector2fc
import org.joml.Vector2ic
import org.joml.Vector3fc
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

interface Box : SizeFloat, Cloneable {

    val position: Vector2fc

    /**
     * [Box]的四个顶点
     * - 0: 左上
     * - 1: 左下
     * - 2: 右下
     * - 3: 右上
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

    val center: Vector2fc get() = Vector2f(centerX, centerY)

    val centerX: Float get() = x + halfWidth

    val centerY: Float get() = y + halfHeight

    val exist: Boolean get() = this.width > 0 && this.height > 0

    val asScreenRectangle: ScreenRectangle get() = ScreenRectangle(this.x.toInt(), this.y.toInt(), this.width.roundToInt(), this.height.roundToInt())

    fun trimEdges(top: Float = 0f, bottom: Float = 0f, left: Float = 0f, right: Float = 0f): Box =
        Box(this.x + left, this.y + top, this.endX - right, this.endY - bottom)

    fun trimEdges(width: Float, height: Float): Box = trimEdges(height / 2, height / 2, width / 2, width / 2)

    fun trimEdges(size: Size<Float>): Box = trimEdges(size.width, size.height)

    fun trimEdges(size: Float): Box = trimEdges(size, size, size, size)

    fun expandEdges(top: Float, bottom: Float, left: Float, right: Float): Box =
        Box(this.x - left, this.y - top, this.endX + right, this.endY + bottom)

    fun expandEdges(width: Float, height: Float): Box = expandEdges(height / 2, height / 2, width / 2, width / 2)

    fun expandEdges(size: Size<Float>): Box = expandEdges(size.width, size.height)

    fun expandEdges(size: Float): Box = expandEdges(size, size, size, size)

    fun copy(x: Float = this.x, y: Float = this.y, width: Float = this.width, height: Float = this.height): Box =
        BoxImpl(x, y, width, height)

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
            y in this.top..this.bottom && x in this.left..this.right
        else false
    }

    operator fun contains(vector3fc: Vector3fc): Boolean {
        return this.exist.either(
            vector3fc.y() in this.top..this.bottom && vector3fc.x() in this.left..this.right,
            false
        )
    }

    operator fun contains(vector2fc: Vector2fc): Boolean {
        return this.exist.either(
            vector2fc.y() in this.top..this.bottom && vector2fc.x() in this.left..this.right,
            false
        )
    }

    operator fun contains(vector2fc: Vector2ic): Boolean {
        return this.exist.either(
            vector2fc.y().toFloat() in this.top..this.bottom && vector2fc.x().toFloat() in this.left..this.right,
            false
        )
    }

    operator fun contains(position: MousePosition): Boolean {
        return this.exist.either(
            position.y in this.top..this.bottom && position.x in this.left..this.right,
            false
        )
    }

    /**
     * 判断两个[Box]是否相交
     * @param target 目标[Box]
     * @return [Box] 相交时返回交集[Box]，否则返回不存在的[Box.Unspecified]
     */
    infix fun intersectWith(target: Box): Box {
        if (!this.exist || !target.exist) return Unspecified
        val startX = max(this.x, target.x)
        val startY = max(this.y, target.y)
        val endX = min(this.endX, target.endX)
        val endY = min(this.endY, target.endY)
        Box(startX, startY, endX, endY).let {
            return if (it.exist) it else Unspecified
        }
    }

    @Suppress("DuplicatedCode")
    companion object {

        val Unspecified = Box(0, 0, 0, 0)

        operator fun invoke(position: Vector2fc, width: Float, height: Float): Box = BoxImpl(position, width, height)

        operator fun invoke(position: Vector3fc, width: Float, height: Float): Box = BoxImpl(position.asVector2fc(), width, height)

        operator fun invoke(position: Vector2fc, width: Number, height: Number): Box = BoxImpl(position, width.toFloat(), height.toFloat())

        operator fun invoke(position: Vector3fc, width: Number, height: Number): Box = BoxImpl(position.asVector2fc(), width.toFloat(), height.toFloat())

        operator fun invoke(x: Number, y: Number, width: Number, height: Number): Box = BoxImpl(x, y, width, height)

        operator fun invoke(startX: Float, startY: Float, endX: Float, endY: Float): Box =
            BoxImpl(startX, startY, endX - startX, endY - startY)

        operator fun invoke(x: Number, y: Number, size: Size<out Number>): Box = BoxImpl(x, y, size)

        operator fun invoke(position: Vector2fc, size: Size<Float>): Box = BoxImpl(position, size)

        operator fun invoke(position: Vector3fc, size: Size<Float>): Box = BoxImpl(position.asVector2fc(), size)

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
                if (!temp.exist) return Unspecified
            }
            return temp
        }
    }

}


