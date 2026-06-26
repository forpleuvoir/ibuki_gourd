package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.config.item.range
import moe.forpleuvoir.ibukigourd.ui.preset.DoubleField
import moe.forpleuvoir.ibukigourd.ui.preset.FloatField
import moe.forpleuvoir.ibukigourd.ui.preset.IntField
import moe.forpleuvoir.ibukigourd.ui.preset.LocalNumberFieldStyle
import moe.forpleuvoir.ibukigourd.ui.preset.NumberFieldStyle
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.pathWithRoot
import org.joml.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

val LocalVector2FieldWidthFraction = staticCompositionLocalOf { 2f / 3f }
val LocalVector3FieldWidthFraction = staticCompositionLocalOf { 2f / 4f }

//region Vector2f
@Composable
fun Vector2fConfigWrapper(
    config: Config<Vector2fc>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    var value by remember { mutableStateOf(config.getValue()) }
    val range = remember { config.range }
    val interval = ConfigRowWrapper.valuePollInterval
    var externalUpdateVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(config.pathWithRoot) {
        while (isActive) {
            delay(interval)
            val newValue = config.getValue()
            if (newValue != value) {
                value = newValue
                externalUpdateVersion++
            }
        }
    }

    ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment, onReset = {
        value = config.getValue()
        externalUpdateVersion++
    }) {
        CompositionLocalProvider(LocalNumberFieldStyle provides NumberFieldStyle.Outlined) {
            val fieldWidth = ConfigRowWrapper.entrySize.width * LocalVector2FieldWidthFraction.current

            key(externalUpdateVersion) {
                Row(
                    modifier = Modifier.height(ConfigRowWrapper.entrySize.height),
                    horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val xRange = range?.let { it.first.x()..it.second.x() }
                    FloatField(
                        value = value.x(),
                        onValueChange = { x ->
                            val newValue = Vector2f(x, value.y())
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = xRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (xRange != null) "X [${xRange.start}..${xRange.endInclusive}]" else "X") },
                    )
                    val yRange = range?.let { it.first.y()..it.second.y() }
                    FloatField(
                        value = value.y(),
                        onValueChange = { y ->
                            val newValue = Vector2f(value.x(), y)
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = yRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (yRange != null) "Y [${yRange.start}..${yRange.endInclusive}]" else "Y") },
                    )
                }
            }
        }
    }
}
//endregion

//region Vector2i
@Composable
fun Vector2iConfigWrapper(
    config: Config<Vector2ic>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    var value by remember { mutableStateOf(config.getValue()) }
    val range = remember { config.range }
    val interval = ConfigRowWrapper.valuePollInterval
    var externalUpdateVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(config.pathWithRoot) {
        while (isActive) {
            delay(interval)
            val newValue = config.getValue()
            if (newValue != value) {
                value = newValue
                externalUpdateVersion++
            }
        }
    }

    ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment, onReset = {
        value = config.getValue()
        externalUpdateVersion++
    }) {
        CompositionLocalProvider(LocalNumberFieldStyle provides NumberFieldStyle.Outlined) {
            val fieldWidth = ConfigRowWrapper.entrySize.width * LocalVector2FieldWidthFraction.current

            key(externalUpdateVersion) {
                Row(
                    modifier = Modifier.height(ConfigRowWrapper.entrySize.height),
                    horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val xRange = range?.let { it.first.x()..it.second.x() }
                    IntField(
                        value = value.x(),
                        onValueChange = { x ->
                            val newValue = Vector2i(x, value.y())
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = xRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (xRange != null) "X [${xRange.start}..${xRange.endInclusive}]" else "X") },
                    )
                    val yRange = range?.let { it.first.y()..it.second.y() }
                    IntField(
                        value = value.y(),
                        onValueChange = { y ->
                            val newValue = Vector2i(value.x(), y)
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = yRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (yRange != null) "Y [${yRange.start}..${yRange.endInclusive}]" else "Y") },
                    )
                }
            }
        }
    }
}
//endregion

