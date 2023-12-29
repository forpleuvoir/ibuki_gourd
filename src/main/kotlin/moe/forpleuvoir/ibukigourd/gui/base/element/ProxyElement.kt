package moe.forpleuvoir.ibukigourd.gui.base.element

/**
 * 只有一个可切换的子元素,一般用于实现Tab选项卡的内容元素
 * @constructor
 */
class ProxyElement : AbstractElement() {

    var content: Element? = null
        set(value) {
            if (value == field) return
            if (value != null) {
                if (field != null) removeElement(field!!)
                addElement(value)
            } else if (field != null) {
                removeElement(field!!)
            }
            field = value
        }

    override fun <T : Element> addElement(element: T): T {
        return super.addElement(element)
    }

}