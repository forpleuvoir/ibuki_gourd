package moe.forpleuvoir.ibukigourd.gui.util

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.renderBackground
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.wasMouseOver
import moe.forpleuvoir.nebula.common.color.ARGBColor

fun Modifier.renderHoveredOutlineBox(color: ARGBColor) = this.renderBackground { ctx, x, y, delta ->
    this as IGWidget
    this.onRenderBackground(ctx, x, y, delta)
    wasMouseOver {
        ctx.batchRenderBox {
            pushBoxOutline(transform, color)
        }
    }
}