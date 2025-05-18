package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBind
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBindBoolean
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.widget.KeyBindButton
import moe.forpleuvoir.ibukigourd.gui.widget.KeyBindSettingButton
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope
import moe.forpleuvoir.ibukigourd.input.KeyBind
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.config.Config


fun ContainerScope.ConfigKeyBindWrapper(
    config: ConfigKeyBind,
    modifier: Modifier = Modifier
) = ConfigRowWrapper(config, modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        KeyBindWrapper(config) { it.getValue() }
        ConfigResetButton(config)
    }
}

fun ContainerScope.ConfigKeyBindBooleanWrapper(
    config: ConfigKeyBindBoolean,
    modifier: Modifier = Modifier
) = ConfigRowWrapper(config, modifier) {
    val boolValue = mutableStateOf(config.getValue().value).apply {
        subscribe {
            config.setValue(config.getValue().copy(value = it))
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        SwitchButton(boolValue, Modifier.width(40f))
        KeyBindWrapper(config, Modifier.width(75f)) { it.getValue().keyBind }
        ConfigResetButton(config) {
            boolValue.setValue(config.getValue().value)
        }
    }
}

private fun <C : Config<*, C>> RowScope.KeyBindWrapper(
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

