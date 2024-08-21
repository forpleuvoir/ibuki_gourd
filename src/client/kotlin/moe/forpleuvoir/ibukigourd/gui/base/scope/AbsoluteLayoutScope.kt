package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.WidgetModifier

interface AbsoluteLayoutScope {

    fun Modifier.position(x: Float, y: Float) = this then WidgetModifier {

    }

}