package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.BoxLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.BoxLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

class BoxWidget : WidgetContainerImpl(), BoxLayout

@JvmInline
value class BoxScope(private val boxWidget: BoxWidget) : GuiScope<BoxWidget>, BoxLayoutScope {
    override fun owner(): BoxWidget = boxWidget

}

fun GuiScope<out WidgetContainer>.box(
    modifier: Modifier? = null,
    context: (BoxScope.() -> Unit)? = null
): BoxWidget = owner().addWidgetChild(BoxWidget()) {
    context?.let { BoxScope(this).it() }
    modifier?.foldIn(Unit) { _, e ->
        e.tryApplyModify(this)
    }
}