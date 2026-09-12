package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.compose_minecraft.platform.ui.text.MinecraftFonts
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.LocalTextStyle
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TextField
import net.minecraft.network.chat.Style

fun TextFieldTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 常规输入：聚焦描边、光标主色
                val normal = rememberTextFieldState()
                TextField(
                    state = normal,
                    modifier = Modifier.width(240.dp),
                )
                Text("echo = ${normal.text}")

                // 头部 label + 尾部图标（对齐旧版 M3 labelPosition.Attached 用法）
                val prefixed = rememberTextFieldState("128")
                TextField(
                    state = prefixed,
                    modifier = Modifier.width(240.dp),
                    label = { Text("width") },
                    trailingIcon = { Text("px") },
                )

                // 错误态：描边染错误色
                val error = rememberTextFieldState("illegal value")
                TextField(
                    state = error,
                    modifier = Modifier.width(240.dp),
                    isError = true,
                )

                // 只读：可选择/复制，不可编辑
                TextField(
                    state = rememberTextFieldState("read only"),
                    modifier = Modifier.width(240.dp),
                    readOnly = true,
                )

                // 禁用：整体压暗，不接收输入
                TextField(
                    state = rememberTextFieldState("disabled"),
                    modifier = Modifier.width(240.dp),
                    enabled = false,
                )

                // 多行：3 行限高，显式给高让三行都能显示；预填三行验证渲染（清空后仍应可点选）
                TextField(
                    state = rememberTextFieldState("first line\nsecond line\nthird line"),
                    modifier = Modifier.width(240.dp).height(116.dp),
                    lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 1, maxHeightInLines = 3),
                    textStyle = Style.EMPTY.withFont(MinecraftFonts.FusionPixelMono)
                )
            }
        }
    }
}
