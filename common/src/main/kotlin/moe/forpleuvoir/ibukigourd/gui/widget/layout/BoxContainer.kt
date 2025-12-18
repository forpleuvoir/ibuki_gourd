package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.BoxLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.BoxLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.widget.Compose
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetContainerImpl

open class BoxWidget : GuiWidgetContainerImpl(), BoxLayout

fun interface BoxScope : GuiScope<GuiWidgetContainer>, BoxLayoutScope

fun ContainerScope.Box(
    modifier: Modifier = Modifier,
    content: BoxScope.() -> Unit
): BoxWidget = addWidgetChild(BoxWidget()) {
    modifier.foldInApply()
    BoxScope { this }.Compose { content() }
}