package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.render
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.render.IGRenderLayers
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.HSVColor

fun WidgetContainerScope.SaturationColorSlider(
    colorState: State<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = ColorComponentSlider(
    colorState,
    colorComponentGetter = { it.toHSVColor().saturation },
    colorComponentSetter = { c, i -> c.toHSVColor().clone().saturation(i) },
    renderColorComponentSetter = { c, i -> c.toHSVColor().clone().saturation(i).alpha(1f) },
    modifier, scope
)

fun WidgetContainerScope.ValueColorSlider(
    colorState: State<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
) = ColorComponentSlider(
    colorState,
    colorComponentGetter = { it.toHSVColor().value },
    colorComponentSetter = { c, i -> c.toHSVColor().clone().value(i) },
    renderColorComponentSetter = { c, i -> c.toHSVColor().clone().value(i).alpha(1f) },
    modifier, scope
)

fun WidgetContainerScope.HueColorSlider(
    colorState: State<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
): IGWidgetImpl {
    val valueState = stateOf(colorState.getValue().toHSVColor().hue / 360f)
    var progress = valueState.getValue().toDouble()
    State.bind(colorState, valueState, {
        (it.toHSVColor().hue / 360f).apply {
            progress = this.toDouble()
        }
    }, {
        colorState.getValue().toHSVColor().hue(it * 360f)
    })
    return FloatSlider(
        valueState,
        range = 0f..1f,
        valueMapper = {
            progress = it
            it.toFloat()
        },
        modifier = Modifier.render { context, _, _, _ ->
            colorComponentSliderRender(
                context,
                IGRenderLayers.positionHsvColor,
                progress,
                colorState.getValue(),
                HSVColor(hue = 0f).toShaderColor(),
                HSVColor().toShaderColor()
            )
        }.then(modifier),
        scope = scope
    )
}


fun HSVColor.toShaderColor(): ARGBColor =
    Color(
        hue / 360f,
        this.saturation,
        this.value,
        this.alphaF
    )

fun ARGBColor.toHSVColor(): HSVColor =
    if (this is HSVColor) this else HSVColor(this.argb)