package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.element.*
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Orientation


@Suppress("MemberVisibilityCanBePrivate")
open class LinearLayout(
    val orientation: Orientation,
    override val element: () -> Element
) : Layout {

    var spacing: Float = 0f

    override fun layout(widthMeasureSpec: MeasureSpec, heightMeasureSpec: MeasureSpec) {
        if (orientation == Orientation.Vertical) {
            element().apply {
                //measure
                var maxWidth = 0f
                var remainingWidth = 0f
                when (width) {
                    is Fixed, is FillRemainingSpace, MatchParent, is Percentage, is Weight -> {
                        transform.width = widthMeasureSpec.value
                        remainingWidth = transform.width
                    }

                    is WrapContent                                                         -> {
                        if (widthMeasureSpec.mode == MeasureSpec.Mode.EXACTLY) {
                            maxWidth = widthMeasureSpec.value
                            remainingWidth = maxWidth
                        }
                    }
                }

                val layoutElements = layoutElements

                val subElements = elements

                //填充剩余空间权重的每一份权重所有的值
                val remainingWidthWeightValue = remainingWidth / layoutElements.filter { it.width is FillRemainingSpace }.sumOf { (it.width as FillRemainingSpace).weight }

                subElements.forEach {
                    var width = MeasureSpec(MeasureSpec.Mode.AT_MOST, maxWidth)
                    var height = MeasureSpec(MeasureSpec.Mode.AT_MOST, maxWidth)
                    when (it.width) {
                        is FillRemainingSpace -> {
                            width = MeasureSpec.exactly((it.width as FillRemainingSpace).weight * remainingWidthWeightValue)
                        }

                        is Fixed              -> {
                            width = MeasureSpec.exactly((it.width as Fixed).value)
                        }

                        MatchParent           -> {
                            width = MeasureSpec.atMost(maxWidth)
                        }

                        is Percentage         -> {
                            width = MeasureSpec.exactly((it.width as Percentage).value * maxWidth)
                        }

                        is Weight             -> TODO()
                        is WrapContent        -> TODO()
                    }


                    onLayout(width, height)
                }


            }
        } else {

        }
    }

}
