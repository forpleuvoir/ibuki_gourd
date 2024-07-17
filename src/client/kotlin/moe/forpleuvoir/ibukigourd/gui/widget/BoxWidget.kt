package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer

fun GuiScope<out WidgetContainer>.box(modifier: Modifier? = null) =
    owner().addWidgetChild(object : IGWidgetImpl() {
        override var constraints: Constraints = Constraints(0f, 0f, 0f, 0f)

        override fun measure(constraints: Constraints): Placeable {
            val (_, maxWidth, _, maxHeight) = this.constraints.constraint(constraints)
            transform.set(maxWidth, maxHeight)
            return this
        }

    }.apply {
        modifier?.foldIn(Unit) { _, e ->
            e.tryApplyModify(this)
        }
    })