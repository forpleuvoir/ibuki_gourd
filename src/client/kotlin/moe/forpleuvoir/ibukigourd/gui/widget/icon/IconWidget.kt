package moe.forpleuvoir.ibukigourd.gui.widget.icon

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors

class IconWidget(
    iconTexture: WidgetTexture,
    private val color: State<ARGBColor> = stateOf(Colors.WHITE)
) : IGWidgetImpl() {

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
        val c = this.constraints.merge(constraints)
        val width = iconTexture.width + padding.width
        val height = iconTexture.height + padding.height
        transform.set(width.coerceIn(c.widthRange), height.coerceIn(c.heightRange))
        return this
    }

    override fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        context.batchRenderTextureColored {
            pushWidgetTexture(contentBox(true), iconTexture, color.getValue())
        }
    }

    companion object

    fun interface IconScope : GuiScope<IconWidget> {

        var remeasureOnChange: Boolean
            get() = owner().remeasureOnChange
            set(value) {
                owner().remeasureOnChange = value
            }

        fun texture(iconTexture: WidgetTexture) {
            owner().iconTexture = iconTexture
        }

    }

}

typealias IconScope = IconWidget.IconScope


fun ContainerScope.Icon(
    texture: WidgetTexture,
    color: ARGBColor = Colors.WHITE,
    modifier: Modifier = Modifier,
    scope: IconScope.() -> Unit = {}
) = addWidgetChild(IconWidget(texture, stateOf(color))) {
    modifier.foldInApply()
    IconScope { this }.scope()
}

fun ContainerScope.Icon(
    texture: MutableState<WidgetTexture>,
    color: State<ARGBColor> = stateOf(Colors.WHITE),
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