//region Vector2d
@Composable
fun Vector2dConfigWrapper(
    config: Config<Vector2dc>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    var value by remember { mutableStateOf(config.getValue()) }
    val range = remember { config.range }
    val interval = ConfigRowWrapper.valuePollInterval
    var externalUpdateVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(config.pathWithRoot) {
        while (isActive) {
            delay(interval)
            val newValue = config.getValue()
            if (newValue != value) {
                value = newValue
                externalUpdateVersion++
            }
        }
    }

    ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment, onReset = {
        value = config.getValue()
        externalUpdateVersion++
    }) {
        CompositionLocalProvider(LocalNumberFieldStyle provides NumberFieldStyle.Outlined) {
            val fieldWidth = ConfigRowWrapper.entrySize.width * LocalVector2FieldWidthFraction.current

            key(externalUpdateVersion) {
                Row(
                    modifier = Modifier.height(ConfigRowWrapper.entrySize.height),
                    horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val xRange = range?.let { it.first.x()..it.second.x() }
                    DoubleField(
                        value = value.x(),
                        onValueChange = { x ->
                            val newValue = Vector2d(x, value.y())
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = xRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (xRange != null) "X [${xRange.start}..${xRange.endInclusive}]" else "X") },
                    )
                    val yRange = range?.let { it.first.y()..it.second.y() }
                    DoubleField(
                        value = value.y(),
                        onValueChange = { y ->
                            val newValue = Vector2d(value.x(), y)
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = yRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (yRange != null) "Y [${yRange.start}..${yRange.endInclusive}]" else "Y") },
                    )
                }
            }
        }
    }
}
//endregion

//region Vector3f
@Composable
fun Vector3fConfigWrapper(
    config: Config<Vector3fc>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    var value by remember { mutableStateOf(config.getValue()) }
    val range = remember { config.range }
    val interval = ConfigRowWrapper.valuePollInterval
    var externalUpdateVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(config.pathWithRoot) {
        while (isActive) {
            delay(interval)
            val newValue = config.getValue()
            if (newValue != value) {
                value = newValue
                externalUpdateVersion++
            }
        }
    }

    ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment, onReset = {
        value = config.getValue()
        externalUpdateVersion++
    }) {
        CompositionLocalProvider(LocalNumberFieldStyle provides NumberFieldStyle.Outlined) {
            val fieldWidth = ConfigRowWrapper.entrySize.width * LocalVector3FieldWidthFraction.current

            key(externalUpdateVersion) {
                Row(
                    modifier = Modifier.height(ConfigRowWrapper.entrySize.height),
                    horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val xRange = range?.let { it.first.x()..it.second.x() }
                    FloatField(
                        value = value.x(),
                        onValueChange = { x ->
                            val newValue = Vector3f(x, value.y(), value.z())
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = xRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (xRange != null) "X [${xRange.start}..${xRange.endInclusive}]" else "X") },
                    )
                    val yRange = range?.let { it.first.y()..it.second.y() }
                    FloatField(
                        value = value.y(),
                        onValueChange = { y ->
                            val newValue = Vector3f(value.x(), y, value.z())
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = yRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (yRange != null) "Y [${yRange.start}..${yRange.endInclusive}]" else "Y") },
                    )
                    val zRange = range?.let { it.first.z()..it.second.z() }
                    FloatField(
                        value = value.z(),
                        onValueChange = { z ->
                            val newValue = Vector3f(value.x(), value.y(), z)
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = zRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (zRange != null) "Z [${zRange.start}..${zRange.endInclusive}]" else "Z") },
                    )
                }
            }
        }
    }
}
//endregion

