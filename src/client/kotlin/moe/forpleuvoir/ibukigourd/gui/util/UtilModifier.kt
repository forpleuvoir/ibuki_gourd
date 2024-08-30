package moe.forpleuvoir.ibukigourd.gui.util

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.render
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.renderBackground
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.renderOverlay
import moe.forpleuvoir.ibukigourd.gui.base.widget.wasMouseOver
import moe.forpleuvoir.nebula.common.color.ARGBColor

fun Modifier.renderHoveredOutlineBox(color: ARGBColor) = this.renderBackground { ctx, x, y, delta ->
    this.onRenderBackground(ctx, x, y, delta)
    wasMouseOver {
        ctx.batchRenderBox {
            pushBoxOutline(transform, color)
        }
    }
}

fun Modifier.disableRenderBackground() = this.renderBackground { _, _, _, _ -> }

fun Modifier.disableRender() = this.render { _, _, _, _ -> }

fun Modifier.disableRenderOverlay() = this.renderOverlay { _, _, _, _ -> }
