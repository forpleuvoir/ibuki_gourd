package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.render.extension.AnchorPosition
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip.tooltip

fun TooltipTestScreen() = TestScreen {
    // 背景交给 Surface：铺 surface 色板并下发 onSurface 内容色
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            Column(
                verticalArrangement = Arrangement.spacedBy(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnchorDemo(anchor = AnchorPosition.Above, label = "上方")
                AnchorDemo(anchor = AnchorPosition.Below, label = "下方")
                AnchorDemo(anchor = AnchorPosition.Left, label = "左侧")
                AnchorDemo(anchor = AnchorPosition.Right, label = "右侧")
            }
        }
    }
}

/**
 * 单个方向的悬停气泡演示：按钮为锚，悬停时经 [TooltipBox] 在 [anchor] 侧弹出
 * [Tooltip] 气泡（箭头朝按钮、缺口开在气泡对应边）。
 */
@Composable
private fun AnchorDemo(anchor: AnchorPosition, label: String) {
    Button({}, modifier = Modifier.tooltip(position = anchor) {
        Text(label)
    }) { Text("悬停看${label}气泡") }
}
