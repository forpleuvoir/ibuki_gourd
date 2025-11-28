package moe.forpleuvoir.ibukigourd.gui.modifier

import moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.WidgetModifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidget
import moe.forpleuvoir.ibukigourd.util.math.bezier.Ease
import moe.forpleuvoir.ibukigourd.util.math.bezier.Easing.Companion.LINEAR
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors

fun Modifier.bgHoverHighlightBox(
    round: Int = 2,
    boxSupplier: GuiWidget.() -> Box = { this.transform.asWorldCoordinateBox },
    colorRange: Pair<ARGBColor, ARGBColor> = Colors.CYAN.alpha(0f) to Colors.CYAN.alpha(0.25f),
    upTick: Float = 6.66f,
    downTick: Float = 4f,
    upEscape: Ease = LINEAR::easeIn,
    downEscape: Ease = LINEAR::easeOut,
) = this then modifier(round, boxSupplier, colorRange, upTick, downTick, upEscape, downEscape) { this.renderBackground = it }

fun Modifier.hoverHighlightBox(
    round: Int = 2,
    boxSupplier: GuiWidget.() -> Box = { this.transform.asWorldCoordinateBox },
    colorRange: Pair<ARGBColor, ARGBColor> = Colors.CYAN.alpha(0f) to Colors.CYAN.alpha(0.25f),
    upTick: Float = 6.66f,
    downTick: Float = 4f,
    upEscape: Ease = LINEAR::easeIn,
    downEscape: Ease = LINEAR::easeOut,
) = this then modifier(round, boxSupplier, colorRange, upTick, downTick, upEscape, downEscape) { this.render = it }

fun Modifier.overlayHoverHighlightBox(
    round: Int = 2,
    boxSupplier: GuiWidget.() -> Box = { this.transform.asWorldCoordinateBox },
    colorRange: Pair<ARGBColor, ARGBColor> = Colors.CYAN.alpha(0f) to Colors.CYAN.alpha(0.25f),
    upTick: Float = 6.66f,
    downTick: Float = 4f,
    upEscape: Ease = LINEAR::easeIn,
    downEscape: Ease = LINEAR::easeOut,
) = this then modifier(round, boxSupplier, colorRange, upTick, downTick, upEscape, downEscape) { this.renderOverlay = it }

private fun modifier(
    round: Int,
    boxSupplier: GuiWidget.() -> Box,
    colorRange: Pair<ARGBColor, ARGBColor>,
    upTick: Float,
    downTick: Float,
    upEscape: Ease,
    downEscape: Ease,
    renderFunction: GuiWidget.((guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) -> Unit) -> Unit
): WidgetModifier = WidgetModifier {
    val (startColor, endColor) = colorRange
    val upt = 1f / upTick
    val dnt = 1f / downTick
    var fraction = 0f
    renderFunction.invoke(it) { context, mouseX, mouseY, delta ->
        fraction = if (it.wasMouseOver) {
            fraction + delta * upt
        } else {
            fraction - delta * dnt
        }.coerceIn(0f..1f)
        context.batchRenderBox {
            pushRoundBox(
                boxSupplier(it),
                startColor.lerp(endColor, (if (it.wasMouseOver) upEscape(fraction) else downEscape(fraction)).coerceIn(0f..1f)),
                round
            )
        }
    }
}