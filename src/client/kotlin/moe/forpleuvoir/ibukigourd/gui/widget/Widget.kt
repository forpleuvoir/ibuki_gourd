package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.render
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor

fun WidgetContainerScope.Widget(
    modifier: Modifier,
    scope: WidgetScope.() -> Unit = {}
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

fun WidgetContainerScope.Rect(
    color: State<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = { }
) = Widget(Modifier.render { context, _, _, _ ->
    context.renderBox(transform.asWorldCoordinateBox, color.getValue())
} then modifier, scope)

fun WidgetContainerScope.Rect(
    color: ARGBColor,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = { }
) = Rect(stateOf(color), modifier, scope)

fun WidgetContainerScope.ColoredBox(
    color: ARGBColor,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = { }
) = ColoredBox(stateOf(color), modifier, scope)

fun WidgetContainerScope.ColoredBox(
    color: State<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = { }
) = Widget(Modifier.render { context, _, _, _ ->
    context.useScissor(transform.asWorldCoordinateBox) {
        batchRenderTextureColored {
            pushTileTexture(transform, WidgetTextures.ALPHA)
        }
    }
    context.renderBox(transform.asWorldCoordinateBox, color.getValue())
} then modifier, scope)