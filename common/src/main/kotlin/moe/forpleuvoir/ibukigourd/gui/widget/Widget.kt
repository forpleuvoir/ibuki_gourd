package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.renderBox
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.render
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor

fun ContainerScope.Widget(
    modifier: Modifier,
    scope: WidgetScope.() -> Unit = {}
) = addWidgetChild(object : GuiWidgetImpl() {
    override fun measure(constraints: Constraints): Placeable {
        val c = this.constraints.merge(constraints)
        transform.set(padding.width.coerceIn(c.widthRange), padding.height.coerceIn(c.heightRange))
        return this
    }
}) {
    modifier.foldInApply()
    GuiScope { this }.scope()
}

fun ContainerScope.Rect(
    color: State<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = { }
) = Widget(Modifier.render { context, _, _, _ ->
    context.renderBox(transform.asWorldCoordinateBox, color.getValue())
} then modifier, scope)

fun ContainerScope.Rect(
    color: ARGBColor,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = { }
) = Rect(stateOf(color), modifier, scope)

fun ContainerScope.ColoredBox(
    color: ARGBColor,
    bgTiledScale: Float,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = { }
) = ColoredBox(stateOf(color), bgTiledScale, modifier, scope)

fun ContainerScope.ColoredBox(
    color: State<ARGBColor>,
    bgTiledScale: Float,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = { }
) = Widget(Modifier.render { guiGraphics, _, _, _ ->
    val size = WidgetTextures.ALPHA.toFloat()
    val scale = bgTiledScale.coerceAtLeast(0.2f)
    guiGraphics {
        pushTiledBlit(transform, WidgetTextures.ALPHA, Size(size.width * scale, size.height * scale))
        pushBox(transform, color.getValue())
    }
} then modifier, scope)

fun ContainerScope.Canvas(
    modifier: Modifier = Modifier,
    render: GuiWidget.(IGGuiGraphics, Float, Float, Float) -> Unit
) = Widget(Modifier.then(modifier).render(render))