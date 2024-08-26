package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.layout.WrappedAbsoluteLayoutData
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.WidgetModifier

interface AbsoluteLayoutScope {

    fun Modifier.position(x: Float, y: Float) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is WrappedAbsoluteLayoutData -> it.parentData = parentData.copy(x = x, y = y)
            null                         -> it.parentData = WrappedAbsoluteLayoutData(x = x, y = y)
        }
    }

}