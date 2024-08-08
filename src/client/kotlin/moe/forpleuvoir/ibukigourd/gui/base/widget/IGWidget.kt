package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.DrawableElement
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.nebula.common.pick

interface IGWidget : DrawableElement, Measurable, Placeable {

    val transform: Transform

    var padding: Padding

    /**
     * 鼠标是否在组件中
     */
    val wasMouseOver: Boolean

    val mouseOverCursor: MouseCursor.Cursor

    /**
     * 组件是否在拖动中
     */
    val wasDragging: Boolean

    val contentWidth: Float get() = transform.width - padding.width

    val contentHeight: Float get() = transform.height - padding.height

    fun contentLeft(isWorldAxis: Boolean) =
        isWorldAxis.pick(transform.worldLeft, transform.left) + padding.left

    fun contentRight(isWorldAxis: Boolean) =
        isWorldAxis.pick(transform.worldRight, transform.right) + padding.right

    fun contentTop(isWorldAxis: Boolean) =
        isWorldAxis.pick(transform.worldTop, transform.top) + padding.top

    fun contentBottom(isWorldAxis: Boolean) =
        isWorldAxis.pick(transform.worldBottom, transform.bottom) + padding.bottom

    fun contentBox(isWorldAxis: Boolean): Box =
        Box(x = contentLeft(isWorldAxis), y = contentTop(isWorldAxis), width = contentWidth, height = contentHeight)


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