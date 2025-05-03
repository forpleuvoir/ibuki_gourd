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
import moe.forpleuvoir.ibukigourd.input.*
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.copyToText
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.state.mutableStateBy
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick
import kotlin.time.Duration.Companion.milliseconds

val KeyBind.hoverText: Text
    get() {
        val conflictText = IGLang.keybindConflict
        val text = Literal(keys.map { it.keyNameText }.joinToString(separator = " + ") { it.plainText })
        if (keys.count() == 0) text.append(IGLang.pressToSetting)
        var count = 0
        InputHandler.detectKeyConflicts(this).forEach {
            count++
            conflictText.appendNewLine().appendLiteral(" - ").append(it.name)
        }
        return if (count > 0) text.copyToText().appendNewLine().append(conflictText)
        else text
    }


fun WidgetContainerScope.KeyBindButton(
    keyBind: KeyBind,
    modifier: Modifier = Modifier,
    onKeyChanged: (KeyBind) -> Unit = {},
): IGButtonWidget {
    var inputting = false
    val keys = mutableSetOf<KeyCode>()

    val inputtingColor = Colors.ORANGE
    val conflictColor = Colors.RED

    fun inputtingText() = Literal(
        keys.map { it.keyNameText }.joinToString(separator = " + ") { it.plainText }
    ).withColor(inputtingColor)

    fun text() = keyBind.asText.apply {
        if (InputHandler.detectKeyConflicts(keyBind).count() > 0) withColor(conflictColor)
    }

    return Button(
        modifier = Modifier
            .width(120f)
            .mousePress { event ->
                onMousePress(event)
                event.tryUse(inputting && event.button.code != Keyboard.BACKSPACE.code).onSuccess {
                    keys.add(event.button)
                }
            }
            .keyPress { event ->
                onKeyPress(event)
                event.tryUse(inputting && event.keyCode != Keyboard.BACKSPACE).onSuccess {
                    keys.add(event.keyCode)
                }
            }
            .mouseRelease { event ->
                val pressed = (this as IGButtonWidget).pressed
                onMouseRelease(event)
                event.tryUse(!inputting && wasMouseOver && pressed).onSuccess {
                    inputting = true
                }
                event.tryUse(inputting).onSuccess {
                    inputting = false
                    keyBind.setKey(*keys.toTypedArray())
                    onKeyChanged(keyBind)
                    keys.clear()
                }
            }
            .keyRelease { event ->
                onKeyRelease(event)
                event.tryUse(inputting).onSuccess {
                    inputting = false
                    keyBind.setKey(*keys.toTypedArray())
                    onKeyChanged(keyBind)
                    keys.clear()
                }
            }
            .hoverText(text = mutableStateBy {
                if (inputting) IGLang.releaseToSaveSetting.withColor(inputtingColor)
                else keyBind.hoverText
            }, showDelay = 50.milliseconds)
            .then(modifier)
    ) {
        TextLabel(mutableStateBy {
            if (inputting) {
                if (keys.count() > 0)
                    inputtingText()
                else Literal(IGLang.pressToSetting.plainText).withColor(inputtingColor)
            } else text()
        })
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
                Column(
                    Modifier.width(240f),
                    verticalArrangement = Arrangement.spacedBy(5f)
                ) {
                    Row(
                        Modifier.fill(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextLabel(IGLang.nextAction)
                        val nextAction = mutableStateOf(setting.nextAction.value).apply {
                            subscribe { setting.nextAction = it.pick(NextAction.Cancel, NextAction.Continue) }
                        }
                        SwitchButton(nextAction, Modifier.width(40f))
                    }

                    Row(
                        Modifier.fill(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextLabel(IGLang.exactMatch)
                        val exactMatch = mutableStateOf(setting.exactMatch).apply {
                            subscribe { setting.exactMatch = it }
                        }
                        SwitchButton(exactMatch, Modifier.width(40f))
                    }

                    Row(
                        Modifier.fill(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextLabel(IGLang.longPressTime)
                        val longPressTime = mutableStateOf(setting.longPressTime).apply {
                            subscribe { setting.longPressTime = it }
                        }
                        LongEditor(longPressTime, range = 0..1000L, modifier = Modifier.width(60f), editorModifier = { Modifier.weight(1) })
                    }
                    Row(
                        Modifier.fill(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextLabel(IGLang.repeatTriggerInterval)
                        val triggerPeriod = mutableStateOf(setting.repeatTriggerInterval).apply {
                            subscribe { setting.repeatTriggerInterval = it }
                        }
                        LongEditor(triggerPeriod, range = 0..1000L, modifier = Modifier.width(60f), editorModifier = { Modifier.weight(1) })
                    }

                    Row(
                        Modifier.fill(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextLabel(IGLang.environment)
                        val enumValue = mutableStateOf(setting.environment).apply {
                            subscribe { setting.environment = it }
                        }
                        EnumSelector(enumValue, modifier = Modifier.width(80f))
                    }
                    Row(
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