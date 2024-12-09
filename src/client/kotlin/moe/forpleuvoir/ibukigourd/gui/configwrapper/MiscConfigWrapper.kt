package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigDurationObject
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.maxSize
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.minSize
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.util.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.widget.LongSlider
import moe.forpleuvoir.ibukigourd.gui.widget.SimpleDialog
import moe.forpleuvoir.ibukigourd.gui.widget.SwitchableProxy
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.text.LongEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextAreaWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.HoverTip
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.ibukigourd.util.state.switch
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.impl.ConfigBoolean
import moe.forpleuvoir.nebula.config.item.impl.ConfigEnum
import moe.forpleuvoir.nebula.config.item.impl.ConfigString

fun WidgetContainerScope.UnspecifiedConfigWrapper(
    config: Config<*, *>,
    modifier: Modifier = Modifier
) = Column(
    modifier,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    ConfigTextLabel(config)
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        Button(
            Modifier.width(80f)
        ) {
            //TODO i18n
            TextLabel("Unsupported")
            HoverTip {
                TextLabel("Unsupported Config")
            }
        }
        ConfigResetButton(config) {}
    }
}

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

        }
        Button {
            Icon(IconTextures.EDIT)
            click {
                SimpleDialog(stateOf(config.translateText)) {
                    TextAreaWrapped(
                        modifier = Modifier
                            .maxSize(320f, 240f).minSize(180f, 150f)
                            .disableRenderBackground()
                            .padding(0f)
                    ) {
                        text = strValue.getValue()
                        textConsumer {
                            strValue.setValue(it)
                        }
                    }
                }.open()
            }
        }

        ConfigResetButton(config) {
            strValue.setValue(config.getValue())
        }
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
        ConfigResetButton(config) {
            enumValue.setValue(config.getValue())
        }
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
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        SwitchButton(boolValue, modifier.width(40f))
        ConfigResetButton(config) {
            boolValue.setValue(config.getValue())
        }
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
            click { state.switch() }
            Icon(IconTextures.SWITCH)
        }
        ConfigResetButton(config) {
            durationValue.setValue(config.getValue())
            longValue.setValue(durationValue.getValue().duration)
            unitValue.setValue(durationValue.getValue().unit)
        }
    }
}