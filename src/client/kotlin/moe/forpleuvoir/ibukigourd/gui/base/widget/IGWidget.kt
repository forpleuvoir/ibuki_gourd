package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.DrawableElement
import moe.forpleuvoir.ibukigourd.gui.base.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box

interface IGWidget : DrawableElement, Measurable {

    val transform: Transform

    var padding: Padding

    var margin: Margin

    /**
     * 鼠标是否在组件中
     */
    val wasMouseOver: Boolean

    /**
     * 组件是否在拖动中
     */
    val wasDragging: Boolean

    val contentWidth: Float get() = transform.width - padding.width

    val contentHeight: Float get() = transform.height - padding.height

    fun contentBox(isWorldAxis: Boolean): Box {
        val left = transform.left - padding.left
        val top = transform.top - padding.top
        val right = transform.right - padding.right
        val bottom = transform.bottom - padding.bottom
        return Box(left, top, right, bottom)
    }

}