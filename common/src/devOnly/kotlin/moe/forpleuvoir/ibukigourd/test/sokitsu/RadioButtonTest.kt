package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.RadioButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.RadioButtonGroup
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text

fun RadioButtonTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                var selected by remember { mutableStateOf(0) }

                // 三段组：left / center / right 各一段
                RadioButtonGroup(selected = selected, onSelect = { selected = it }) {
                    item { Text("选项 A") }
                    item { Text("选项 B") }
                    item { Text("选项 C") }
                }

                // 四段 + 中间禁用：验证 center 连续段与禁用态
                var selectedWithDisabled by remember { mutableStateOf(1) }
                RadioButtonGroup(selected = selectedWithDisabled, onSelect = { selectedWithDisabled = it }) {
                    item { Text("甲") }
                    item { Text("乙") }
                    item(enabled = false) { Text("丙(禁用)") }
                    item { Text("丁") }
                }

                // 单按钮：走 singleSprite；选中后落禁用态，不可再点
                var single by remember { mutableStateOf(false) }
                RadioButton(selected = single, onSelect = { single = true }) {
                    Text("独立单选")
                }

                // RTL：Row 自动镜像摆放，left 纹理应仍出现在视觉最左端
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    RadioButtonGroup(selected = selected, onSelect = { selected = it }) {
                        item { Text("RTL A") }
                        item { Text("RTL B") }
                        item { Text("RTL C") }
                    }
                }
            }
        }
    }
}
