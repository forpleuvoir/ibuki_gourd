package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.WrappedLinearLayoutData
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.WidgetModifier

@GuiDslMark
fun interface LinearLayoutScope {

    fun layout(): LinearLayout

    fun Modifier.weight(weight: Int) = this then WidgetModifier {
        it.parentData = WrappedLinearLayoutData(weight)
    }


}