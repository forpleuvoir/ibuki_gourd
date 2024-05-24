package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.MeasureSpec

@GuiDslMark
interface Layout {

    val element: () -> Element

    /**
     * 布局子元素
     */
    fun layout(widthMeasureSpec: MeasureSpec, heightMeasureSpec: MeasureSpec)

}