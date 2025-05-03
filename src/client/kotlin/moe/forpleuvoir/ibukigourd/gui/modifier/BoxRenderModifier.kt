package moe.forpleuvoir.ibukigourd.gui.modifier

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.WidgetModifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.nebula.common.color.ARGBColor

fun Modifier.renderBackgroundBox(
    color: ARGBColor,
    boxSupplier: IGWidget.() -> Box = { this.transform.asWorldCoordinateBox },
    round: Int = 0,
) = this then modifier(color, boxSupplier, round) { this.renderBackground = it }

fun Modifier.renderBox(
    color: ARGBColor,
    boxSupplier: IGWidget.() -> Box = { this.transform.asWorldCoordinateBox },
    round: Int = 0,
) = this then modifier(color, boxSupplier, round) { this.render = it }

fun Modifier.renderOverlayBox(
    color: ARGBColor,
    boxSupplier: IGWidget.() -> Box = { this.transform.asWorldCoordinateBox },
    round: Int = 0,
) = this then modifier(color, boxSupplier, round) { this.renderOverlay = it }

private fun modifier(
    color: ARGBColor,
    boxSupplier: IGWidget.() -> Box,
    round: Int,
    renderFunction: IGWidget.((context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit) -> Unit
): WidgetModifier = WidgetModifier {
    renderFunction.invoke(it) { context, mouseX, mouseY, delta ->
        context.batchRenderBox {
            pushRoundBox(boxSupplier(it), color, round)
        }
    }
}