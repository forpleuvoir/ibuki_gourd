package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.ui.sokitsu.DurationField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.DurationSlider
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.ConfigRange
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 时长：有区间时在「滑条」与「时长框」之间切换（同 [IntConfigWrapper] 的切换条），否则只给时长框。
 *
 * 滑条只在区间跨度落在 `1s..600s` 时给出：跨度过大时逐格拖动没有意义，
 * 跨度为 0（固定值）时也没有可拖的余地。
 *
 * @param config 时长配置项
 * @param modifier 作用于整行
 */
@Composable
fun DurationConfigWrapper(config: Config<Duration>, modifier: Modifier = Modifier) {
    val value by config.asState()
    val range = (config as? ConfigRange<Duration>)?.let { it.minValue..it.maxValue }
    val span = range?.let { it.endInclusive - it.start }
    val slider: (@Composable RowScope.() -> Unit)? =
        if (range != null && span != null && span in 1.seconds..SliderMaxSpan) {
            {
                DurationSlider(
                    value = value,
                    onValueChange = { config.setValue(it) },
                    valueRange = range,
                    modifier = Modifier.weight(1f).height(configControlHeight()),
                )
            }
        } else {
            null
        }

    NumberRow(config, modifier, slider) {
        DurationField(
            value = value,
            onValueChange = { config.setValue(it) },
            valueRange = range,
            modifier = Modifier.weight(1f),
        )
    }
}

/** 时长滑条可用的最大区间跨度。 */
private val SliderMaxSpan: Duration = 600.seconds
