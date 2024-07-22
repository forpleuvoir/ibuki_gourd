package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.DrawableElement
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
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
        val x = if (isWorldAxis) transform.worldX + padding.left else transform.x + padding.left
        val y = if (isWorldAxis) transform.worldY + padding.top else transform.y + padding.top
        val width = transform.width - padding.width
        val height = transform.height - padding.height
        return Box(x = x, y = y, width = width, height = height)
    }

    //------------ Placeable ------------\\

    override val size: Size<Float>
        get() = transform

    override var margin: Margin

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