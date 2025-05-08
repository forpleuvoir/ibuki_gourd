package moe.forpleuvoir.ibukigourd.gui.widget.icon

import moe.forpleuvoir.ibukigourd.gui.base.element.addDefaultLayer
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors

class IconWidget(
    iconTexture: WidgetTexture,
    private var color: ARGBColor = Colors.WHITE
) : IGWidgetImpl() {

    init {
        addDefaultLayer(::renderWidget)
    }

    private var remeasureOnChange: Boolean = true

    var iconTexture: WidgetTexture = iconTexture
        set(value) {
            if (field != value) {
                val remeasure = !(field.uSize == value.uSize && field.vSize == value.vSize) && remeasureOnChange
                field = value
                if (remeasure) remeasure()
            }
        }

    override fun measure(constraints: Constraints): Placeable {
        val c = this.constraints.constraintAs(constraints)
        val width = iconTexture.width + padding.width
        val height = iconTexture.height + padding.height
        transform.set(width.coerceIn(c.widthRange), height.coerceIn(c.heightRange))
        return this
    }

    fun renderWidget(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        context.batchRenderTextureColored {
            pushWidgetTexture(contentBox(true), iconTexture, color)
        }
    }

    fun interface IconScope : GuiScope<IconWidget> {

        var remeasureOnChange: Boolean
            get() = owner().remeasureOnChange
            set(value) {
                owner().remeasureOnChange = value
            }

        var color: ARGBColor
            get() = owner().color
            set(value) {
                owner().color = value
            }

        fun texture(iconTexture: WidgetTexture) {
            owner().iconTexture = iconTexture
        }

    }

}

typealias IconScope = IconWidget.IconScope

fun WidgetContainerScope.Icon(
    texture: WidgetTexture,
    color: ARGBColor = Colors.WHITE,
    modifier: Modifier = Modifier,
    scope: IconScope.() -> Unit = {}
) = addWidgetChild(IconWidget(texture, color)) {
    modifier.foldInApply()
    IconScope { this }.scope()
}

fun WidgetContainerScope.Icon(
    texture: MutableState<WidgetTexture>,
    color: ARGBColor = Colors.WHITE,
    modifier: Modifier = Modifier,
    scope: IconScope.() -> Unit = {}
) = addWidgetChild(IconWidget(texture.getValue(), color)) {
    modifier.foldInApply()
    IconScope { this }.apply {
        scope()
        texture.subscribe {
            texture(it)
        }
    }
}