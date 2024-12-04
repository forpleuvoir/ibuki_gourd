package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBind
import moe.forpleuvoir.ibukigourd.config.item.impl.ConfigKeyBindBoolean
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.keyPress
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.keyRelease
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.onClose
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.layout.ColumnScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.LongEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextLabel
import moe.forpleuvoir.ibukigourd.gui.widget.tip.HoverTip
import moe.forpleuvoir.ibukigourd.gui.widget.tip.PopupTip
import moe.forpleuvoir.ibukigourd.input.KeyBind
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.keyBindSetting
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.switch
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.collection.notification
import moe.forpleuvoir.nebula.common.util.primitive.pick
import moe.forpleuvoir.nebula.config.Config
import kotlin.time.Duration.Companion.milliseconds


fun WidgetContainerScope.ConfigKeyBindWrapper(
    config: ConfigKeyBind,
    modifier: Modifier = Modifier
) = Column(
    modifier,
    horizontalArrangement = Arrangement.SpaceBetween
) {

    val text = mutableStateOf(config.getValue().asText)
    ConfigTextLabel(config)
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
) = Column(
    modifier,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    val text = mutableStateOf(config.getValue().keyBind.asText)
    val boolValue = mutableStateOf(config.getValue().value).apply {
        subscribe {
            config.setValue(config.getValue().copy(value = it))
        }
    }
    ConfigTextLabel(config)
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
    var inputting = false
    val keys = mutableSetOf<KeyCode>()
    val settingsPopupState = mutableStateOf(false)
    val keyBind = mapping(config)
    Button(
        modifier = Modifier
            .width(120f)
            .keyPress { event ->
                onKeyPress(event)
                event.tryUse(inputting).onSuccess {
                    keys.add(event.keyCode)
                    if (keys.size == 1) text.setValue(Literal(event.keyCode.toString()).withColor(Colors.ORANGE))
                    else text.setValue(Literal(keys.joinToString(separator = " + ")).withColor(Colors.ORANGE))
                }
            }.keyRelease { event ->
                onKeyRelease(event)
                if (inputting) {
                    inputting = false
                    keyBind.setKey(*keys.toTypedArray())
                    config.onChange(config)
                    text.setValue(keyBind.asText)
                    keys.clear()
                }
            }.then(buttonModifier)
    ) {
        press {
            inputting = !inputting
            text.setValue(Literal("按下按键设置"))
        }
        TextLabel(text)
        HoverTip(50.milliseconds) {
            TextLabel(text)
        }

        val setting = keyBindSetting().apply {
            copyFrom(keyBind.setting)
        }
        PopupTip(
            settingsPopupState,
            screenModifier = Modifier.onClose {
                keyBind.setting.copyFrom(setting)
                config.onChange(config)
            },
            optionalDirection = Direction.clockwiseFromLeft.notification()
        ) {
            //on open
            setting.copyFrom(keyBind.setting)
            //TODO i18n
            Row(
                Modifier.width(240f),
                verticalArrangement = Arrangement.spacedBy(5f)
            ) {
                Column(
                    Modifier.fill(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextLabel("按键触发环境")
                    val enumValue = mutableStateOf(setting.environment).apply {
                        subscribe { setting.environment = it }
                    }
                    val selected = mutableStateOf(setting.environment.name)
                    selected.bind(enumValue) { it.name }

                    EnumSelector(selected, enumValue, Modifier.width(80f))
                }

                Column(
                    Modifier.fill(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextLabel("是否取消之后的操作")
                    val nextAction = mutableStateOf(setting.nextAction.value).apply {
                        subscribe { setting.nextAction = it.pick(NextAction.Cancel, NextAction.Continue) }
                    }
                    SwitchButton(nextAction, Modifier.width(40f))
                }

                Column(
                    Modifier.fill(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextLabel("只有完全匹配的按键才会触发")
                    val exactMatch = mutableStateOf(setting.exactMatch).apply {
                        subscribe { setting.exactMatch = it }
                    }
                    SwitchButton(exactMatch, Modifier.width(40f))
                }

                Column(
                    Modifier.fill(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextLabel("按键触发模式")
                    val enumValue = mutableStateOf(setting.triggerMode).apply {
                        subscribe { setting.triggerMode = it }
                    }
                    val selected = mutableStateOf(setting.triggerMode.name)
                    selected.bind(enumValue) { it.name }

                    EnumSelector(selected, enumValue, Modifier.width(80f))
                }

                Column(
                    Modifier.fill(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextLabel("按下多久触发长按")
                    val longPressTime = mutableStateOf(setting.longPressTime).apply {
                        subscribe { setting.longPressTime = it }
                    }
                    LongEditor(longPressTime, range = 0..1000L, modifier = Modifier.width(60f))
                }

                Column(
                    Modifier.fill(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextLabel("重复触发的按键周期")
                    val triggerPeriod = mutableStateOf(setting.triggerPeriod).apply {
                        subscribe { setting.triggerPeriod = it }
                    }
                    LongEditor(triggerPeriod, range = 0..1000L, modifier = Modifier.width(60f))
                }

            }
        }
    }

    Button {
        Icon(IconTextures.SETTING)
        press { settingsPopupState.switch() }
    }
}

