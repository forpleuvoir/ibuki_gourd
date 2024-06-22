package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.nebula.common.api.Initializable

interface ElementContainer : Initializable {

    var init: () -> Unit

    /**
     * 基础属性变换
     */
    val transform: moe.forpleuvoir.ibukigourd.gui.base.Transform

    var width: ElementDimension

    var height: ElementDimension

    /**
     * 对子元素进行布局并测量尺寸
     */
    fun onLayout()

    /**
     * 子元素
     */
    val elements: List<Element>

    val layoutElements: List<Element>

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

    var margin: Margin

    var padding: Padding

    /**
     * 内容矩形
     * @param isWorld Boolean
     * @return Rectangle
     */
    fun contentBox(isWorld: Boolean): Box

}