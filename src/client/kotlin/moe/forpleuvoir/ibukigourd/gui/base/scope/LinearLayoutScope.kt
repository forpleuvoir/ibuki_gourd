package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.layout.Gravity
import moe.forpleuvoir.ibukigourd.gui.base.layout.LinearLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.WrappedLinearLayoutData
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.WidgetModifier

@GuiDslMark
interface LinearLayoutScope {

    val linearLayout: LinearLayout

    fun spacing(spacing: Float) {
        linearLayout.spacing = spacing
    }

    fun Modifier.weight(weight: Int) = this then WidgetModifier {
        check(weight >= 0) { "weight must be >= 0" }
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

    /**
     * 填充至最大空间 如果为垂直布局则填充宽度,为水平布局则填充高度
     * @receiver Modifier
     * @return Modifier
     */
    fun Modifier.fill() = this then WidgetModifier {
        when (val parentData = it.parentData) {
            is WrappedLinearLayoutData -> it.parentData = parentData.copy(fill = true)
            null                       -> it.parentData = WrappedLinearLayoutData(fill = true)
        }
    }

}