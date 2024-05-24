package moe.forpleuvoir.ibukigourd.gui.base.element

/**
 * 只有参与布局的元素才会才会被重新设置大小
 *
 * 父元素为[WrapContent]时需要获取子元素的尺寸
 * - 所有子元素为[Fixed]时，直接获取尺寸
 */
sealed interface ElementDimension {

    /**
     * 是否与父元素尺寸类型冲突
     * 例如父元素尺寸为[WrapContent]时子元素尺寸不能为[MatchParent]
     *
     * @param other ElementDimension
     * @return Boolean
     */
    fun conflictsParentDimension(other: ElementDimension): Boolean

}

val Number.fixed get() = Fixed(this.toFloat())

/**
 * 固定尺寸
 * @param value Float
 * @constructor
 */
data class Fixed(val value: Float) : ElementDimension {
    override fun conflictsParentDimension(other: ElementDimension): Boolean {
        return false
    }
}

/**
 * 会根据内容自动调整大小
 * @param default Float 如果没有内容，则使用默认值,如果默认值为空则使用元素的Padding
 */
data class WrapContent(val default: Float? = null) : ElementDimension {
    override fun conflictsParentDimension(other: ElementDimension): Boolean {
        return false
    }
}

val wrap_content = WrapContent(null)

/**
 * 会根据父元素的大小自动调整大小
 */
data object MatchParent : ElementDimension {
    override fun conflictsParentDimension(other: ElementDimension): Boolean {
        return other is WrapContent
    }
}

val match_parent = MatchParent

/**
 * 填充剩余空间
 */
data class FillRemainingSpace(val weight: Int) : ElementDimension {
    override fun conflictsParentDimension(other: ElementDimension): Boolean {
        return other is WrapContent
    }
}

val fill_remaining_space get() = FillRemainingSpace(1)

val Int.weight get() = Weight(this)

/**
 * 根据内容大小调整大小
 * @param weight Float
 * @constructor
 */
data class Weight(val weight: Int) : ElementDimension {
    override fun conflictsParentDimension(other: ElementDimension): Boolean {
        return other is WrapContent
    }
}

val Number.percent get() = Percentage(this.toFloat())

/**
 * 百分比,
 * @param value Float 0..1
 * @constructor
 */
data class Percentage(val value: Float) : ElementDimension {
    init {
        check(value in 0f..1f) { "Percentage value must be between 0 and 1" }
    }

    override fun conflictsParentDimension(other: ElementDimension): Boolean {
        return other is WrapContent
    }
}