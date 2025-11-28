package moe.forpleuvoir.ibukigourd.gui.base.layout

@JvmInline
value class Bias private constructor(val value: Float?) {
    companion object {

        private const val DEFAULT_VALUE = 0f

        private val VALUE_RANGE = -1f..1f

        val Start = Bias(-1f)

        val Center = Bias(0f)

        val End = Bias(1f)

        fun of(value: Float? = null): Bias {
            return Bias(value?.coerceIn(VALUE_RANGE))
        }

    }

    fun getOrDefault(other: Float = DEFAULT_VALUE): Float {
        return value ?: other
    }

    fun getOrDefault(other: Bias): Float {
        return value ?: other.value ?: DEFAULT_VALUE
    }

}