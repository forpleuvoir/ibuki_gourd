package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import moe.forpleuvoir.ibukigourd.ui.sokitsu.DoubleField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.DoubleSlider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FloatField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FloatSlider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IntField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IntSlider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.LongField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.LongSlider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Switch
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.ConfigNode
import moe.forpleuvoir.nebula.config.item.ConfigRange

/**
 * 数值行：区间可滑动时在「滑条」与「数值框」两种编辑器之间切换，否则只给数值框。
 *
 * 与旧版的差别：旧版还有一段"外部值变化时滑条平滑滑过去"的 `Animatable` 补间，本版去掉 ——
 * sokitsu 的滑条是**无状态**的（值由外部驱动），补间需要额外维护拖拽态，收益不抵复杂度。
 *
 * @param config 行对应的配置节点（取名称与注释）
 * @param modifier 作用于整行
 * @param slider 滑条编辑器；为 null 表示区间不可滑动，只显示 [field]
 * @param field 数值框编辑器
 */
@Composable
internal fun NumberRow(
    config: ConfigNode,
    modifier: Modifier = Modifier,
    slider: (@Composable RowScope.() -> Unit)?,
    field: @Composable RowScope.() -> Unit,
) = ConfigRowWrapper(config, modifier) {
    if (slider == null) {
        field()
        return@ConfigRowWrapper
    }

    var sliderMode by remember(config) { mutableStateOf(true) }
    val rotation by animateFloatAsState(if (sliderMode) 0f else 180f)
    if (sliderMode) slider() else field()
    IconButton(
        onClick = { sliderMode = !sliderMode },
        modifier = Modifier.size(ConfigControlDefaults.IconButtonSize),
    ) {
        Icon(Icons.SyncAlt, scale = ConfigRowDefaults.IconScale, modifier = Modifier.rotate(rotation))
    }
}

/** 布尔：开关。 */
@Composable
fun BooleanConfigWrapper(config: Config<Boolean>, modifier: Modifier = Modifier) {
    val value by config.asState()
    ConfigRowWrapper(config, modifier) {
        Switch(
            checked = value,
            onCheckedChange = { config.setValue(it) },
        )
    }
}

/** 整数：有不超过 [ConfigControlDefaults.SliderSpanLimit] 的区间时给滑条，否则只给数值框。 */
@Composable
fun IntConfigWrapper(config: Config<Int>, modifier: Modifier = Modifier) {
    val value by config.asState()
    val range = (config as? ConfigRange<Int>)?.let { it.minValue..it.maxValue }
    val span = range?.let { it.last - it.first }
    val slider: (@Composable RowScope.() -> Unit)? =
        if (range != null && span != null && span in 1 until ConfigControlDefaults.SliderSpanLimit) {
            { IntSlider(value, { config.setValue(it) }, valueRange = range, modifier = Modifier.width(ConfigControlDefaults.SliderWidth)) }
        } else {
            null
        }
    NumberRow(config, modifier, slider) {
        IntField(
            value = value,
            onValueChange = { config.setValue(it) },
            valueRange = range,
            modifier = Modifier.width(ConfigControlDefaults.FieldWidth),
        )
    }
}

/** 长整数：同 [IntConfigWrapper]。 */
@Composable
fun LongConfigWrapper(config: Config<Long>, modifier: Modifier = Modifier) {
    val value by config.asState()
    val range = (config as? ConfigRange<Long>)?.let { it.minValue..it.maxValue }
    val span = range?.let { it.last - it.first }
    val slider: (@Composable RowScope.() -> Unit)? =
        if (range != null && span != null && span in 1 until ConfigControlDefaults.SliderSpanLimit.toLong()) {
            { LongSlider(value, { config.setValue(it) }, valueRange = range, modifier = Modifier.width(ConfigControlDefaults.SliderWidth)) }
        } else {
            null
        }
    NumberRow(config, modifier, slider) {
        LongField(
            value = value,
            onValueChange = { config.setValue(it) },
            valueRange = range,
            modifier = Modifier.width(ConfigControlDefaults.FieldWidth),
        )
    }
}

/** 单精度浮点：区间有限且跨度小于 [ConfigControlDefaults.SliderSpanLimit] 时给滑条。 */
@Composable
fun FloatConfigWrapper(config: Config<Float>, modifier: Modifier = Modifier) {
    val value by config.asState()
    val range = (config as? ConfigRange<Float>)?.let { it.minValue..it.maxValue }
    val span = range?.let { it.endInclusive - it.start }
    val slider: (@Composable RowScope.() -> Unit)? =
        if (range != null && span != null && span.isFinite() && span > 0f && span < ConfigControlDefaults.SliderSpanLimit) {
            { FloatSlider(value, { config.setValue(it) }, valueRange = range, modifier = Modifier.width(ConfigControlDefaults.SliderWidth)) }
        } else {
            null
        }
    NumberRow(config, modifier, slider) {
        FloatField(
            value = value,
            onValueChange = { config.setValue(it) },
            valueRange = range,
            modifier = Modifier.width(ConfigControlDefaults.FieldWidth),
        )
    }
}

/** 双精度浮点：同 [FloatConfigWrapper]。 */
@Composable
fun DoubleConfigWrapper(config: Config<Double>, modifier: Modifier = Modifier) {
    val value by config.asState()
    val range = (config as? ConfigRange<Double>)?.let { it.minValue..it.maxValue }
    val span = range?.let { it.endInclusive - it.start }
    val slider: (@Composable RowScope.() -> Unit)? =
        if (range != null && span != null && span.isFinite() && span > 0f && span < ConfigControlDefaults.SliderSpanLimit) {
            { DoubleSlider(value, { config.setValue(it) }, valueRange = range, modifier = Modifier.width(ConfigControlDefaults.SliderWidth)) }
        } else {
            null
        }
    NumberRow(config, modifier, slider) {
        DoubleField(
            value = value,
            onValueChange = { config.setValue(it) },
            valueRange = range,
            modifier = Modifier.width(ConfigControlDefaults.FieldWidth),
        )
    }
}
