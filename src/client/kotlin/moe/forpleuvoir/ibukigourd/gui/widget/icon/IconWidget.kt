package moe.forpleuvoir.ibukigourd.gui.widget.icon

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.TextureUVMapping
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors

class IconWidget(
    iconTexture: WidgetTexture,
    private var color: ARGBColor = Colors.WHITE
) : IGWidgetImpl() {

    private var changedRemeasure: Boolean = true

    var iconTexture: WidgetTexture = iconTexture
        set(value) {
            if (field != value) {
                val remeasure = (field as TextureUVMapping) != (value as TextureUVMapping) && changedRemeasure
                field = value
                if (remeasure) screen()?.remeasure()
            }
        }

    override fun measure(constraints: Constraints): Placeable {
        val c = this.constraints.constraintAs(constraints)
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

    companion object

    fun interface IconScope : GuiScope<IconWidget> {

        var changedRemeasure: Boolean
            get() = owner().changedRemeasure
            set(value) {
                owner().changedRemeasure = value
            }

        var color: ARGBColor
            get() = owner().color
            set(value) {
                owner().color = value
            }

    }

}

typealias IconScope = IconWidget.IconScope

fun GuiScope<out WidgetContainer>.icon(
    texture: WidgetTexture,
    color: ARGBColor = Colors.WHITE,
    modifier: Modifier = Modifier,
    scope: IconScope.() -> Unit = {}
) = addWidgetChild(IconWidget(texture, color)) {
    IconScope { this }.scope()
    modifier.foldInApply()
}