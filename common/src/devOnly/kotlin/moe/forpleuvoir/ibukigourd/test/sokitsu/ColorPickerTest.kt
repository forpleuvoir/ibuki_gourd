package moe.forpleuvoir.ibukigourd.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.colorpicker.ColorPickButton
import moe.forpleuvoir.ibukigourd.ui.colorpicker.ColorPicker
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text

/**
 * 取色器测试屏。
 *
 * 验证点：
 * 1. 通道条拖拽（按下即定位 + 拖动跟随），H/S/V 与 R/G/B/A 两套 tab；
 * 2. **受控回灌**：外部按钮直接改颜色，取色器所有通道应立即跟着变（不是本地状态）；
 * 3. **色相兜底**：先"外部置黑"再把亮度拉起来，色相条不应跳回 0；
 * 4. alpha 条带棋盘，调低 alpha 时预览方块能透出棋盘；
 * 5. 色值按钮点击写入剪贴板。
 */
@Composable
fun ColorPickerTestContent() {
    var color by remember { mutableStateOf(Color(0xFF8647B3)) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("当前颜色：$color")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button({ color = Color(0xFFD32F2F) }) { Text("外部置红") }
            Button({ color = Color(0xFF388E3C) }) { Text("外部置绿") }
            Button({ color = Color(0xFF000000) }) { Text("外部置黑") }
            Button({ color = Color(0x808647B3) }) { Text("外部半透明") }
        }

        // ColorPickButton：按钮底色即当前色，点击弹窗编辑；弹窗内改的是副本，确认才提交
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("ColorPickButton：")
            ColorPickButton(
                color = color,
                onValueChange = { color = it },
                title = { Text("取色") },
            )
            Text("（取消应丢弃改动）")
        }

        ColorPicker(color = color, onValueChange = { color = it })
    }
}

fun ColorPickerTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            ColorPickerTestContent()
        }
    }
}
