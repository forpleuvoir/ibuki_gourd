package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.layout.Bias
import moe.forpleuvoir.ibukigourd.gui.base.layout.WrappedListLayoutData
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.WidgetModifier

@GuiDslMark
interface ListLayoutScope {

    fun Modifier.gravity(bias: Bias) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is WrappedListLayoutData -> it.parentData = parentData.copy(bias = bias)
            null                     -> it.parentData = WrappedListLayoutData(bias = bias)
        }
    }

    fun Modifier.gravityCenter() = gravity(Bias.Center)

    fun Modifier.gravityStart() = gravity(Bias.Start)

    fun Modifier.gravityEnd() = gravity(Bias.End)

    fun Modifier.fill() = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is WrappedListLayoutData -> it.parentData = parentData.copy(fill = true)
            null                     -> it.parentData = WrappedListLayoutData(fill = true)
        }
    }


}