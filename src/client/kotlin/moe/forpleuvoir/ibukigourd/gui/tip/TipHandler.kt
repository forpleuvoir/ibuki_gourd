package moe.forpleuvoir.ibukigourd.gui.tip

import moe.forpleuvoir.ibukigourd.gui.base.element.Element

interface TipHandler : Element {

    val tipList: Iterable<Tip>

    var maxTip: Int

    fun pushTip(tip: Tip): Boolean

    fun popTip(tip: Tip): Boolean

}