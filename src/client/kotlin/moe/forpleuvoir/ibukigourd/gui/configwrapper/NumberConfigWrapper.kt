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
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        IntSlider(intValue, config.minValue..config.maxValue, modifier = Modifier.width(120f))
        IntEditor(intValue, config.minValue..config.maxValue, modifier = Modifier.width(60f), editorModifier = { Modifier.weight(1) })
        ConfigResetButton(config, intValue)
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
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        LongSlider(longValue, config.minValue..config.maxValue, modifier = Modifier.width(120f))
        LongEditor(longValue, config.minValue..config.maxValue, modifier = Modifier.width(60f), editorModifier = { Modifier.weight(1) })
        ConfigResetButton(config, longValue)
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
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        FloatSlider(floatValue, config.minValue..config.maxValue, modifier = Modifier.width(120f))
        FloatEditor(floatValue, config.minValue..config.maxValue, modifier = Modifier.width(60f), editorModifier = { Modifier.weight(1) })
        ConfigResetButton(config, floatValue)
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
    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {
        DoubleSlider(doubleValue, config.minValue..config.maxValue, modifier = Modifier.width(120f))
        DoubleEditor(doubleValue, config.minValue..config.maxValue, modifier = Modifier.width(60f), editorModifier = { Modifier.weight(1) })
        ConfigResetButton(config, doubleValue)
    }
}