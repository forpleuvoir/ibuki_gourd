package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect

interface Layout {

    val elementContainer: () -> ElementContainer

    var spacing: Float

    /**
     * 排列子元素
     * @param elements List<Element>
     * @param margin Margin
     * @param padding Padding
     * @return [Size] 排列完元素之后计算出的高度和宽度,如果为空则没有任何元素参与排列
     */
    fun arrange(elements: List<Element>, margin: Margin, padding: Margin): Size<Float>?

    fun alignRects(elements: List<Element>, arrangement: Arrangement): List<Rectangle<Vector3<Float>>> {
        val alignElements = elements.filter { !it.fixed }
        val container = elementContainer()
        val content = container.contentRect(false)
        if (container.transform.fixedWidth) {
            container.remainingWidth = content.width
        }
        if (container.transform.fixedHeight) {
            container.remainingHeight = content.height
        }

        return alignElements.mapIndexed { index, element ->
            var spacing = spacing
            if (alignElements.lastIndex == index) {
                spacing = 0f
            }
            element.widthDimensionMode.resizeWidth(element, element.parent())
            element.heightDimensionMode.resizeHeight(element, element.parent())
            rect(
                vertex(0f, 0f, element.transform.z), arrangement.switch(
                    Size.create(element.transform.width + element.margin.width, element.transform.height + element.margin.height + spacing),
                    Size.create(element.transform.width + element.margin.width + spacing, element.transform.height + element.margin.height)
                )
            ).also { rect ->
                arrangement.switch({
                    if (container.transform.fixedHeight) {
                        container.remainingHeight -= rect.height
                    }
                }, {
                    if (container.transform.fixedWidth) {
                        container.remainingWidth -= rect.width
                    }
                })
            }
        }
    }

}