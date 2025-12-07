package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.minWidth
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.modifier.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.widget.ColorPicker
import moe.forpleuvoir.ibukigourd.gui.widget.Dialog
import moe.forpleuvoir.ibukigourd.gui.widget.button.ColorButton
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.gui.widget.toHSVColor
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.color.HSVColor
import moe.forpleuvoir.nebula.config.item.impl.ConfigRGBColor

fun ContainerScope.ColorConfigWrapper(
    config: ConfigRGBColor<ARGBColor>,
    modifier: Modifier = Modifier
) = ConfigRowWrapper(config, modifier) {
    val colorValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            if (config.getValue() is HSVColor)
                config.setValue(it.toHSVColor())
            else config.setValue(Color.ofARGB(it.argb))
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        ColorConfigSettingButton(colorValue, Modifier.minWidth(80f))
        ConfigResetButton(config) {
            colorValue.setValue(config.getValue())
        }
    }
}

private fun ContainerScope.ColorConfigSettingButton(
    color: MutableState<ARGBColor>,
    modifier: Modifier = Modifier
) = ColorButton(color, modifier) {
    val text = mutableStateOf(color) {
        Literal(it.hexStr).withColor(getContrastColor(it))
    }
    Text(text)
    click {
        Dialog(
            Modifier.disableRenderBackground()
        ) {
            ColorPicker(color)
        }.open()
    }
}

fun getContrastColor(backgroundColor: ARGBColor): ARGBColor {
    // 计算背景颜色的亮度
    val brightness = (backgroundColor.red * 0.299 + backgroundColor.green * 0.587 + backgroundColor.blue * 0.114)
    // 根据亮度选择对比色
    return if (brightness > 186) Colors.BLACK else Colors.WHITE
}