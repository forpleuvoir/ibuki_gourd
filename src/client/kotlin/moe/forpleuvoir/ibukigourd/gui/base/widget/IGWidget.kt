package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractDrawableElement
import moe.forpleuvoir.ibukigourd.gui.base.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box

/**
 * 所有组件的基类,实现任何组件都应该继承此类
 */
abstract class IGWidget : AbstractDrawableElement(), Measurable {

    //------------ IbukiGourd Widget ------------\\

    val transform: Transform = Transform()

    var padding: Padding = Padding(0)

    var margin: Margin = Margin(0)

    abstract val constraints: Constraints

    val contentWidth: Float get() = transform.width - padding.width

    val contentHeight: Float get() = transform.height - padding.height

    fun contentBox(isWorldAxis: Boolean): Box {
        val left = transform.left - padding.left
        val top = transform.top - padding.top
        val right = transform.right - padding.right
        val bottom = transform.bottom - padding.bottom
        return Box(left, top, right, bottom)
    }

    //------------ Measurable ------------\\

    override var parentData: Any? = null

    abstract override fun measure(constraints: Constraints): SizeFloat

    override fun minIntrinsicWidth(height: Float): Float = constraints.minWidth

    override fun maxIntrinsicWidth(height: Float): Float = constraints.maxWidth

    override fun minIntrinsicHeight(width: Float): Float = constraints.minHeight

    override fun maxIntrinsicHeight(width: Float): Float = constraints.maxHeight

}
