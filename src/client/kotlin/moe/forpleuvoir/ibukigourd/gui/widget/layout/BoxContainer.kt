package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.BoxLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.render
import moe.forpleuvoir.ibukigourd.gui.base.scope.BoxLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.nebula.common.color.ARGBColor

class BoxWidget : WidgetContainerImpl(), BoxLayout {

    fun interface Scope : GuiScope<BoxWidget>, BoxLayoutScope

}

typealias BoxScope = BoxWidget.Scope

fun WidgetContainerScope.Box(
    modifier: Modifier = Modifier,
    context: BoxScope.() -> Unit = { }
): BoxWidget = addWidgetChild(BoxWidget()) {
    BoxScope { this }.context()
    modifier.foldInApply()
}

fun WidgetContainerScope.ColoredBox(
    color: ARGBColor,
    modifier: Modifier = Modifier,
    context: BoxScope.() -> Unit = { }
): BoxWidget = addWidgetChild(BoxWidget()) {
    BoxScope { this }.context()
    (Modifier.render { context, _, _, _ ->
        context.renderBox(transform.asWorldBox, color)
    } then modifier).foldInApply()
}

fun WidgetContainerScope.ColoredBox(
    color: State<ARGBColor>,
    modifier: Modifier = Modifier,
    context: BoxScope.() -> Unit = { }
): BoxWidget = addWidgetChild(BoxWidget()) {
    BoxScope { this }.context()
    (Modifier.render { context, _, _, _ ->
        context.renderBox(transform.asWorldBox, color.getValue())
    } then modifier).foldInApply()
}