package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.shape.rectangle.Rectangle
import moe.forpleuvoir.nebula.common.api.Initializable

interface ElementContainer : Initializable {

    var init: () -> Unit

    /**
     * 基础属性变换
     */
    val transform: Transform

    var width: ElementDimension

    var height: ElementDimension


    /**
     * 此方法应该由父元素调用
     * 测量自身的尺寸
     * @param elementMeasureDimension ElementMeasureDimension 父元素的测量尺寸
     * @return Size<Float>
     */
    fun measure(elementMeasureDimension: ElementMeasureDimension): ElementMeasureDimension

    /**
     * 对子元素进行排列
     */
    fun layout()

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

    /**
     * 内容矩形
     * @param isWorld Boolean
     * @return Rectangle
     */
    fun contentRect(isWorld: Boolean): Rectangle<Vector3<Float>>

}