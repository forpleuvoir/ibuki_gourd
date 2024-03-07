@file:Suppress("DuplicatedCode", "unused")

package moe.forpleuvoir.ibukigourd.render.base.arrange

import moe.forpleuvoir.ibukigourd.render.shape.rectangle.Rectangle
import org.joml.Vector3fc

interface Alignment {

    /**
     * 计算对齐之后的位置信息
     * @param parent 外部矩形
     * @param rectangles 内部需要被对齐的矩形大小
     * @return [Vector3fc]对齐之后的位置信息,应该为内部矩形的左上角顶点
     */
    fun align(parent: Rectangle, rectangles: List<Rectangle>): List<Vector3fc>

    fun align(parent: Rectangle, rectangle: Rectangle): Vector3fc = align(parent, listOf(rectangle))[0]

}


