package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.DrawableElement
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box

interface IGWidget : DrawableElement, Measurable, Placeable {

    val transform: Transform

    var padding: Padding

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

    //------------ Placeable ------------\\

    override var margin: Margin

    override val halfHeight: Float get() = transform.halfHeight
    override val halfWidth: Float get() = transform.halfWidth
    override val height: Float get() = transform.height
    override val width: Float get() = transform.width

    override fun placeAt(x: Float, y: Float, isWorldAxis: Boolean) {
        if (isWorldAxis) {
            transform.worldX = x
            transform.worldY = y
        } else {
            transform.x = x
            transform.y = y
        }
    }

}