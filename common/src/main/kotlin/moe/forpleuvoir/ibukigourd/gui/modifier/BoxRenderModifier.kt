package moe.forpleuvoir.ibukigourd.gui.modifier

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.WidgetModifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidget
import moe.forpleuvoir.nebula.common.color.ARGBColor

fun Modifier.renderBackgroundBox(
    color: ARGBColor,
    boxSupplier: GuiWidget.() -> Box = { this.transform.asWorldCoordinateBox },
    round: Int = 0,
) = this then modifier(color, boxSupplier, round) { this.renderBackground = it }

fun Modifier.renderBox(
    color: ARGBColor,
    boxSupplier: GuiWidget.() -> Box = { this.transform.asWorldCoordinateBox },
    round: Int = 0,
) = this then modifier(color, boxSupplier, round) { this.render = it }

fun Modifier.renderOverlayBox(
    color: ARGBColor,
    boxSupplier: GuiWidget.() -> Box = { this.transform.asWorldCoordinateBox },
    round: Int = 0,
) = this then modifier(color, boxSupplier, round) { this.renderOverlay = it }

private fun modifier(
    color: ARGBColor,
    boxSupplier: GuiWidget.() -> Box,
    round: Int,
    renderFunction: GuiWidget.((guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) -> Unit) -> Unit
): WidgetModifier = WidgetModifier {
    renderFunction.invoke(it) { guiGraphics, mouseX, mouseY, delta ->
        guiGraphics.pushRoundBox(boxSupplier(it), color, round)
    }
}