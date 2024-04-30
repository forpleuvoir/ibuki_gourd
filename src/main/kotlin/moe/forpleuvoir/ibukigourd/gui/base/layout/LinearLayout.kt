package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.element.*
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Orientation


@Suppress("MemberVisibilityCanBePrivate")
open class LinearLayout(
    orientation: Orientation,
    override val element: () -> Element
) : Layout {

    var spacing: Float = 0f

    override fun layout() {
        val alignElements = elements.filter { !it.fixed }
        if (alignElements.isEmpty()) return

        val alignRects = alignRects(alignElements, alignment.arrangement)

        val container = element()

        val containerContentRect = container.contentRect(false)
        val size = alignment.arrangement.contentSize(alignRects)

        when {
            //如果为[MatchParent]则宽度已固定
            container.width == MatchParent -> {}

        }

        val contentRect = when {
            //固定高度和宽度
            container.width is Fixed && container.height is Fixed   -> {
                containerContentRect
            }
            //固定宽度 不固定高度
            container.width is Fixed && container.height !is Fixed  -> {
                Rect(containerContentRect.position, containerContentRect.width, size.height)
            }
            //不固定宽度 固定高度
            container.width !is Fixed && container.height !is Fixed -> {
                Rect(containerContentRect.position, size.width, containerContentRect.height)
            }
            //不固定宽度 不固定高度
            else                                                    -> {
                Rect(containerContentRect.position, size)
            }
        }
        alignment.align(contentRect, alignRects).forEachIndexed { index, vector3f ->
            val element = alignElements[index]
            element.transform.translateTo(vector3f + Vector3f(element.margin.left, element.margin.top))
            element.visible = element.transform.inBox(contentRect, false)
        }
        return Size.of(contentRect.width + padding.width, contentRect.height + padding.height)
    }

}
