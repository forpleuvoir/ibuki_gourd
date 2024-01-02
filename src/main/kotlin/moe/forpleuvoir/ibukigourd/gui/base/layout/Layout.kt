package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementDimension.Companion.MATCH_PARENT
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementDimension.Companion.WEIGHT
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.Dimension
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect
import moe.forpleuvoir.nebula.common.sumOf

interface Layout {

    val elementContainer: () -> ElementContainer

    var spacing: Float

    /**
     * 排列子元素
     * @param elements List<Element>
     * @param margin Margin
     * @param padding Padding
     * @return [Dimension] 排列完元素之后计算出的高度和宽度,如果为空则没有任何元素参与排列
     */
    fun arrange(elements: List<Element>, margin: Margin, padding: Margin): Dimension<Float>?

    fun alignRects(elements: List<Element>, arrangement: Arrangement): List<Rectangle<Vector3<Float>>> {
        val alignElements = elements.filter { !it.fixed }
        val enableWidthWeight = alignElements.all { it.dimension.width == WEIGHT }
        val widthWeight = alignElements.sumOf { it.dimension.widthWeight }
        val enableHeightWeight = alignElements.all { it.dimension.height == WEIGHT }
        val heightWeight = alignElements.sumOf { it.dimension.heightWeight }

        val content = elementContainer().contentRect(false)
        return alignElements.mapIndexed { index, element ->
            var spacing = spacing
            if (alignElements.lastIndex == index) {
                spacing = 0f
            }

            //如果元素的尺寸高度大于0,则为固定尺寸
            element.transform.fixedHeight = element.dimension.height > 0
            if (element.transform.fixedHeight) {
                element.transform.height = element.dimension.height

            } else if (element.dimension.height == MATCH_PARENT) {
                element.transform.height = content.height
            } else if (element.dimension.height == WEIGHT && enableHeightWeight) {
                element.transform.height = element.dimension.heightWeight / heightWeight * content.height
            }


            rect(
                vertex(0f, 0f, element.transform.z), arrangement.switch(
                    Dimension.create(element.transform.width + element.margin.width, element.transform.height + element.margin.height + spacing),
                    Dimension.create(element.transform.width + element.margin.width + spacing, element.transform.height + element.margin.height)
                )
            )
        }
    }

}