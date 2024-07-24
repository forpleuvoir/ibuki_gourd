package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.layout.Gravity
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.WrappedLinearLayoutData
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.WidgetModifier

@GuiDslMark
fun interface LinearLayoutScope {

    fun layout(): LinearLayout

    fun Modifier.weight(weight: Int) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is WrappedLinearLayoutData -> it.parentData = parentData.copy(weight = weight)
            null                       -> it.parentData = WrappedLinearLayoutData(weight = weight)
        }
    }

    fun Modifier.gravity(gravity: Gravity) = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is WrappedLinearLayoutData -> it.parentData = parentData.copy(gravity = gravity)
            null                       -> it.parentData = WrappedLinearLayoutData(gravity = gravity)
        }
    }

    fun Modifier.gravityCenter() = gravity(Gravity.Center)

    fun Modifier.gravityStart() = gravity(Gravity.Start)

    fun Modifier.gravityEnd() = gravity(Gravity.End)


    fun Modifier.fill() = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is WrappedLinearLayoutData -> it.parentData = parentData.copy(fill = true)
            null                       -> it.parentData = WrappedLinearLayoutData(fill = true)
        }
    }

}