package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.width
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.widget.Spinner
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.util.valueOf
import moe.forpleuvoir.nebula.config.item.impl.ConfigBoolean
import moe.forpleuvoir.nebula.config.item.impl.ConfigEnum
import moe.forpleuvoir.nebula.config.item.impl.ConfigString

fun WidgetContainerScope.StringConfigWrapper(
    config: ConfigString,
    modifier: Modifier = Modifier
) = Column(
    modifier,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    val strValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    ConfigTextLabel(config)
    Column {
        TextEditor(
            modifier = Modifier.width(140f)
        ) {
            text = strValue.getValue()
            textConsumer {
                disableTextNotification {
                    strValue.setValue(it)
                }
            }
            strValue.subscribe {
                text = it
            }
        }
        ConfigResetButton(config, strValue)
    }
}

fun <E : Enum<E>> WidgetContainerScope.EnumConfigWrapper(
    config: ConfigEnum<E>,
    modifier: Modifier = Modifier
) = Column(
    modifier,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    val enumValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    val selected = mutableStateOf(enumValue.getValue().name)
    selected.bind(enumValue) { it.name }

    ConfigTextLabel(config)
    Column {
        Spinner(
            options = config.getValue()::class.java.enumConstants.map { it.name },
            selected = selected,
            onChange = {
                Enum.valueOf(enumValue.getValue()::class, it)?.let { it1 -> enumValue.setValue(it1) }
            },
            selectedWrapper = {
                TextLabel(it, modifier = Modifier)
            },
            optionWrapper = {
                TextLabel(it, modifier = Modifier)
            },
            modifier = Modifier.width(140f),
        )
        ConfigResetButton(config, enumValue)
    }
}

fun WidgetContainerScope.BooleanConfigWrapper(
    config: ConfigBoolean,
    modifier: Modifier = Modifier
) = Column(
    modifier,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    val boolValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    ConfigTextLabel(config)
    Column {
        SwitchButton(boolValue, modifier.width(80f))
        ConfigResetButton(config, boolValue)
    }
}