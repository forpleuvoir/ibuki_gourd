package moe.forpleuvoir.ibukigourd.gui.base.modifier.widget

import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier

fun interface LayoutModifier : Modifier.Element {
    fun applyModify(element: LinearLayout)

    override fun tryApplyModify(target: Any) {
        if (target is LinearLayout) applyModify(target)
    }

}

