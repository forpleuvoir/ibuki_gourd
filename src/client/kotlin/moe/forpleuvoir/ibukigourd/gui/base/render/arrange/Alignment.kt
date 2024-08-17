@file:Suppress("DuplicatedCode", "unused")

package moe.forpleuvoir.ibukigourd.gui.base.render.arrange

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import org.joml.Vector2fc

interface Alignment {

    /**
     * 计算对齐之后的位置信息
     * @param parent 外部矩形
     * @param children 内部需要被对齐的矩形
     * @return List<[Vector2fc]>对齐之后的位置信息,应该为矩形的左上角顶点
     */
    fun align(parent: Box, children: List<Size<Float>>): List<Vector2fc>

    fun align(parent: Box, child: Size<Float>): Vector2fc = align(parent, listOf(child))[0]

}


