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
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.config.Config


fun WidgetContainerScope.ConfigKeyBindWrapper(
    config: ConfigKeyBind,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        KeyBindWrapper(config) { it.getValue() }
        ConfigResetButton(config)
    }
}

fun WidgetContainerScope.ConfigKeyBindBooleanWrapper(
    config: ConfigKeyBindBoolean,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val boolValue = mutableStateOf(config.getValue().value).apply {
        subscribe {
            config.setValue(config.getValue().copy(value = it))
        }
    }
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        SwitchButton(boolValue, modifier.width(40f))
        KeyBindWrapper(config, Modifier.width(75f)) { it.getValue().keyBind }
        ConfigResetButton(config) {
            boolValue.setValue(config.getValue().value)
        }
    }
}

private fun <C : Config<*, C>> ColumnScope.KeyBindWrapper(
    config: C,
    buttonModifier: Modifier = Modifier,
    mapping: (C) -> KeyBind
) {
    KeyBindButton(mapping(config), buttonModifier) {
        config.onChange(config)
    }
    KeyBindSettingButton(mapping(config), config.translateText) {
        config.onChange(config)
    }
}

