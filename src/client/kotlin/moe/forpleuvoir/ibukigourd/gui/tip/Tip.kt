package moe.forpleuvoir.ibukigourd.gui.tip

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.element.Element

abstract class Tip(final override var parent: () -> Element, private val tipHandler: () -> TipHandler) : AbstractElement() {

    init {
        transform.parent = { parent().transform }
    }

    override var fixed: Boolean = true


    fun push(): Boolean = tipHandler().pushTip(this)


    fun pop(): Boolean = tipHandler().popTip(this)

}