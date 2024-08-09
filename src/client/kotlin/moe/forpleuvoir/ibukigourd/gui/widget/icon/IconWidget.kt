package moe.forpleuvoir.ibukigourd.gui.widget.icon

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors

class IconWidget(
    private val iconTexture: WidgetTexture,
    private val color: ARGBColor = Colors.WHITE
) : IGWidgetImpl() {


    override fun measure(constraints: Constraints): Placeable {
        val c = this.constraints.constraint(constraints)
        val width = iconTexture.width + padding.width
        val height = iconTexture.height + padding.height
        transform.set(width.coerceIn(c.widthRange), height.coerceIn(c.heightRange))
        return this
    }

    override fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        context.batchRenderTextureColored {
            context.drawWidgetTexture(contentBox(true), iconTexture, color)
        }
    }

}

fun GuiScope<out WidgetContainer>.icon(
    texture: WidgetTexture,
    color: ARGBColor = Colors.WHITE,
    modifier: Modifier? = null
) = owner().addWidgetChild(IconWidget(texture, color)) {
    modifier?.foldIn(Unit) { _, e ->
        e.tryApplyModify(this)
    }
}