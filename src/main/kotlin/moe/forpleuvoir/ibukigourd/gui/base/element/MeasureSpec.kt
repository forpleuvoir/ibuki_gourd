package moe.forpleuvoir.ibukigourd.gui.base.element

data class MeasureSpec(
    val mode: Mode,
    val value: Float
) {

    companion object {

        fun exactly(value: Float): MeasureSpec {
            return MeasureSpec(Mode.EXACTLY, value)
        }

        fun atMost(value: Float): MeasureSpec {
            return MeasureSpec(Mode.AT_MOST, value)
        }

        fun unspecified(value: Float): MeasureSpec {
            return MeasureSpec(Mode.UNSPECIFIED, value)
        }

    }


    enum class Mode {
        UNSPECIFIED,
        EXACTLY,
        AT_MOST
    }
}