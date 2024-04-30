package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.element.Element

@GuiDslMark
interface Layout {

    val element: () -> Element

    /**
     * 布局子元素
     */
    fun layout()

}