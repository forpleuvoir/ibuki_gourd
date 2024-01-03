package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.widget.text.TextBoxWidget

/**
 * 只有参与排序的元素才会才会被重新设置大小
 * @constructor
 */
sealed class DimensionMode {

    abstract fun resizeWidth(element: Element, parent: Element)

    abstract fun resizeHeight(element: Element, parent: Element)
}

data object None : DimensionMode() {
    override fun resizeWidth(element: Element, parent: Element) = Unit
    override fun resizeHeight(element: Element, parent: Element) = Unit

}

data object MatchParent : DimensionMode() {
    override fun resizeWidth(element: Element, parent: Element) {
        element.transform.width = parent.contentRect(false).width
        element.transform.fixedWidth = true
    }

    override fun resizeHeight(element: Element, parent: Element) {
        element.transform.height = parent.contentRect(false).height
        element.transform.fixedHeight = true
    }

}

data object FillRemainingSpace : DimensionMode() {
    override fun resizeWidth(element: Element, parent: Element) {
        if (element is TextBoxWidget) {
            parent.remainingWidth
            println("重新填充宽度")
        }
        element.transform.width = parent.remainingWidth
        element.transform.fixedWidth = true
    }

    override fun resizeHeight(element: Element, parent: Element) {
        if (element is TextBoxWidget) {
            parent.remainingHeight
        }
        element.transform.height = parent.remainingHeight
        element.transform.fixedHeight = true
    }

}

//class Weight(val weight: Float) : DimensionMode() {
//    override fun resizeWidth(element: Element, parent: Element) {
//        TODO("Not yet implemented")
//    }
//
//    override fun resizeHeight(element: Element, parent: Element) {
//        TODO("Not yet implemented")
//    }
//
//}
