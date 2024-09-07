package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderGradientBox
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.render
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.client.render.RenderLayer

fun WidgetContainerScope.ColorPicker() {}

fun WidgetContainerScope.ARGBColorPicker(
    colorState: State<ARGBColor>,
    modifier: Modifier = Modifier,
) {
    Row {


    }
}

fun WidgetContainerScope.RedColorSlider(
    colorState: State<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = ColorComponentSlider(
    colorState,
    colorComponentGetter = { it.redF },
    colorComponentSetter = { c, i -> Color(c.argb).red(i) },
    renderColorComponentSetter = { c, i -> Color(c.argb).red(i).alpha(255) },
    modifier, scope
)

fun WidgetContainerScope.GreenColorSlider(
    colorState: State<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = ColorComponentSlider(
    colorState,
    colorComponentGetter = { it.greenF },
    colorComponentSetter = { c, i -> Color(c.argb).green(i) },
    renderColorComponentSetter = { c, i -> Color(c.argb).green(i).alpha(255) },
    modifier, scope
)

fun WidgetContainerScope.BlueColorSlider(
    colorState: State<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = ColorComponentSlider(
    colorState,
    colorComponentGetter = { it.blueF },
    colorComponentSetter = { c, i -> Color(c.argb).blue(i) },
    renderColorComponentSetter = { c, i -> Color(c.argb).blue(i).alpha(255) },
    modifier, scope
)

fun WidgetContainerScope.AlphaColorSlider(
    colorState: State<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = ColorComponentSlider(
    colorState,
    colorComponentGetter = { it.alphaF },
    colorComponentSetter = { c, i -> Color(c.argb).alpha(i) },
    renderColorComponentSetter = { c, i -> Color(c.argb).alpha(i) },
    modifier, scope
)


fun WidgetContainerScope.ColorComponentSlider(
    colorState: State<ARGBColor>,
    colorComponentGetter: (ARGBColor) -> Float,
    colorComponentSetter: (ARGBColor, Float) -> ARGBColor,
    renderColorComponentSetter: (ARGBColor, Float) -> ARGBColor,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
): IGWidgetImpl {
    val valueState = stateOf(colorComponentGetter(colorState.getValue()))
    var progress = valueState.getValue().toDouble()
    State.bind(colorState, valueState, {
        colorComponentGetter(it).apply {
            progress = this.toDouble()
        }
    }, {
        colorComponentSetter(colorState.getValue(), it)
    })
    return FloatSlider(
        valueState,
        range = 0f..1f,
        valueMapper = {
            progress = it
            it.toFloat()
        },
        modifier = Modifier
            .render { context, _, _, _ ->
                colorComponentSliderRender(
                    context,
                    RenderLayer.getGui(),
                    progress,
                    colorState.getValue(),
                    renderColorComponentSetter(colorState.getValue(), 0f),
                    renderColorComponentSetter(colorState.getValue(), 1f)
                )
            }.then(modifier),
        scope = scope
    )
}

internal fun IGWidget.colorComponentSliderRender(
    context: IGDrawContext,
    layer: RenderLayer,
    progress: Double,
    color: ARGBColor,
    startColor: ARGBColor,
    endColor: ARGBColor
) {
    val box = transform.asWorldBox
    val bg = WidgetTextures.COLOR_SLIDER_BG
    val content = box.copy(box.x + bg.corner.left, box.y + bg.corner.top, box.width - bg.corner.width, box.height - bg.corner.height)
    context.useScissor(content) {
        batchRenderTextureColored {
            pushTileTexture(content, WidgetTextures.ALPHA, tileScale = 0.5f)
        }
    }
    context.renderGradientBox(content, startColor, endColor, layer = layer)
    context.batchRenderTextureColored {
        pushWidgetTexture(box, bg, color = color + Color(0, 0, 0, 255))
        pushWidgetTexture(
            box.copy(
                x = box.x + ((box.width - WidgetTextures.COLOR_SLIDER_ARROW.width.toFloat()) * progress.toFloat()),
                width = WidgetTextures.COLOR_SLIDER_ARROW.width.toFloat()
            ),
            WidgetTextures.COLOR_SLIDER_ARROW
        )
    }
}