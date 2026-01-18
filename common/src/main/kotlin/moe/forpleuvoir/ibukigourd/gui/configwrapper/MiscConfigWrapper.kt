package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl.Companion.open
import moe.forpleuvoir.ibukigourd.gui.base.screen.closeScreen
import moe.forpleuvoir.ibukigourd.gui.base.tip.Tip
import moe.forpleuvoir.ibukigourd.gui.base.tip.TipHandler
import moe.forpleuvoir.ibukigourd.gui.base.tip.popHoverTip
import moe.forpleuvoir.ibukigourd.gui.base.tip.pushHoverTip
import moe.forpleuvoir.ibukigourd.gui.modifier.disableRenderBackground
import moe.forpleuvoir.ibukigourd.gui.widget.ConfirmDialog
import moe.forpleuvoir.ibukigourd.gui.widget.DurationSlider
import moe.forpleuvoir.ibukigourd.gui.widget.EnumSelector
import moe.forpleuvoir.ibukigourd.gui.widget.SimpleDialog
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.button.SwitchButton
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.DoubleEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextAreaWrapped
import moe.forpleuvoir.ibukigourd.gui.widget.text.TextEditor
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.config.ConfigSerializable
import moe.forpleuvoir.nebula.config.item.impl.ConfigBoolean
import moe.forpleuvoir.nebula.config.item.impl.ConfigDuration
import moe.forpleuvoir.nebula.config.item.impl.ConfigEnum
import moe.forpleuvoir.nebula.config.item.impl.ConfigString
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun ContainerScope.UnspecifiedConfigWrapper(
    config: ConfigSerializable,
    modifier: Modifier = Modifier
) = ConfigRowWrapper(config, modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        Button(
            Modifier.width(120f).hoverText(IGLang.unsupported)
        ) {
            Text(IGLang.unsupported)
        }
    }
}

fun ContainerScope.StringConfigWrapper(
    config: ConfigString,
    modifier: Modifier = Modifier
) = ConfigRowWrapper(config, modifier) {
    val strValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        TextEditor(
            modifier = Modifier.width(120f)
        ) {
            bindState(strValue)
        }
        Button {
            Icon(IconTextures.EDIT)
            click {
                SimpleDialog(stateOf(config.translateText)) {
                    TextAreaWrapped(
                        modifier = Modifier
                            .minHeight(150f).maxHeight(240f)
                            .maxWidth(480f)
                            .disableRenderBackground()
                            .padding(0f)
                    ) {
                        bindState(strValue)
                    }
                }.open()
            }
        }

        ConfigResetButton(config) {
            strValue.setValue(config.getValue())
        }
    }
}

fun <E : Enum<E>> ContainerScope.EnumConfigWrapper(
    config: ConfigEnum<E>,
    modifier: Modifier = Modifier
) = ConfigRowWrapper(config, modifier) {
    val enumValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    val selected = mutableStateOf(enumValue) { it }
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        EnumSelector(enumValue, modifier = Modifier.width(80f))
        ConfigResetButton(config) {
            enumValue.setValue(config.getValue())
        }
    }
}

fun ContainerScope.BooleanConfigWrapper(
    config: ConfigBoolean,
    modifier: Modifier = Modifier
) = ConfigRowWrapper(config, modifier) {
    val boolValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        SwitchButton(boolValue, Modifier.width(40f))
        ConfigResetButton(config) {
            boolValue.setValue(config.getValue())
        }
    }
}

fun ContainerScope.ConfigDurationWrapper(
    config: ConfigDuration,
    modifier: Modifier = Modifier
) = ConfigRowWrapper(config, modifier) {

    val durationValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        DurationSlider(
            durationValue,
            config.minDuration..config.maxDuration,
            modifier = Modifier.width(120f)
        )
        Button(
            modifier = Modifier.hoverText(IGLang.edit)
        ) {
            Icon(IconTextures.EDIT)
            click {
                val value = mutableStateOf(durationValue.getValue().toDouble(DurationUnit.SECONDS))
                val unit = mutableStateOf(DurationUnit.SECONDS)
                var editor: (() -> Transform)? = null
                ConfirmDialog(
                    stateOf(config.translateText),
                    onConfirm = {
                        val duration = value.getValue().toDuration(unit.getValue())
                        if (duration in config.minDuration..config.maxDuration) {
                            durationValue.setValue(duration)
                            closeScreen()
                        } else {
                            editor?.let {
                                TipHandler.pushHoverTip(2.seconds, it, Tip {
                                    Text(IGLang.notInRange(duration, config.minDuration, config.maxDuration))
                                })
                            }
                        }
                    },
                    screenModifier = Modifier.onClose {
                        TipHandler.popHoverTip()
                    }
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5f)) {
                        DoubleEditor(value, 0.0..999.9, modifier = Modifier.width(120f), editorModifier = { Modifier.weight(1) }) {
                            editor = { owner().transform }
                        }
                        EnumSelector(unit, modifier = Modifier.width(75f))
                    }
                }.open()
            }
        }
        ConfigResetButton(config) {
            durationValue.setValue(it.getValue())
        }
    }
}