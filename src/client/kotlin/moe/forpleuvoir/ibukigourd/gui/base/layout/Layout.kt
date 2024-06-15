package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.MeasureSpec

@moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
interface Layout {

    /**
     * 布局子元素
     */
    fun Element.layout()

    fun Element.measureWidth(measureSpec: MeasureSpec): Float

    fun Element.measureHeight(measureSpec: MeasureSpec): Float

}