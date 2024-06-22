package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.Margin

data class MeasureSpec(
    val mode: Mode,
    /**
     * 给到的值是包括了元素外边距的[Element.margin],使用时需要减去[Margin.width]
     * ```kotlin
     *   measureSpec.value - margin.width
     *
     * ```
     */
    val value: Float
) {

    companion object {

        fun exactly(value: Float): MeasureSpec {
            check(value >= 0) {
                "value must be greater than or equal to 0"
            }
            return MeasureSpec(Mode.EXACTLY, value)
        }

        fun atMost(value: Float): MeasureSpec {
            check(value >= 0) {
                "value must be greater than or equal to 0"
            }
            return MeasureSpec(Mode.AT_MOST, value)
        }
    }

    enum class Mode {
        EXACTLY,
        AT_MOST
    }

}