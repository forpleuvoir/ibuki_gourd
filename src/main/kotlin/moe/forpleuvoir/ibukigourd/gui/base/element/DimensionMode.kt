package moe.forpleuvoir.ibukigourd.gui.base.element

/**
 * 只有参与排序的元素才会才会被重新设置大小
 * @constructor
 */
sealed class DimensionMode {

    abstract fun resizeWidth(element: Element, parent: Element)

    abstract fun resizeHeight(element: Element, parent: Element)
}

val Number.f get() = Fixed(this.toFloat())

class Fixed(val value: Float) : DimensionMode() {
    override fun resizeWidth(element: Element, parent: Element) = Unit
    override fun resizeHeight(element: Element, parent: Element) = Unit

}

data object WrapContent : DimensionMode() {
    override fun resizeWidth(element: Element, parent: Element) = Unit
    override fun resizeHeight(element: Element, parent: Element) = Unit

}

data object MatchParent : DimensionMode() {
    override fun resizeWidth(element: Element, parent: Element) {
        element.transform.width = parent.contentRect(false).width
    }

    override fun resizeHeight(element: Element, parent: Element) {
        element.transform.height = parent.contentRect(false).height
    }

}

data object FillRemainingSpace : DimensionMode() {

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

class Weight(val weight: Float) : DimensionMode() {
    override fun resizeWidth(element: Element, parent: Element) {
        TODO("Not yet implemented")
    }

    override fun resizeHeight(element: Element, parent: Element) {
        TODO("Not yet implemented")
    }

}
