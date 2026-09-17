package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.render.extension.AnchorPosition
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip.tooltip

fun TooltipTestScreen() = TestScreen {
    // 背景交给 Surface：铺 surface 色板并下发 onSurface 内容色
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).width(360.dp)) {
            Text("气泡（Tooltip）测试")
            Spacer(Modifier.height(16.dp))

            Text("方向演示（悬停触发）")
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnchorDemo(anchor = AnchorPosition.Above, label = "上方")
                AnchorDemo(anchor = AnchorPosition.Below, label = "下方")
                AnchorDemo(anchor = AnchorPosition.Left, label = "左侧")
                AnchorDemo(anchor = AnchorPosition.Right, label = "右侧")
            }

            Spacer(Modifier.height(24.dp))
            Text("可滚动列表（悬停某项后滚动，看气泡是否跟随目标移动）")
            Spacer(Modifier.height(12.dp))

            // 滚动容器：每项悬停弹出气泡。目标随滚动移动时，
            // 若气泡位置计算器不刷新，气泡会冻结在原落点。
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                repeat(40) { i ->
                    val n = i + 1
                    Button(
                        {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .tooltip(position = AnchorPosition.Below) {
                                Text("这是第 $n 项的提示..................\n换行测试")
                            },
                    ) { Text("第 $n 项") }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

/**
 * 单个方向的悬停气泡演示：按钮为锚，悬停时经 [tooltip] 在 [anchor] 侧弹出
 * [moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.BubblePanel] 气泡（箭头朝按钮、缺口开在气泡对应边）。
 */
@Composable
private fun AnchorDemo(anchor: AnchorPosition, label: String) {
    Button({}, modifier = Modifier.tooltip(position = anchor) {
        Text("这是${label}的工具提示..................\n换行测试")
    }) { Text("悬停看${label}气泡") }
}
