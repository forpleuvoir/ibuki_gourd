package moe.forpleuvoir.ibukigourd.gui.base.element

/**
 * 只有参与排序的元素才会才会被重新设置大小
 * @constructor
 */
sealed class ElementDimension {

    abstract fun resizeWidth(element: Element, parent: Element)

    abstract fun resizeHeight(element: Element, parent: Element)
}

val Number.f get() = Fixed(this.toFloat())

data class Fixed(val value: Float) : ElementDimension() {
    override fun resizeWidth(element: Element, parent: Element) {
        element.transform.width = value
    }

    override fun resizeHeight(element: Element, parent: Element) {
        element.transform.height = value
    }

}

/**
 * 会根据内容自动调整大小
 * @param default Float 如果没有内容，则使用默认值,如果默认值为空则使用元素的Padding
 */
data class WrapContent(val default: Float? = null) : ElementDimension() {

    override fun resizeWidth(element: Element, parent: Element) = Unit
    override fun resizeHeight(element: Element, parent: Element) = Unit

}

data object MatchParent : ElementDimension() {
    override fun resizeWidth(element: Element, parent: Element) {
        element.transform.width = parent.contentRect(false).width
    }

    override fun resizeHeight(element: Element, parent: Element) {
        element.transform.height = parent.contentRect(false).height
    }

}

data object FillRemainingSpace : ElementDimension() {

    override fun resizeWidth(element: Element, parent: Element) {
        element.transform.width = parent.remainingWidth
    }

    override fun resizeHeight(element: Element, parent: Element) {
        element.transform.height = parent.remainingHeight
    }

}

/**
 *
 */
val Number.w get() = Weight(this.toFloat())

data class Weight(val weight: Float) : ElementDimension() {
    override fun resizeWidth(element: Element, parent: Element) {
        TODO("Not yet implemented")
    }

    override fun resizeHeight(element: Element, parent: Element) {
        TODO("Not yet implemented")
    }

}
