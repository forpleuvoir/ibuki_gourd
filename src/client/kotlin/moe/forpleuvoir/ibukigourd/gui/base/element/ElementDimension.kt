package moe.forpleuvoir.ibukigourd.gui.base.element

/**
 * 只有参与布局的元素才会才会被重新设置大小
 *
 * 父元素为[WrapContent]时需要获取子元素的尺寸
 * - 所有子元素为[Fixed]时，直接获取尺寸
 */
sealed interface ElementDimension

sealed class WeightElementDimension(
    open val weight: Int
) : ElementDimension

/**
 * 固定尺寸
 * @param value Float
 * @constructor
 */
data class Fixed(val value: Float) : ElementDimension {
    init {
        check(value >= 0f) { "Value must be non-negative, was $value" }
    }
}

val Number.fixed get() = Fixed(this.toFloat())

/**
 * 会根据内容自动调整大小
 * @param default Float 如果没有内容，则使用默认值,如果默认值为空则使用元素的Padding
 * @param weight Int 权重,如果父元素无法确定其最大尺寸,则根据权重分配剩余空间
 */
data class WrapContent(val default: Float? = null, override val weight: Int) : WeightElementDimension(weight) {
    init {
        check(default == null || default >= 0f) { "Default must be non-negative, was $default" }
        check(weight > 0) { "Weight must be greater than 1, was $weight" }
    }
}

val wrap_content = WrapContent(null, 1)

/**
 * 匹配父元素尺寸,如果同一级有多个[MatchParent]的兄弟元素,则按权重[weight]分配比例
 */
data class MatchParent(override val weight: Int) : WeightElementDimension(weight) {
    init {
        check(weight > 0) { "Weight must be greater than 1, was $weight" }
    }
}

val match_parent get() = MatchParent(1)

data class Proportion(val proportion: Float) : ElementDimension {
    init {
        check(proportion in 0.0..1.0) {
            "proportion must be between 0.0 and 1.0"
        }
    }
}

val Number.proportion get() = Proportion(this.toFloat())

