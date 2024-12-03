package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigDurationObject
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.LongSlider
import moe.forpleuvoir.ibukigourd.gui.widget.Spinner
import moe.forpleuvoir.ibukigourd.gui.widget.SwitchableProxy
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.LongEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextAreaWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.PopupTip
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.switch
import moe.forpleuvoir.nebula.common.util.collection.notifiableList
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
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        val showState = mutableStateOf(false)
        TextEditor(
            modifier = Modifier.width(120f)
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
            PopupTip(
                showState,
                optionalDirection = notifiableList(Direction.Left, Direction.Top, Direction.Right, Direction.Bottom).apply {}
            ) {
                Row {
                    ConfigTextLabel(config)
                    TextAreaWrapped(modifier = Modifier.maxWidth(360f).minWidth(180f).maxHeight(300f).minHeight(150f)) {
                        text = strValue.getValue()
                        textConsumer {
                            strValue.setValue(it)
                        }
                    }
                }
            }
        }
        Button {
            Icon(IconTextures.EDIT)
            press { showState.switch() }
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
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        EnumSelector(selected, enumValue, Modifier.width(80f))
        ConfigResetButton(config, enumValue)
    }
}

private fun <E : Enum<E>> ColumnScope.EnumSelector(
    selected: MutableState<String>,
    enumValue: MutableState<E>,
    modifier: Modifier = Modifier
) = Spinner(
    options = enumValue.getValue()::class.java.enumConstants.map { it.name },
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
    modifier = modifier,
)


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
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        SwitchButton(boolValue, modifier.width(40f))
        ConfigResetButton(config, boolValue)
    }
}

fun WidgetContainerScope.ConfigDurationWrapper(
    config: ConfigDurationObject,
    modifier: Modifier = Modifier
) = Column(
    modifier,
    horizontalArrangement = Arrangement.SpaceBetween
) {

    val longValue = mutableStateOf(config.getValue().duration).apply {
        subscribe {
            config.setValue(config.getValue().copy(duration = it))
        }
    }
    val unitValue = mutableStateOf(config.getValue().unit).apply {
        subscribe {
            config.setValue(config.getValue().copy(unit = it))
        }
    }

    val durationValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }

    val selected = mutableStateOf(unitValue.getValue().name)
    selected.bind(unitValue) { it.name }

    ConfigTextLabel(config)
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        val state = mutableStateOf(true)

        SwitchableProxy(
            { LongSlider(longValue, 0L..1000L, modifier = Modifier.width(120f), textMapper = { Literal(config.duration.toString()) }) },
            {
                Column(horizontalArrangement = Arrangement.spacedBy(0f)) {
                    LongEditor(longValue, 0L..1000L, modifier = Modifier.width(45f), editorModifier = { Modifier.weight(1) })
                    EnumSelector(selected, unitValue, Modifier.width(75f))
                }
            },
            state
        )
        Button {
            press { state.switch() }
            Icon(IconTextures.SWITCH)
        }
        ConfigResetButton(config, durationValue) {
            durationValue.setValue(config.getValue())
            longValue.setValue(durationValue.getValue().duration)
            unitValue.setValue(durationValue.getValue().unit)
        }
    }
}