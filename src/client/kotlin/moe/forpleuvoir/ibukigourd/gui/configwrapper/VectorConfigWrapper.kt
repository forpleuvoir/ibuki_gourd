package moe.forpleuvoir.ibukigourd.gui.configwrapper

import moe.forpleuvoir.ibukigourd.config.item.*
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.widget.*
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf

private const val VECTOR_EDITOR_WIDTH = 60f

//------------ Vector2 ------------\\

fun WidgetContainerScope.ConfigVector2iWrapper(
    config: ConfigVector2i,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val vector2iValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    Vector2iEditor(
        vector2iValue,
        config.minValue,
        config.maxValue,
        Modifier,
        Modifier.width(VECTOR_EDITOR_WIDTH),
        Arrangement.spacedBy(5f)
    ) { xValue, yValue ->
        ConfigResetButton(config) {
            vector2iValue.setValue(config.getValue())
            xValue.setValue(vector2iValue.getValue().x())
            yValue.setValue(vector2iValue.getValue().y())
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
    Vector2fEditor(
        vector2fValue,
        config.minValue,
        config.maxValue,
        Modifier,
        Modifier.width(VECTOR_EDITOR_WIDTH),
        Arrangement.spacedBy(5f)
    ) { xValue, yValue ->
        ConfigResetButton(config) {
            vector2fValue.setValue(config.getValue())
            xValue.setValue(vector2fValue.getValue().x())
            yValue.setValue(vector2fValue.getValue().y())
        }
    }
}

fun WidgetContainerScope.ConfigVector2dWrapper(
    config: ConfigVector2d,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val vector2dValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    Vector2dEditor(
        vector2dValue,
        config.minValue,
        config.maxValue,
        Modifier,
        Modifier.width(VECTOR_EDITOR_WIDTH),
        Arrangement.spacedBy(5f)
    ) { xValue, yValue ->
        ConfigResetButton(config) {
            vector2dValue.setValue(config.getValue())
            xValue.setValue(vector2dValue.getValue().x())
            yValue.setValue(vector2dValue.getValue().y())
        }
    }
}

//------------ Vector3 ------------\\

fun WidgetContainerScope.ConfigVector3iWrapper(
    config: ConfigVector3i,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val vector3iValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    Vector3iEditor(
        vector3iValue,
        config.minValue,
        config.maxValue,
        Modifier,
        Modifier.width(VECTOR_EDITOR_WIDTH),
        Arrangement.spacedBy(5f)
    ) { xValue, yValue, zValue ->
        ConfigResetButton(config) {
            vector3iValue.setValue(config.getValue())
            xValue.setValue(vector3iValue.getValue().x())
            yValue.setValue(vector3iValue.getValue().y())
            zValue.setValue(vector3iValue.getValue().z())
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
    Vector3fEditor(
        vector3fValue,
        config.minValue,
        config.maxValue,
        Modifier,
        Modifier.width(VECTOR_EDITOR_WIDTH),
        Arrangement.spacedBy(5f)
    ) { xValue, yValue, zValue ->
        ConfigResetButton(config) {
            vector3fValue.setValue(config.getValue())
            xValue.setValue(vector3fValue.getValue().x())
            yValue.setValue(vector3fValue.getValue().y())
            zValue.setValue(vector3fValue.getValue().z())
        }
    }
}

fun WidgetContainerScope.ConfigVector3dWrapper(
    config: ConfigVector3d,
    modifier: Modifier = Modifier
) = ConfigColumnWrapper(config, modifier) {
    val vector3dValue = mutableStateOf(config.getValue()).apply {
        subscribe {
            config.setValue(it)
        }
    }
    Vector3dEditor(
        vector3dValue,
        config.minValue,
        config.maxValue,
        Modifier,
        Modifier.width(VECTOR_EDITOR_WIDTH),
        Arrangement.spacedBy(5f)
    ) { xValue, yValue, zValue ->
        ConfigResetButton(config) {
            vector3dValue.setValue(config.getValue())
            xValue.setValue(vector3dValue.getValue().x())
            yValue.setValue(vector3dValue.getValue().y())
            zValue.setValue(vector3dValue.getValue().z())
        }
    }
}