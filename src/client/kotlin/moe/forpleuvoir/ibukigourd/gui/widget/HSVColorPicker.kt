package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope
import moe.forpleuvoir.ibukigourd.gui.widget.text.FloatEditor
import moe.forpleuvoir.ibukigourd.render.IGRenderLayers
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.HSVColor

fun WidgetContainerScope.HSVColorPicker(
    colorState: MutableState<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: RowScope.() -> Unit = {}
) = Row(
    modifier = Modifier.size(200f, 82f).then(modifier),
    verticalArrangement = Arrangement.spacedBy(2f, Alignment.CenterVertically),
) {
    Column(Modifier.weight(1)) {
        Box(
            modifier.padding(vertical = 4f).weight(1).margin(right = 2f)
        ) {
            HueColorSlider(colorState, modifier = Modifier.fill().align(Alignment.Center))
        }
        FloatEditor(
            mutableStateOf(colorState, { it.toHSVColor().hue }) { colorState.getValue().clone().toHSVColor().hue(it) },
            range = 0f..360f,
            modifier = Modifier.width(45f),
            editorModifier = { Modifier.weight(1) }
        )
    }
    Column(Modifier.weight(1)) {
        Box(
            modifier.padding(vertical = 4f).weight(1).margin(right = 2f)
        ) {
            SaturationColorSlider(colorState, modifier = Modifier.fill().align(Alignment.Center))
        }
        FloatEditor(
            mutableStateOf(colorState, { it.toHSVColor().saturation * 100 }) { colorState.getValue().clone().toHSVColor().saturation(it / 100) },
            range = 0f..100f,
            modifier = Modifier.width(45f),
            editorModifier = { Modifier.weight(1) }
        )
    }
    Column(Modifier.weight(1)) {
        Box(
            modifier.padding(vertical = 4f).weight(1).margin(right = 2f)
        ) {
            ValueColorSlider(colorState, modifier = Modifier.fill().align(Alignment.Center))
        }
        FloatEditor(
            mutableStateOf(colorState, { it.toHSVColor().value * 100 }) { colorState.getValue().clone().toHSVColor().value(it / 100) },
            range = 0f..100f,
            modifier = Modifier.width(45f),
            editorModifier = { Modifier.weight(1) }
        )
    }
    Column(Modifier.weight(1)) {
        Box(
            modifier.padding(vertical = 4f).weight(1).margin(right = 2f)
        ) {
            AlphaColorSlider(colorState, modifier = Modifier.fill().align(Alignment.Center))
        }
        FloatEditor(
            mutableStateOf(colorState, { it.alphaF * 100 }) { Color(colorState.getValue().argb).alpha(it / 100) },
            range = 0f..100f,
            modifier = Modifier.width(45f),
            editorModifier = { Modifier.weight(1) }
        )
    }
    scope()
}


fun WidgetContainerScope.SaturationColorSlider(
    colorState: MutableState<ARGBColor>,
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
    colorState: MutableState<ARGBColor>,
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
    colorState: MutableState<ARGBColor>,
    modifier: Modifier = Modifier,
    scope: WidgetScope.() -> Unit = {}
): IGWidgetImpl {
    val valueState = mutableStateOf(colorState.getValue().toHSVColor().hue)
    var progress = valueState.getValue().toDouble() / 360f
    MutableState.bind(colorState, valueState, {
        (it.toHSVColor().hue).apply {
            progress = (this / 360f).toDouble()
        }
    }, {
        colorState.getValue().clone().toHSVColor().hue(it)
    })
    return FloatSlider(
        valueState,
        range = 0f..360f,
        valueMapper = {
            progress = it
            (it * 360f).toFloat()
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