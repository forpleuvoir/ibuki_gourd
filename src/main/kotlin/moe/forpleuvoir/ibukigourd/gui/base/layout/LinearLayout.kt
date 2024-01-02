package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementContainer
import moe.forpleuvoir.ibukigourd.render.base.Alignment
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.Arrangement.Vertical
import moe.forpleuvoir.ibukigourd.render.base.Dimension
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect

/**
 * 线性布局实现
 * @param elementContainer () -> ElementContainer
 * @param alignment Alignment
 * @constructor
 */
@Suppress("MemberVisibilityCanBePrivate")
class LinearLayout(
    override val elementContainer: () -> ElementContainer,
    private val alignment: Alignment = PlanarAlignment.Center(Vertical)
) : Layout {

    override var spacing: Float = 0f

    constructor(
        elementContainer: () -> ElementContainer,
        arrangement: Arrangement = Vertical,
        alignment: (Arrangement) -> Alignment = PlanarAlignment::Center
    ) : this(elementContainer, alignment(arrangement))

    override fun arrange(elements: List<Element>, margin: Margin, padding: Margin): Dimension<Float>? {
        val alignElements = elements.filter { !it.fixed }
        if (alignElements.isEmpty()) return null

        val alignRects = alignRects(alignElements, alignment.arrangement)

        val container = elementContainer()
        val containerContentRect = container.contentRect(false)
        val size = alignment.arrangement.contentSize(alignRects)
        val contentRect = when {
            //固定高度和宽度
            container.transform.fixedWidth && container.transform.fixedHeight  -> {
                containerContentRect
            }
            //固定宽度 不固定高度
            container.transform.fixedWidth && !container.transform.fixedHeight -> {
                rect(containerContentRect.position, containerContentRect.width, size.height)
            }
            //不固定宽度 固定高度
            !container.transform.fixedWidth && container.transform.fixedHeight -> {
                rect(containerContentRect.position, size.width, containerContentRect.height)
            }
            //不固定宽度 不固定高度
            else                                                               -> {
                rect(containerContentRect.position, size)
            }
        }
        alignment.align(contentRect, alignRects).forEachIndexed { index, vector3f ->
            val element = alignElements[index]
            element.transform.translateTo(vector3f + Vector3f(element.margin.left, element.margin.top))
            element.visible = element.transform.inRect(contentRect, false)
        }
        return Dimension.create(contentRect.width + padding.width, contentRect.height + padding.height)
    }

}
