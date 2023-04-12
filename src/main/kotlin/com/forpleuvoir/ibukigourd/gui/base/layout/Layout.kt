package com.forpleuvoir.ibukigourd.gui.base.layout

import com.forpleuvoir.ibukigourd.gui.base.Element
import com.forpleuvoir.ibukigourd.gui.base.Margin
import com.forpleuvoir.ibukigourd.gui.base.Padding

interface Layout {

    /**
     * 排列完之后计算出的宽度
     */
    val width: Float

    /**
     * 排列完之后计算出的高度
     */
    val height:Float

    /**
     * 排列子元素
     * @param elements List<Element>
     * @param margin Margin
     * @param padding Padding
     */
    fun arrange(elements: List<Element>, margin: Margin, padding: Padding)

}