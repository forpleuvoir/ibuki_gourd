package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderAlignmentText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.render
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.render.IGRenderLayers
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.color.HSVColor
import moe.forpleuvoir.nebula.common.util.primitive.pick
import net.minecraft.client.render.RenderLayer


fun WidgetContainerScope.HueColorSlider(
    colorState: State<HSVColor>,
    modifier: Modifier = Modifier
) = HSVColorComponentSlider(
    colorState,
    colorComponentGetter = { it.hue / 360 },
    colorComponentSetter = { c, i -> c.clone().hue(i * 360) },
    renderColorComponentSetter = { c, i -> HSVColor().hue(i * 360).alpha(1f).toShaderColor() },
    true,
    modifier
)

fun WidgetContainerScope.SaturationColorSlider(
    colorState: State<HSVColor>,
    modifier: Modifier = Modifier
) = HSVColorComponentSlider(
    colorState,
    colorComponentGetter = { it.saturation },
    colorComponentSetter = { c, i -> c.clone().saturation(i) },
    renderColorComponentSetter = { c, i -> c.clone().saturation(i).alpha(1f) },
    false,
    modifier
)

fun WidgetContainerScope.ValueColorSlider(
    colorState: State<HSVColor>,
    modifier: Modifier = Modifier
) = HSVColorComponentSlider(
    colorState,
    colorComponentGetter = { it.value },
    colorComponentSetter = { c, i -> c.clone().value(i) },
    renderColorComponentSetter = { c, i -> c.clone().value(i).alpha(1f) },
    false,
    modifier
)

fun WidgetContainerScope.AlphaColorSlider(
    colorState: State<HSVColor>,
    modifier: Modifier = Modifier
) = HSVColorComponentSlider(
    colorState,
    colorComponentGetter = { it.alphaF },
    colorComponentSetter = { c, i -> c.clone().alpha(i) },
    renderColorComponentSetter = { c, i -> c.clone().alpha(i) },
    false,
    modifier
)

fun WidgetContainerScope.HSVColorComponentSlider(
    colorState: State<HSVColor>,
    colorComponentGetter: (HSVColor) -> Float,
    colorComponentSetter: (HSVColor, Float) -> HSVColor,
    renderColorComponentSetter: (HSVColor, Float) -> ARGBColor,
    isHue: Boolean,
    modifier: Modifier = Modifier,
): IGWidgetImpl {
    val valueState = stateOf(colorComponentGetter(colorState.getValue()))
    valueState.subscribe {
        colorState.setValue(colorComponentSetter(colorState.getValue(), it))
    }
    var progress = valueState.getValue().toDouble()
    return FloatSlider(
        valueState,
        range = 0f..1f,
        valueMapper = {
            progress = it
            it.toFloat()
        },
        modifier = Modifier.render { context, _, _, _ ->
            context.batchRenderBox(if (isHue) IGRenderLayers.positionHsvColor else RenderLayer.getGui()) {
                pushBoxOutline(transform, Colors.BLACK)
                pushGradientBox(
                    transform,
                    startColor = renderColorComponentSetter(colorState.getValue(), 0f),
                    endColor = renderColorComponentSetter(colorState.getValue(), 1f)
                )
                pushBox(transform.asWorldBox.copy(x = transform.worldX - 0.5f + (progress.toFloat() * transform.width), width = 1f), Colors.BLACK)
            }
            context.renderAlignmentText(
                String.format("%.2f", valueState.getValue() * isHue.pick(360f, 100f)) + "%",
                transform.asWorldBox,
                Alignment.Center,
                color = Colors.WHITE
            )
        }.then(modifier)
    ) {}
}


fun HSVColor.toShaderColor(): ARGBColor =
    Color(
        hue / 360f,
        this.saturation,
        this.value,
        this.alphaF
    )