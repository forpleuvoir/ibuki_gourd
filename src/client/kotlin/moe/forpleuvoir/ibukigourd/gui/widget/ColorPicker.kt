package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.render
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
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


fun WidgetContainerScope.ColorSlider(
    colorState: State<ARGBColor>,
    modifier: Modifier = Modifier,
) {
    val valueState = stateOf(colorState.getValue().red)
    valueState.subscribe {
        colorState.setValue(Color(colorState.getValue().argb).red(it))
    }
    IntSlider(
        stateOf(colorState.getValue().red),
        range = 0..255,
        modifier = Modifier.render { context, _, _, _ ->
            context.batchRenderBox {
                pushBoxOutline(transform, Colors.BLACK)
            }
        }
    ) {}
}