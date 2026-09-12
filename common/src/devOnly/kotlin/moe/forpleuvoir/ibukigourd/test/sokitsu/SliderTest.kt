package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.DurationSlider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IntSlider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.PercentSlider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Slider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuTone
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun SliderTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 连续滑条：轨道内居中显示百分比（文字被进度边界切成两色）
                var continuous by remember { mutableStateOf(0.35f) }
                Slider(
                    value = continuous,
                    onValueChange = { continuous = it },
                    modifier = Modifier.width(240.dp),
                    label = { Text("${(continuous * 100).roundToInt()}%") },
                )

                // 离散滑条：0..10 共 11 档（steps = 10），拖动吸附到整档
                var discrete by remember { mutableStateOf(3f) }
                Slider(
                    value = discrete,
                    onValueChange = { discrete = it },
                    valueRange = 0f..10f,
                    steps = 10,
                    modifier = Modifier.width(240.dp),
                    label = { Text("${discrete.roundToInt()} / 10") },
                )

                // 无标签：只剩轨道 + 填充两段颜色
                var bare by remember { mutableStateOf(0.7f) }
                Slider(
                    value = bare,
                    onValueChange = { bare = it },
                    modifier = Modifier.width(160.dp),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 禁用态：不可交互、配色压暗
                    Slider(
                        value = 0.5f,
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier.width(160.dp),
                        label = { Text("禁用") },
                    )

                    // 只读展示：onValueChange = null，仍绘制但不接收手势
                    Slider(
                        value = 1f,
                        onValueChange = null,
                        modifier = Modifier.width(160.dp),
                        label = { Text("只读") },
                    )
                }

                // 作用域换色板：轨道与填充应随 secondary 走，文字色自动跟随
                var secondary by remember { mutableStateOf(0.6f) }
                CompositionLocalProvider(LocalSokitsuTone provides SokitsuTheme.colorScheme.secondary) {
                    Slider(
                        value = secondary,
                        onValueChange = { secondary = it },
                        modifier = Modifier.width(240.dp),
                        label = { Text("secondary") },
                    )
                }

                // 负区间 + 浮点文本：确认受控值与格式化显示
                var echo by remember { mutableStateOf(0f) }
                Slider(
                    value = echo,
                    onValueChange = { echo = it },
                    valueRange = -1f..1f,
                    modifier = Modifier.width(240.dp),
                    label = { Text("%.2f".format(echo)) },
                )

                // 类型化滑条：整数档吸附 + 百分比 + 时长
                var count by remember { mutableStateOf(32) }
                IntSlider(
                    value = count,
                    onValueChange = { count = it },
                    valueRange = 0..100,
                    modifier = Modifier.width(240.dp),
                )
                var volume by remember { mutableStateOf(0.75f) }
                PercentSlider(
                    value = volume,
                    onValueChange = { volume = it },
                    steps = 99,
                    modifier = Modifier.width(240.dp),
                )
                var duration by remember { mutableStateOf(90.seconds) }
                DurationSlider(
                    value = duration,
                    onValueChange = { duration = it },
                    valueRange = Duration.ZERO..(10.minutes),
                    modifier = Modifier.width(240.dp),
                )
            }
        }
    }
}
