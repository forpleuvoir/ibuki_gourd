package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.BoxLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.BoxLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

class BoxWidget : WidgetContainerImpl(), BoxLayout {

    companion object {}

    data class BoxScope(private val boxWidget: BoxWidget) : GuiScope<BoxWidget>, BoxLayoutScope {
        override fun owner(): BoxWidget = boxWidget
    }

}

typealias BoxScope = BoxWidget.BoxScope

fun GuiScope<out WidgetContainer>.box(
    modifier: Modifier = Modifier,
    context: BoxScope.() -> Unit = { }
): BoxWidget = addWidgetChild(BoxWidget()) {
    BoxScope(this).context()
    modifier.foldInApply()
}