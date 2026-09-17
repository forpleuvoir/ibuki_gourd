package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.ProgressBar
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Slider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text

/**
 * 进度条测试屏：验证轨道 / 填充、边界值与回退链。
 *
 * 验证点：
 * 1. 交互 —— 滑条实时驱动，确认填充宽度跟随；
 * 2. 边界 —— `0f` 只剩轨道、`1f` 填满；越界值被钳制（滑条不会越界，静态项覆盖 `0` / `1`）；
 * 3. 颜色 —— 调用点传参（填充 + 轨道）优先于组件 token；
 * 4. 无轨道 —— `trackColor` 传 [Color.Transparent] 时只剩填充块；
 * 5. 宽度 —— 固定宽度时填充按该宽度换算，不铺满面板。
 *
 * 高度取自 `progress_bar` meta（默认 4dp），不受 pixel_scale 影响。
 */
@Composable
fun ProgressBarTestContent() {
    var value by remember { mutableStateOf(0.5f) }

    Column(
        modifier = Modifier.width(360.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("拖动滑条控制（当前 ${"%.2f".format(value)}）")
        Slider(
            value = value,
            onValueChange = { value = it },
            modifier = Modifier.width(300.dp),
        )
        ProgressBar(value)

        Text("静态取值：0 / 0.25 / 0.5 / 1")
        listOf(0f, 0.25f, 0.5f, 1f).forEach { ProgressBar(it) }

        Text("自定义颜色（填充 + 轨道）")
        ProgressBar(value, color = Color(0xFF7ED957), trackColor = Color(0x33444444))

        Text("无轨道（trackColor 传透明）")
        ProgressBar(value, trackColor = Color.Transparent)

        Text("固定宽度 160dp")
        ProgressBar(value, Modifier.width(160.dp))
    }
}

fun ProgressBarTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            ProgressBarTestContent()
        }
    }
}
