package moe.forpleuvoir.ibukigourd.gui.modifier

import moe.forpleuvoir.ibukigourd.gui.base.element.RenderPriority
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.addRenderLayer
import moe.forpleuvoir.ibukigourd.gui.base.widget.wasMouseOver
import moe.forpleuvoir.nebula.common.color.ARGBColor

fun Modifier.renderHoveredOutlineBox(color: ARGBColor) = addRenderLayer(RenderPriority.BACKGROUND + 1) { ctx, x, y, delta ->
    wasMouseOver {
        ctx.batchRenderBox {
            pushBoxOutline(transform, color)
        }
    }
}