//region Vector3i
@Composable
fun Vector3iConfigWrapper(
    config: Config<Vector3ic>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    var value by remember { mutableStateOf(config.getValue()) }
    val range = remember { config.range }
    val interval = ConfigRowWrapper.valuePollInterval
    var externalUpdateVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(config.pathWithRoot) {
        while (isActive) {
            delay(interval)
            val newValue = config.getValue()
            if (newValue != value) {
                value = newValue
                externalUpdateVersion++
            }
        }
    }

    ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment, onReset = {
        value = config.getValue()
        externalUpdateVersion++
    }) {
        CompositionLocalProvider(LocalNumberFieldStyle provides NumberFieldStyle.Outlined) {
            val fieldWidth = ConfigRowWrapper.entrySize.width * LocalVector3FieldWidthFraction.current

            key(externalUpdateVersion) {
                Row(
                    modifier = Modifier.height(ConfigRowWrapper.entrySize.height),
                    horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val xRange = range?.let { it.first.x()..it.second.x() }
                    IntField(
                        value = value.x(),
                        onValueChange = { x ->
                            val newValue = Vector3i(x, value.y(), value.z())
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = xRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (xRange != null) "X [${xRange.start}..${xRange.endInclusive}]" else "X") },
                    )
                    val yRange = range?.let { it.first.y()..it.second.y() }
                    IntField(
                        value = value.y(),
                        onValueChange = { y ->
                            val newValue = Vector3i(value.x(), y, value.z())
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = yRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (yRange != null) "Y [${yRange.start}..${yRange.endInclusive}]" else "Y") },
                    )
                    val zRange = range?.let { it.first.z()..it.second.z() }
                    IntField(
                        value = value.z(),
                        onValueChange = { z ->
                            val newValue = Vector3i(value.x(), value.y(), z)
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = zRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (zRange != null) "Z [${zRange.start}..${zRange.endInclusive}]" else "Z") },
                    )
                }
            }
        }
    }
}
//endregion

//region Vector3d
@Composable
fun Vector3dConfigWrapper(
    config: Config<Vector3dc>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    var value by remember { mutableStateOf(config.getValue()) }
    val range = remember { config.range }
    val interval = ConfigRowWrapper.valuePollInterval
    var externalUpdateVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(config.pathWithRoot) {
        while (isActive) {
            delay(interval)
            val newValue = config.getValue()
            if (newValue != value) {
                value = newValue
                externalUpdateVersion++
            }
        }
    }

    ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment, onReset = {
        value = config.getValue()
        externalUpdateVersion++
    }) {
        CompositionLocalProvider(LocalNumberFieldStyle provides NumberFieldStyle.Outlined) {
            val fieldWidth = ConfigRowWrapper.entrySize.width * LocalVector3FieldWidthFraction.current

            key(externalUpdateVersion) {
                Row(
                    modifier = Modifier.height(ConfigRowWrapper.entrySize.height),
                    horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val xRange = range?.let { it.first.x()..it.second.x() }
                    DoubleField(
                        value = value.x(),
                        onValueChange = { x ->
                            val newValue = Vector3d(x, value.y(), value.z())
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = xRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (xRange != null) "X [${xRange.start}..${xRange.endInclusive}]" else "X") },
                    )
                    val yRange = range?.let { it.first.y()..it.second.y() }
                    DoubleField(
                        value = value.y(),
                        onValueChange = { y ->
                            val newValue = Vector3d(value.x(), y, value.z())
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = yRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (yRange != null) "Y [${yRange.start}..${yRange.endInclusive}]" else "Y") },
                    )
                    val zRange = range?.let { it.first.z()..it.second.z() }
                    DoubleField(
                        value = value.z(),
                        onValueChange = { z ->
                            val newValue = Vector3d(value.x(), value.y(), z)
                            config.setValue(newValue)
                            value = newValue
                        },
                        range = zRange,
                        modifier = Modifier.width(fieldWidth),
                        label = { Text(if (zRange != null) "Z [${zRange.start}..${zRange.endInclusive}]" else "Z") },
                    )
                }
            }
        }
    }
}
//endregion
