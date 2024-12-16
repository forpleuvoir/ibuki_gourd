package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBind
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBindBoolean
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.widget.EnumSelector
import moe.forpleuvoir.ibukigourd.gui.widget.SimpleDialog
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.LongEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.input.KeyBind
import moe.forpleuvoir.ibukigourd.input.KeyBindSetting
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.ibukigourd.util.state.switch
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick
import moe.forpleuvoir.nebula.config.Config
import kotlin.time.Duration.Companion.milliseconds


fun WidgetContainerScope.ConfigKeyBindWrapper(
    config: ConfigKeyBind,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val text = mutableStateOf(config.getValue().asText)
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        KeyBindWrapper(text, config) { it.getValue() }
        ConfigResetButton(config) {
            text.setValue(config.getValue().asText)
        }
    }
}

fun WidgetContainerScope.ConfigKeyBindBooleanWrapper(
    config: ConfigKeyBindBoolean,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val text = mutableStateOf(config.getValue().keyBind.asText)
    val boolValue = mutableStateOf(config.getValue().value).apply {
        subscribe {
            config.setValue(config.getValue().copy(value = it))
        }
    }
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        SwitchButton(boolValue, modifier.width(40f))
        KeyBindWrapper(text, config, Modifier.width(75f)) { it.getValue().keyBind }
        ConfigResetButton(config) {
            text.setValue(config.getValue().keyBind.asText)
        }
    }
}

private fun <C : Config<*, C>> ColumnScope.KeyBindWrapper(
    text: MutableState<Text>,
    config: C,
    buttonModifier: Modifier = Modifier,
    mapping: (C) -> KeyBind
) {
    val hoverText = mutableStateOf(text.getValue())
    var inputting = false
    val keys = mutableSetOf<KeyCode>()
    val settingsPopupState = mutableStateOf(false)
    val keyBind = mapping(config)
    val setting = KeyBindSetting().apply {
        copyFrom(keyBind.setting)
    }

    val inputtingColor = Colors.ORANGE

    Button(
        modifier = Modifier
            .width(120f)
            .keyPress { event ->
                onKeyPress(event)
                event.tryUse(inputting && event.keyCode != Keyboard.BACKSPACE).onSuccess {
                    keys.add(event.keyCode)
                    hoverText.setValue(IGLang.releaseToSaveSetting.withColor(inputtingColor))
                    text.setValue(Literal(keys.map { it.keyNameText }.joinToString(separator = " + ") { it.plainText }).withColor(inputtingColor))
                }
            }.keyRelease { event ->
                onKeyRelease(event)
                event.tryUse(inputting).onSuccess {
                    inputting = false
                    keyBind.setKey(*keys.toTypedArray())
                    config.onChange(config)
                    text.setValue(keyBind.asText)
                    hoverText.setValue(text.getValue())
                    keys.clear()
                }
            }
            .hoverText(hoverText)
            .hoverTextShowDelay(50.milliseconds)
            .then(buttonModifier)
    ) {
        release {
            inputting = true
            text.setValue(IGLang.pressToSetting.withColor(inputtingColor))
        }
        TextLabel(text)
    }

    Button {
        Icon(IconTextures.SETTING)
        click {
            settingsPopupState.switch()
            SimpleDialog(
                stateOf(config.translateText),
                screenModifier = Modifier.onClose {
                    keyBind.setting.copyFrom(setting)
                    config.onChange(config)
                },
            ) {
                //on open
                setting.copyFrom(keyBind.setting)
                Row(
                    Modifier.width(240f),
                    verticalArrangement = Arrangement.spacedBy(5f)
                ) {
                    Column(
                        Modifier.fill(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextLabel(IGLang.nextAction)
                        val nextAction = mutableStateOf(setting.nextAction.value).apply {
                            subscribe { setting.nextAction = it.pick(NextAction.Cancel, NextAction.Continue) }
                        }
                        SwitchButton(nextAction, Modifier.width(40f))
                    }

                    Column(
                        Modifier.fill(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextLabel(IGLang.exactMatch)
                        val exactMatch = mutableStateOf(setting.exactMatch).apply {
                            subscribe { setting.exactMatch = it }
                        }
                        SwitchButton(exactMatch, Modifier.width(40f))
                    }

                    Column(
                        Modifier.fill(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextLabel(IGLang.longPressTime)
                        val longPressTime = mutableStateOf(setting.longPressTime).apply {
                            subscribe { setting.longPressTime = it }
                        }
                        LongEditor(longPressTime, range = 0..1000L, modifier = Modifier.width(60f), editorModifier = { Modifier.weight(1) })
                    }
                    Column(
                        Modifier.fill(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextLabel(IGLang.repeatTriggerInterval)
                        val triggerPeriod = mutableStateOf(setting.repeatTriggerInterval).apply {
                            subscribe { setting.repeatTriggerInterval = it }
                        }
                        LongEditor(triggerPeriod, range = 0..1000L, modifier = Modifier.width(60f), editorModifier = { Modifier.weight(1) })
                    }

                    Column(
                        Modifier.fill(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextLabel(IGLang.environment)
                        val enumValue = mutableStateOf(setting.environment).apply {
                            subscribe { setting.environment = it }
                        }
                        EnumSelector(enumValue, modifier = Modifier.width(80f))
                    }
                    Column(
                        Modifier.fill(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextLabel(IGLang.triggerMode)
                        val enumValue = mutableStateOf(setting.triggerMode).apply {
                            subscribe { setting.triggerMode = it }
                        }
                        EnumSelector(enumValue, modifier = Modifier.width(80f))
                    }

                }
            }.open()
        }
    }
}

