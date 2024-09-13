package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.width
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.widget.DoubleSlider
import moe.forpleuvoir.ibukigourd.gui.widget.FloatSlider
import moe.forpleuvoir.ibukigourd.gui.widget.IntSlider
import moe.forpleuvoir.ibukigourd.gui.widget.LongSlider
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.text.DoubleEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.FloatEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.IntEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.LongEditor
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.config.item.impl.ConfigDouble
import moe.forpleuvoir.nebula.config.item.impl.ConfigFloat
import moe.forpleuvoir.nebula.config.item.impl.ConfigInt
import moe.forpleuvoir.nebula.config.item.impl.ConfigLong

fun WidgetContainerScope.IntConfigWrapper(
    config: ConfigInt,
    modifier: Modifier = Modifier
) = Column(
    modifier,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    val intValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    ConfigTextLabel(config)
    Column {
        IntSlider(intValue, config.minValue..config.maxValue, modifier = Modifier.width(120f))
        IntEditor(intValue, config.minValue..config.maxValue, modifier = Modifier.width(60f), editorModifier = { Modifier.weight(1) })
        ConfigResetButton(config) { c ->
            intValue.setValue(c.getValue())
        }
    }
}

fun WidgetContainerScope.LongConfigWrapper(
    config: ConfigLong,
    modifier: Modifier = Modifier
) = Column(
    modifier,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    val longValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    ConfigTextLabel(config)
    Column {
        LongSlider(longValue, config.minValue..config.maxValue, modifier = Modifier.width(120f))
        LongEditor(longValue, config.minValue..config.maxValue, modifier = Modifier.width(60f), editorModifier = { Modifier.weight(1) })
        ConfigResetButton(config) { c ->
            longValue.setValue(c.getValue())
        }
    }
}

fun WidgetContainerScope.FloatConfigWrapper(
    config: ConfigFloat,
    modifier: Modifier = Modifier
) = Column(
    modifier,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    val floatValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    ConfigTextLabel(config)
    Column {
        FloatSlider(floatValue, config.minValue..config.maxValue, modifier = Modifier.width(120f))
        FloatEditor(floatValue, config.minValue..config.maxValue, modifier = Modifier.width(60f), editorModifier = { Modifier.weight(1) })
        ConfigResetButton(config) { c ->
            floatValue.setValue(c.getValue())
        }
    }
}

fun WidgetContainerScope.DoubleConfigWrapper(
    config: ConfigDouble,
    modifier: Modifier = Modifier
) = Column(
    modifier,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    val doubleValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    ConfigTextLabel(config)
    Column {
        DoubleSlider(doubleValue, config.minValue..config.maxValue, modifier = Modifier.width(120f))
        DoubleEditor(doubleValue, config.minValue..config.maxValue, modifier = Modifier.width(60f), editorModifier = { Modifier.weight(1) })
        ConfigResetButton(config) { c ->
            doubleValue.setValue(c.getValue())
        }
    }
}