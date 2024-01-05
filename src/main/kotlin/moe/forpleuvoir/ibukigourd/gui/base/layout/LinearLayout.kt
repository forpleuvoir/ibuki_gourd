package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.element.*
import moe.forpleuvoir.ibukigourd.render.base.Alignment
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.Arrangement.Vertical
import moe.forpleuvoir.ibukigourd.render.base.Dimension
import moe.forpleuvoir.ibukigourd.render.base.PlanarAlignment
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.graphics.rectangle.rect
import moe.forpleuvoir.nebula.common.pick

/**
 * 线性布局实现
 * @param elementContainer () -> ElementContainer
 * @param alignment Alignment
 * @constructor
 */
@Suppress("MemberVisibilityCanBePrivate")
open class LinearLayout(
    var alignment: Alignment = PlanarAlignment.Center(Vertical)
) : AbstractElement() {

    override var spacing: Float = 0f

    fun alignRects(elements: Collection<Element>, arrangement: Arrangement): List<Rectangle<Vector3<Float>>> {
        val alignElements = elements.filter { !it.fixed }
        return alignElements.mapIndexed { index, element ->
            val spacing = (alignElements.lastIndex == index).pick(0f, this.spacing)
            rect(
                vertex(0f, 0f, element.transform.z), arrangement.choose(
                    Dimension.create(element.transform.width + element.margin.width, element.transform.height + element.margin.height + spacing),
                    Dimension.create(element.transform.width + element.margin.width + spacing, element.transform.height + element.margin.height)
                )
            )
        }
    }

    override fun layout(elements: Collection<Element>, margin: Margin, padding: Margin): Dimension<Float> {
        val alignElements = elements.filter { !it.fixed }
        if (alignElements.isEmpty()) return Dimension.create(0f, 0f)

        val alignRects = alignRects(alignElements, alignment.arrangement)

        val container = elementContainer()

        val containerContentRect = container.contentRect(false)
        val size = alignment.arrangement.contentSize(alignRects)

        when{
            //如果为[MatchParent]则宽度已固定
            container.width ==MatchParent->{}

        }

        val contentRect = when {
            //固定高度和宽度
            container.width is Fixed && container.height is Fixed  -> {
                containerContentRect
            }
            //固定宽度 不固定高度
            container.width is Fixed && container.height !is Fixed -> {
                rect(containerContentRect.position, containerContentRect.width, size.height)
            }
            //不固定宽度 固定高度
            container.width !is Fixed && container.height !is Fixed  -> {
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

    override fun onMeasured(width: MeasureDimension, height: MeasureDimension) {

    }

}
