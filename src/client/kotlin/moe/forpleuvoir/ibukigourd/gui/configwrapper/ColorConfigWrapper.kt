package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.bgBlurRadius
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.minWidth
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.name
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.disableRender
import moe.forpleuvoir.ibukigourd.gui.widget.ColorPicker
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.OpenPopupTip
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.util.collection.notification
import moe.forpleuvoir.nebula.config.item.impl.ConfigRGBColor

fun WidgetContainerScope.ColorConfigWrapper(
    configColor: ConfigRGBColor<ARGBColor>,
    modifier: Modifier = Modifier
) = Column(
    Modifier.name("ColorConfigWrapper").then(modifier),
    horizontalArrangement = Arrangement.SpaceBetween
) {
    val colorValue = mutableStateOf(configColor.getValue()).apply {
        subscribe {
            configColor.setValue(it)
        }
    }
    ConfigTextLabel(configColor)
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        ColorButton(colorValue, Modifier.minWidth(80f))
        ConfigResetButton(configColor) {
            colorValue.setValue(configColor.getValue())
        }
    }
}

fun WidgetContainerScope.ColorButton(
    color: MutableState<ARGBColor>,
    modifier: Modifier = Modifier
) = Button(Modifier.name("ColorButton").then(modifier)) {
    val text = mutableStateOf(color) { Literal(it.hexStr).withColor(it) }
    TextLabel(text)
    press {
        OpenPopupTip(
            modifier = Modifier.disableRender(),
            screenModifier = Modifier.bgBlurRadius(0f),
            optionalDirection = Direction.clockwiseFromLeft.notification()
        ) {
            ColorPicker(color)
        }
    }
}