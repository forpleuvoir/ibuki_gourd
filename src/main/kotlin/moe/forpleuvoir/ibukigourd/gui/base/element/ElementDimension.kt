package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.render.base.MutableDimension

class ElementDimension(override var width: Float, override var height: Float) : MutableDimension<Float> {

    /**
     * 如果尺寸为以下值,则交给排列时[arrange]处理
     *
     * 大于0时为固定尺寸
     */
    companion object {
        /**
         * 适配子元素大小
         */
        const val WRAP_CONTENT = 0f

        /**
         * 父元素为固定尺寸生效,否则为[WRAP_CONTENT]
         *
         * 匹配父元素尺寸,不会忽略父元素的Padding
         */
        const val MATCH_PARENT = -1f

        /**
         * 父元素为固定尺寸生效,否则为[WRAP_CONTENT]
         *
         * 权重,会与其他元素的权重相比较,其他所有元素也必须为[WEIGHT]否则为[WRAP_CONTENT]
         */
        const val WEIGHT = -2f

        /**
         * 填充剩余空间,上一个元素计算完位置和大小之后,填充满父元素剩余的尺寸,如果没有可利用的尺寸则为[WRAP_CONTENT]
         */
        const val FULL_REMAIN = -3f
    }

    /**
     * 如果使用特殊值则返回0
     */
    override val halfWidth: Float
        get() = if (width > 0) width / 2 else 0f

    /**
     * 如果使用特殊值则返回0
     */
    override val halfHeight: Float
        get() = if (height > 0) height / 2 else 0f


    /**
     * 宽度权重
     */
    var widthWeight: Float = 0f

    /**
     * 高度权重
     */
    var heightWeight: Float = 0f

    fun set(width: Float, height: Float) {
        this.width = width
        this.height = height
    }

    fun wrapTransform(transform: Transform) {
        if (width > 0) {
            transform.width = width
        }
        if (height > 0) {
            transform.width = width
        }
    }


}
