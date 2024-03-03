package moe.forpleuvoir.ibukigourd.gui.base.element

/**
 * 只有参与排序的元素才会才会被重新设置大小
 *
 * 父元素为[WrapContent]时需要获取子元素的尺寸
 * - 所有子元素为[Fixed]时，直接获取尺寸
 *
 */
sealed interface ElementDimension

val Number.f get() = Fixed(this.toFloat())

/**
 * 固定尺寸
 * @param value Float
 * @constructor
 */
data class Fixed(val value: Float) : ElementDimension

/**
 * 会根据内容自动调整大小
 * @param default Float 如果没有内容，则使用默认值,如果默认值为空则使用元素的Padding
 */
data class WrapContent(val default: Float? = null) : ElementDimension

val wrap_content = WrapContent(null)

/**
 * 会根据父元素的大小自动调整大小
 */
data object MatchParent : ElementDimension

/**
 * 填充剩余空间
 */
data object FillRemainingSpace : ElementDimension

val Number.w get() = Weight(this.toFloat())

/**
 * 根据内容大小调整大小
 * @param weight Float
 * @constructor
 */
data class Weight(val weight: Float) : ElementDimension

val Number.p get() = Percentage(this.toFloat())

/**
 * 百分比,
 * @param value Float 0..1
 * @constructor
 */
data class Percentage(val value: Float) : ElementDimension {
    init {
        check(value in 0f..1f) { "Percentage value must be between 0 and 1" }
    }
}