package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.widget.*
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.text.DoubleEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.FloatEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.IntEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.LongEditor
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.switch
import moe.forpleuvoir.nebula.config.item.impl.ConfigDouble
import moe.forpleuvoir.nebula.config.item.impl.ConfigFloat
import moe.forpleuvoir.nebula.config.item.impl.ConfigInt
import moe.forpleuvoir.nebula.config.item.impl.ConfigLong

private const val editorWidth = 120f

fun WidgetContainerScope.IntConfigWrapper(
    config: ConfigInt,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val intValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        val state = mutableStateOf(true)
        SwitchableProxy(
            { IntSlider(intValue, config.minValue..config.maxValue, modifier = Modifier.width(editorWidth)) },
            { IntEditor(intValue, config.minValue..config.maxValue, modifier = Modifier.width(editorWidth), editorModifier = { Modifier.weight(1) }) },
            state
        )
        Button {
            click { state.switch() }
            Icon(IconTextures.SWITCH)
        }
        ConfigResetButton(config) {
            intValue.setValue(config.getValue())
        }
    }
}

fun WidgetContainerScope.LongConfigWrapper(
    config: ConfigLong,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val longValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        val state = mutableStateOf(true)
        SwitchableProxy(
            { LongSlider(longValue, config.minValue..config.maxValue, modifier = Modifier.width(editorWidth)) },
            { LongEditor(longValue, config.minValue..config.maxValue, modifier = Modifier.width(editorWidth), editorModifier = { Modifier.weight(1) }) },
            state
        )
        Button {
            click { state.switch() }
            Icon(IconTextures.SWITCH)
        }
        ConfigResetButton(config) {
            longValue.setValue(config.getValue())
        }
    }
}

fun WidgetContainerScope.FloatConfigWrapper(
    config: ConfigFloat,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val floatValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        val state = mutableStateOf(true)
        SwitchableProxy(
            { FloatSlider(floatValue, config.minValue..config.maxValue, modifier = Modifier.width(editorWidth)) },
            { FloatEditor(floatValue, config.minValue..config.maxValue, modifier = Modifier.width(editorWidth), editorModifier = { Modifier.weight(1) }) },
            state
        )
        Button {
            click { state.switch() }
            Icon(IconTextures.SWITCH)
        }
        ConfigResetButton(config) {
            floatValue.setValue(config.getValue())
        }
    }
}

fun WidgetContainerScope.DoubleConfigWrapper(
    config: ConfigDouble,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val doubleValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        val state = mutableStateOf(true)
        SwitchableProxy(
            { DoubleSlider(doubleValue, config.minValue..config.maxValue, modifier = Modifier.width(editorWidth)) },
            { DoubleEditor(doubleValue, config.minValue..config.maxValue, modifier = Modifier.width(editorWidth), editorModifier = { Modifier.weight(1) }) },
            state
        )
        Button {
            click { state.switch() }
            Icon(IconTextures.SWITCH)
        }
        ConfigResetButton(config) {
            doubleValue.setValue(config.getValue())
        }
    }
}