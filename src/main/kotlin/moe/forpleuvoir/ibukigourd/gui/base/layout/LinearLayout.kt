package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.element.*
import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.arrange.Orientation
import moe.forpleuvoir.ibukigourd.render.base.arrange.Orientation.Vertical
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.ibukigourd.render.shape.rectangle.Rect
import moe.forpleuvoir.ibukigourd.render.shape.rectangle.Rectangle
import moe.forpleuvoir.nebula.common.pick


@Suppress("MemberVisibilityCanBePrivate")
open class LinearLayout(
    width: ElementDimension = wrap_content,
    height: ElementDimension = wrap_content,
    var orientation: Orientation = Vertical,
) : AbstractElement(width, height) {

    var spacing: Float = 0f

    enum class Alignment {
        Start, Center, End
    }

    protected fun getDirection(element: Element): Alignment {
        return element.layoutData[LinearLayout::class].let {
            (it is Alignment).pick(it as Alignment, Alignment.Center)
        }
    }

    fun alignRects(elements: Collection<Element>): List<Rectangle<Vector3<Float>>> {
        val alignElements = elements.filter { !it.fixed }
        return alignElements.mapIndexed { index, element ->
            val spacing = (alignElements.lastIndex == index).pick(0f, this.spacing)
            Rect(
                vertex(0f, 0f, element.transform.z), orientation.peek(
                    Size.of(element.transform.width + element.margin.width, element.transform.height + element.margin.height + spacing),
                    Size.of(element.transform.width + element.margin.width + spacing, element.transform.height + element.margin.height)
                )
            )
        }
    }

    override fun layout() {
        val alignElements = elements.filter { !it.fixed }
        if (alignElements.isEmpty()) return

        val alignRects = alignRects(alignElements, alignment.arrangement)

        val container = elementContainer()

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
            element.visible = element.transform.inRect(contentRect, false)
        }
        return Size.of(contentRect.width + padding.width, contentRect.height + padding.height)
    }

    /**
     * 需要判断的所有情况
     * 自身为Fixed
     *
     *
     * @param elementMeasureDimension ElementMeasureDimension
     * @return ElementMeasureDimension
     */
    override fun measure(elementMeasureDimension: ElementMeasureDimension): ElementMeasureDimension {
        val (mWidth, mHeight) = elementMeasureDimension
        when (width) {
            is Fixed           -> measuredDimension.width = (width as Fixed).value
            FillRemainingSpace -> measuredDimension.width = mWidth.value
            MatchParent        -> measuredDimension.width = mWidth.value
            is Weight          -> measuredDimension.width = mWidth.value
            is Percentage      -> measuredDimension.width = mWidth.value * (width as Percentage).value
            is WrapContent     -> TODO("需要layout完成之后重新测量")
        }

        when (height) {
            is Fixed           -> measuredDimension.height = (height as Fixed).value
            FillRemainingSpace -> measuredDimension.height = mHeight.value
            MatchParent        -> measuredDimension.height = mHeight.value
            is Weight          -> measuredDimension.height = mHeight.value
            is Percentage      -> measuredDimension.height = mHeight.value * (height as Percentage).value
            is WrapContent     -> TODO("需要layout完成之后重新测量")
        }
        var (widthRemainingSpace, heightRemainingSpace) = 0f to 0f
        subElements.forEach {
            when (it.width) {
                FillRemainingSpace ->
                is Fixed           -> TODO()
                MatchParent        -> TODO()
                is Percentage      -> TODO()
                is Weight          -> TODO()
                is WrapContent     -> TODO()
            }
        }


        return orientation.peek(
            verticalMeasure(elementMeasureDimension),
            horizontalMeasure(elementMeasureDimension)
        )
    }

    fun verticalMeasure(elementMeasureDimension: ElementMeasureDimension): ElementMeasureDimension {
        var width: MeasureDimension
        var height: MeasureDimension
        if (layoutElements.all { it.width is Fixed }) {
            width = MeasureDimension(MeasureDimension.Mode.EXACTLY, layoutElements.maxOf { (it.width as Fixed).value })
        }

        if (layoutElements.all { it.height is Fixed }) {
            height = MeasureDimension(MeasureDimension.Mode.EXACTLY, layoutElements.maxOf { (it.height as Fixed).value })
        }

        return width with height
    }

    fun horizontalMeasure(elementMeasureDimension: ElementMeasureDimension): ElementMeasureDimension {

    }


}
