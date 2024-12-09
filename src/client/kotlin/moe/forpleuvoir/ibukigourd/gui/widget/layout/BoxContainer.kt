package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.BoxLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.BoxLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.Compose
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

class BoxWidget : WidgetContainerImpl(), BoxLayout {

    fun interface Scope : GuiScope<BoxWidget>, BoxLayoutScope

}

typealias BoxScope = BoxWidget.Scope

fun WidgetContainerScope.Box(
    modifier: Modifier = Modifier,
    context: BoxScope.() -> Unit = { }
): BoxWidget = addWidgetChild(BoxWidget()) {
    modifier.foldInApply()
    Compose { BoxScope { this }.context() }
}