package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.IGButtonWidget
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
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
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick
import kotlin.time.Duration.Companion.milliseconds

fun WidgetContainerScope.KeyBindButton(
    keyBind: KeyBind,
    text: MutableState<Text> = mutableStateOf(keyBind.asText),
    hoverText: MutableState<Text> = mutableStateOf(text.getValue()),
    modifier: Modifier = Modifier,
    onKeyChanged: (KeyBind) -> Unit = {},
): IGButtonWidget {
    var inputting = false
    val keys = mutableSetOf<KeyCode>()

    val inputtingColor = Colors.ORANGE
    return Button(
        modifier = Modifier
            .width(120f)
            .mousePress { event ->
                onMousePress(event)
                event.tryUse(inputting && event.button.code != Keyboard.BACKSPACE.code).onSuccess {
                    keys.add(event.button)
                    hoverText.setValue(IGLang.releaseToSaveSetting.withColor(inputtingColor))
                    text.setValue(Literal(keys.map { it.keyNameText }.joinToString(separator = " + ") { it.plainText }).withColor(inputtingColor))
                }
            }
            .keyPress { event ->
                onKeyPress(event)
                event.tryUse(inputting && event.keyCode != Keyboard.BACKSPACE).onSuccess {
                    keys.add(event.keyCode)
                    hoverText.setValue(IGLang.releaseToSaveSetting.withColor(inputtingColor))
                    text.setValue(Literal(keys.map { it.keyNameText }.joinToString(separator = " + ") { it.plainText }).withColor(inputtingColor))
                }
            }
            .mouseRelease { event ->
                val pressed = (this as IGButtonWidget).pressed
                onMouseRelease(event)
                event.tryUse(!inputting && wasMouseOver && pressed).onSuccess {
                    inputting = true
                    text.setValue(IGLang.pressToSetting.withColor(inputtingColor))
                }
                event.tryUse(inputting).onSuccess {
                    inputting = false
                    keyBind.setKey(*keys.toTypedArray())
                    onKeyChanged(keyBind)
                    text.setValue(keyBind.asText)
                    hoverText.setValue(text.getValue())
                    keys.clear()
                }
            }
            .keyRelease { event ->
                onKeyRelease(event)
                event.tryUse(inputting).onSuccess {
                    inputting = false
                    keyBind.setKey(*keys.toTypedArray())
                    onKeyChanged(keyBind)
                    text.setValue(keyBind.asText)
                    hoverText.setValue(text.getValue())
                    keys.clear()
                }
            }
            .hoverText(text = hoverText, showDelay = 50.milliseconds)
            .then(modifier)
    ) {
        TextLabel(text)
    }
}

fun WidgetContainerScope.KeyBindSettingButton(
    keyBind: KeyBind,
    title: Text,
    modifier: Modifier = Modifier,
    onSettingChange: (KeyBindSetting) -> Unit = {}
): IGButtonWidget {
    val setting = KeyBindSetting().apply {
        copyFrom(keyBind.setting)
    }
    return Button(
        modifier
    ) {
        Icon(IconTextures.SETTING)
        click {
            SimpleDialog(
                stateOf(title),
                screenModifier = Modifier.onClose {
                    keyBind.setting.copyFrom(setting)
                    onSettingChange.invoke(setting)
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