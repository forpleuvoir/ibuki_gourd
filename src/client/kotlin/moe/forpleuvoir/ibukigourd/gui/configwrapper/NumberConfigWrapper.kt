package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.widget.*
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.text.DoubleEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.FloatEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.IntEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.LongEditor
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.switch
import moe.forpleuvoir.nebula.config.item.impl.ConfigDouble
import moe.forpleuvoir.nebula.config.item.impl.ConfigFloat
import moe.forpleuvoir.nebula.config.item.impl.ConfigInt
import moe.forpleuvoir.nebula.config.item.impl.ConfigLong

private const val EDITOR_WIDTH = 120f

fun ContainerScope.IntConfigWrapper(
    config: ConfigInt,
    modifier: Modifier = Modifier,
    width: Float = EDITOR_WIDTH,
    textMapper: (Int) -> Text = { Literal(it.toString()) }
) = ConfigRowWrapper(config, modifier) {
    val intValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        val state = mutableStateOf(true)
        SwitchableProxy(
            { IntSlider(intValue, config.minValue..config.maxValue, textMapper = textMapper, modifier = Modifier.width(width)) },
            { IntEditor(intValue, config.minValue..config.maxValue, modifier = Modifier.width(width), editorModifier = { Modifier.weight(1) }) },
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

fun ContainerScope.LongConfigWrapper(
    config: ConfigLong,
    modifier: Modifier = Modifier,
    width: Float = EDITOR_WIDTH,
    textMapper: (Long) -> Text = { Literal(it.toString()) }
) = ConfigRowWrapper(config, modifier) {
    val longValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        val state = mutableStateOf(true)
        SwitchableProxy(
            { LongSlider(longValue, config.minValue..config.maxValue, textMapper = textMapper, modifier = Modifier.width(width)) },
            { LongEditor(longValue, config.minValue..config.maxValue, modifier = Modifier.width(width), editorModifier = { Modifier.weight(1) }) },
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

fun ContainerScope.FloatConfigWrapper(
    config: ConfigFloat,
    modifier: Modifier = Modifier,
    width: Float = EDITOR_WIDTH,
    textMapper: (Float) -> Text = { Literal("%.2f".format(it)) }
) = ConfigRowWrapper(config, modifier) {
    val floatValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        val state = mutableStateOf(true)
        SwitchableProxy(
            { FloatSlider(floatValue, config.minValue..config.maxValue, textMapper = textMapper, modifier = Modifier.width(width)) },
            { FloatEditor(floatValue, config.minValue..config.maxValue, modifier = Modifier.width(width), editorModifier = { Modifier.weight(1) }) },
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

fun ContainerScope.DoubleConfigWrapper(
    config: ConfigDouble,
    modifier: Modifier = Modifier,
    width: Float = EDITOR_WIDTH,
    textMapper: (Double) -> Text = { Literal("%.2f".format(it)) }
) = ConfigRowWrapper(config, modifier) {
    val doubleValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        val state = mutableStateOf(true)
        SwitchableProxy(
            { DoubleSlider(doubleValue, config.minValue..config.maxValue, textMapper = textMapper, modifier = Modifier.width(width)) },
            { DoubleEditor(doubleValue, config.minValue..config.maxValue, modifier = Modifier.width(width), editorModifier = { Modifier.weight(1) }) },
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
