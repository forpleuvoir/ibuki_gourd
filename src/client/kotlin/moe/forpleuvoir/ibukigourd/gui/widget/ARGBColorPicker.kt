package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderAlignmentText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.render
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidgetImpl
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors

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
    modifier: Modifier = Modifier
) = ARGBColorComponentSlider(
    colorState,
    colorComponentGetter = { it.red },
    colorComponentSetter = { c, i -> Color(c.argb).red(i) },
    renderColorComponentSetter = { c, i -> Color(c.argb).red(i).alpha(255) },
    modifier
)

fun WidgetContainerScope.GreenColorSlider(
    colorState: State<ARGBColor>,
    modifier: Modifier = Modifier
) = ARGBColorComponentSlider(
    colorState,
    colorComponentGetter = { it.green },
    colorComponentSetter = { c, i -> Color(c.argb).green(i) },
    renderColorComponentSetter = { c, i -> Color(c.argb).green(i).alpha(255) },
    modifier
)

fun WidgetContainerScope.BlueColorSlider(
    colorState: State<ARGBColor>,
    modifier: Modifier = Modifier
) = ARGBColorComponentSlider(
    colorState,
    colorComponentGetter = { it.blue },
    colorComponentSetter = { c, i -> Color(c.argb).blue(i) },
    renderColorComponentSetter = { c, i -> Color(c.argb).blue(i).alpha(255) },
    modifier
)

fun WidgetContainerScope.AlphaColorSlider(
    colorState: State<ARGBColor>,
    modifier: Modifier = Modifier
) = ARGBColorComponentSlider(
    colorState,
    colorComponentGetter = { it.alpha },
    colorComponentSetter = { c, i -> Color(c.argb).alpha(i) },
    renderColorComponentSetter = { c, i -> Color(c.argb).alpha(i) },
    modifier
)


fun WidgetContainerScope.ARGBColorComponentSlider(
    colorState: State<ARGBColor>,
    colorComponentGetter: (ARGBColor) -> Int,
    colorComponentSetter: (ARGBColor, Int) -> ARGBColor,
    renderColorComponentSetter: (ARGBColor, Int) -> ARGBColor,
    modifier: Modifier = Modifier,
): IGWidgetImpl {
    val valueState = stateOf(colorComponentGetter(colorState.getValue()))
    valueState.subscribe {
        colorState.setValue(colorComponentSetter(colorState.getValue(), it))
    }
    var progress = valueState.getValue().toDouble() / 255
    return IntSlider(
        valueState,
        range = 0..255,
        valueMapper = {
            progress = it
            (it * 255).toInt()
        },
        modifier = Modifier.render { context, _, _, _ ->
            context.batchRenderBox {
                pushBoxOutline(transform, Colors.BLACK)
                pushGradientBox(
                    transform,
                    startColor = renderColorComponentSetter(colorState.getValue(), 0),
                    endColor = renderColorComponentSetter(colorState.getValue(), 255)
                )
                pushBox(transform.asWorldBox.copy(x = transform.worldX - 0.5f + (progress.toFloat() * transform.width), width = 1f), Colors.BLACK)
            }
            context.renderAlignmentText(valueState.getValue().toString(), transform.asWorldBox, Alignment.Center, color = Colors.WHITE)
        }.then(modifier)
    ) {}
}