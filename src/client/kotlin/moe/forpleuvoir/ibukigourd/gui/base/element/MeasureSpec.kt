package moe.forpleuvoir.ibukigourd.gui.base.element

data class MeasureSpec(
    val mode: Mode,
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