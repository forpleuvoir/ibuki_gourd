package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.renderAlignmentText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.bgBlurRadius
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.minWidth
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.renderOverlay
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.modifier.disableRender
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.ColorPicker
import moe.forpleuvoir.ibukigourd.gui.widget.button.ColorButton
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.PopupTip
import moe.forpleuvoir.ibukigourd.gui.widget.toHSVColor
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.color.HSVColor
import moe.forpleuvoir.nebula.common.util.collection.notification
import moe.forpleuvoir.nebula.config.item.impl.ConfigRGBColor

fun WidgetContainerScope.ColorConfigWrapper(
    config: ConfigRGBColor<ARGBColor>,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val colorValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            if (config.getValue() is HSVColor)
                config.setValue(it.toHSVColor())
            else config.setValue(Color(it.argb))
        }
    }
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        ColorConfigSettingButton(colorValue, Modifier.minWidth(80f))
        ConfigResetButton(config) {
            colorValue.setValue(config.getValue())
        }
    }
}

private fun WidgetContainerScope.ColorConfigSettingButton(
    color: MutableState<ARGBColor>,
    modifier: Modifier = Modifier
) = ColorButton(color, modifier) {
    val text = mutableStateOf(color) {
        Literal(it.hexStr).withColor(getContrastColor(it))
    }
    TextLabel(text)
    click {
        PopupTip(
            modifier = Modifier
                .disableRender()
                .renderOverlay { context, _, _, _ ->
                    context.renderAlignmentText(
                        IGLang.clickBlankBack,
                        screen()!!.transform,
                        color = HSVColor(0f, 0f, 0.85f),
                        alignment = Alignment.biasedBy(0f, 0.8f),
                        shadow = true
                    )
                },
            screenModifier = Modifier.bgBlurRadius(0f),
            optionalDirection = Direction.clockwiseFromLeft.notification()
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