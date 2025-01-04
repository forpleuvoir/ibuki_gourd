package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.item.ConfigVector2f
import moe.forpleuvoir.ibukigourd.config.item.ConfigVector3f
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.widget.*
import moe.forpleuvoir.ibukigourd.gui.widget.button.Button
import moe.forpleuvoir.ibukigourd.gui.widget.icon.Icon
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Column
import moe.forpleuvoir.ibukigourd.gui.widget.text.*
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.util.math.copy
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.switch
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.config.item.impl.ConfigDouble
import moe.forpleuvoir.nebula.config.item.impl.ConfigFloat
import moe.forpleuvoir.nebula.config.item.impl.ConfigInt
import moe.forpleuvoir.nebula.config.item.impl.ConfigLong

private const val EDITOR_WIDTH = 120f
private const val VECTOR_EDITOR_WIDTH = 60f

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
            { IntSlider(intValue, config.minValue..config.maxValue, modifier = Modifier.width(EDITOR_WIDTH)) },
            { IntEditor(intValue, config.minValue..config.maxValue, modifier = Modifier.width(EDITOR_WIDTH), editorModifier = { Modifier.weight(1) }) },
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
            { LongSlider(longValue, config.minValue..config.maxValue, modifier = Modifier.width(EDITOR_WIDTH)) },
            { LongEditor(longValue, config.minValue..config.maxValue, modifier = Modifier.width(EDITOR_WIDTH), editorModifier = { Modifier.weight(1) }) },
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
            { FloatSlider(floatValue, config.minValue..config.maxValue, modifier = Modifier.width(EDITOR_WIDTH)) },
            { FloatEditor(floatValue, config.minValue..config.maxValue, modifier = Modifier.width(EDITOR_WIDTH), editorModifier = { Modifier.weight(1) }) },
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
            { DoubleSlider(doubleValue, config.minValue..config.maxValue, modifier = Modifier.width(EDITOR_WIDTH)) },
            { DoubleEditor(doubleValue, config.minValue..config.maxValue, modifier = Modifier.width(EDITOR_WIDTH), editorModifier = { Modifier.weight(1) }) },
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


fun WidgetContainerScope.ConfigVector2fWrapper(
    config: ConfigVector2f,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val vector2fValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }

    val xValue = mutableStateOf(vector2fValue.getValue().x()).apply {
        subscribe {
            vector2fValue.setValue(vector2fValue.getValue().copy(x = it))
        }
    }
    val yValue = mutableStateOf(vector2fValue.getValue().y()).apply {
        subscribe {
            vector2fValue.setValue(vector2fValue.getValue().copy(y = it))
        }
    }

    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {

        Column(horizontalArrangement = Arrangement.spacedBy(2f)) {
            TextLabel(Literal("X").withColor(Colors.RED))
            FloatEditor(
                xValue,
                config.minValue.x()..config.maxValue.x(),
                modifier = Modifier.width(VECTOR_EDITOR_WIDTH),
                editorModifier = { Modifier.weight(1) })
        }

        Column(horizontalArrangement = Arrangement.spacedBy(2f)) {
            TextLabel(Literal("Y").withColor(Colors.LIME))
            FloatEditor(
                yValue,
                config.minValue.y()..config.maxValue.y(),
                modifier = Modifier.width(VECTOR_EDITOR_WIDTH),
                editorModifier = { Modifier.weight(1) })
        }

        ConfigResetButton(config) {
            vector2fValue.setValue(config.getValue())
            xValue.setValue(vector2fValue.getValue().x())
            yValue.setValue(vector2fValue.getValue().y())
        }
    }

}


fun WidgetContainerScope.ConfigVector3fWrapper(
    config: ConfigVector3f,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val vector3fValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }

    val xValue = mutableStateOf(vector3fValue.getValue().x()).apply {
        subscribe {
            vector3fValue.setValue(vector3fValue.getValue().copy(x = it))
        }
    }
    val yValue = mutableStateOf(vector3fValue.getValue().y()).apply {
        subscribe {
            vector3fValue.setValue(vector3fValue.getValue().copy(y = it))
        }
    }
    val zValue = mutableStateOf(vector3fValue.getValue().z()).apply {
        subscribe {
            vector3fValue.setValue(vector3fValue.getValue().copy(z = it))
        }
    }

    Column(
        horizontalArrangement = Arrangement.spacedBy(5f)
    ) {

        Column(horizontalArrangement = Arrangement.spacedBy(2f)) {
            TextLabel(Literal("X").withColor(Colors.RED))
            FloatEditor(
                xValue,
                config.minValue.x()..config.maxValue.x(),
                modifier = Modifier.width(VECTOR_EDITOR_WIDTH),
                editorModifier = { Modifier.weight(1) })
        }

        Column(horizontalArrangement = Arrangement.spacedBy(2f)) {
            TextLabel(Literal("Y").withColor(Colors.LIME))
            FloatEditor(
                yValue,
                config.minValue.y()..config.maxValue.y(),
                modifier = Modifier.width(VECTOR_EDITOR_WIDTH),
                editorModifier = { Modifier.weight(1) })
        }

        Column(horizontalArrangement = Arrangement.spacedBy(2f)) {
            TextLabel(Literal("Z").withColor(Colors.BLUE))
            FloatEditor(
                zValue,
                config.minValue.z()..config.maxValue.z(),
                modifier = Modifier.width(VECTOR_EDITOR_WIDTH),
                editorModifier = { Modifier.weight(1) })
        }


        ConfigResetButton(config) {
            vector3fValue.setValue(config.getValue())
            xValue.setValue(vector3fValue.getValue().x())
            yValue.setValue(vector3fValue.getValue().y())
            zValue.setValue(vector3fValue.getValue().z())
        }
    }

}