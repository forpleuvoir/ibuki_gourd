package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.layout.Layout
import moe.forpleuvoir.ibukigourd.render.base.MutableSize
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.Rectangle
import moe.forpleuvoir.nebula.common.api.Initializable

interface ElementContainer : Initializable {

    var init: () -> Unit

    /**
     * 基础属性变换
     */
    val transform: Transform

    var spacing: Float

    var widthDimensionMode: DimensionMode

    var heightDimensionMode: DimensionMode

    /**
     * 只有在排序时才有有用的数据,固定大小的元素才会有剩余空间
     */
    var remainingWidth: Float

    var remainingHeight: Float


    fun arrange()

    /**
     * 子元素
     */
    val elements: List<Element>

    val renderElements: List<Element>

    val fixedElements: List<Element>

    val handleElements: List<Element>

    fun <T : Element> addElement(element: T): T

    fun preElement(element: Element): Element?

    fun nextElement(element: Element): Element?

    fun elementIndexOf(element: Element): Int

    fun removeElement(element: Element): Boolean

    fun removeElement(index: Int)

    fun clearElements(predicate: (Element) -> Boolean)

    val margin: Margin

    val padding: Padding

    fun margin(margin: Number)

    fun margin(margin: Margin)

    fun margin(left: Number = this.margin.left, right: Number = this.margin.right, top: Number = this.margin.top, bottom: Number = this.margin.bottom)

    fun padding(padding: Number)

    fun padding(padding: Padding)

    fun padding(left: Number = this.padding.left, right: Number = this.padding.right, top: Number = this.padding.top, bottom: Number = this.padding.bottom)

    val layout: Layout

    /**
     * 内容矩形
     * @param isWorld Boolean
     * @return Rectangle
     */
    fun contentRect(isWorld: Boolean): Rectangle<Vector3<Float>>

}