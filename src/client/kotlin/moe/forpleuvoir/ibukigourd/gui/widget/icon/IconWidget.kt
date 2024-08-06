package moe.forpleuvoir.ibukigourd.gui.widget.icon

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.size
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

    private val aspectRatio: Float = iconTexture.width.toFloat() / iconTexture.height.toFloat()

    override fun measure(constraints: Constraints): Placeable {
        val (minWidth, maxWidth, minHeight, maxHeight) = this.constraints.constraint(constraints)
        val constraintsAspectRatio = maxWidth / maxHeight
        if (aspectRatio < constraintsAspectRatio) {
            transform.set((maxHeight * aspectRatio).coerceIn(minWidth, maxWidth), maxHeight)
        } else {
            transform.set(maxWidth, (maxWidth / aspectRatio).coerceIn(minHeight, maxHeight))
        }
        return this
    }

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
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
    val m = Modifier.size(texture.toFloat()) thenNullable modifier
    m.foldIn(Unit) { _, e ->
        e.tryApplyModify(this)
    }
}