package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBind
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBindBoolean
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.widget.KeyBindButton
import moe.forpleuvoir.ibukigourd.gui.widget.KeyBindSettingButton
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.input.KeyBind
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.config.Config


fun WidgetContainerScope.ConfigKeyBindWrapper(
    config: ConfigKeyBind,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val text = mutableStateOf(config.getValue().asText)
    val hoverText = mutableStateOf(text.getValue())
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        KeyBindWrapper(text, hoverText, config) { it.getValue() }
        ConfigResetButton(config) {
            text.setValue(config.getValue().asText)
            hoverText.setValue(text.getValue())
        }
    }
}

fun WidgetContainerScope.ConfigKeyBindBooleanWrapper(
    config: ConfigKeyBindBoolean,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val text = mutableStateOf(config.getValue().keyBind.asText)
    val hoverText = mutableStateOf(text.getValue())
    val boolValue = mutableStateOf(config.getValue().value).apply {
        subscribe {
            config.setValue(config.getValue().copy(value = it))
        }
    }
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        SwitchButton(boolValue, modifier.width(40f))
        KeyBindWrapper(text, hoverText, config, Modifier.width(75f)) { it.getValue().keyBind }
        ConfigResetButton(config) {
            text.setValue(config.getValue().keyBind.asText)
            boolValue.setValue(config.getValue().value)
            hoverText.setValue(text.getValue())
        }
    }
}

private fun <C : Config<*, C>> ColumnScope.KeyBindWrapper(
    text: MutableState<Text>,
    hoverText: MutableState<Text>,
    config: C,
    buttonModifier: Modifier = Modifier,
    mapping: (C) -> KeyBind
) {
    KeyBindButton(mapping(config), text, hoverText, buttonModifier) {
        config.onChange(config)
    }
    KeyBindSettingButton(mapping(config), config.translateText) {
        config.onChange(config)
    }
}

