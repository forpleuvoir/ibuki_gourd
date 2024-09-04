package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl

fun WidgetContainerScope.Widget(
    modifier: Modifier,
    scope: WidgetScope.() -> Unit
) = addWidgetChild(object : IGWidgetImpl() {
    override fun measure(constraints: Constraints): Placeable {
        val c = this.constraints.constraintAs(constraints)
        transform.set(padding.width.coerceIn(c.widthRange), padding.height.coerceIn(c.heightRange))
        return this
    }
}) {
    modifier.foldInApply()
    GuiScope { this }.scope()
}

