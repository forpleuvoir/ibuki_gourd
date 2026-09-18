package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.selector.SelectorTrigger
import moe.forpleuvoir.ibukigourd.ui.selector.SelectorTriggerDefaults

/**
 * 选择器触发件测试屏：验证布局、状态解耦与槽位可替换性。
 *
 * 皮肤为**单张**纹理（meta 里的 `selector_trigger.sprite`），交互状态不切换素材，
 * 差异由染色与 outline 描边表达。
 *
 * 验证点：
 * 1. 开合 —— 点击切换，图标在 Down / Up 之间切换；
 * 2. 状态解耦 —— 同样的组件，`enabled` / `expanded` 全由外部驱动；
 * 3. 槽位替换 —— content 换成自定义内容、expandIcon 换成自定义图标；
 * 4. 配色 —— 传参覆盖皮肤色板与内容色。
 */
@Composable
fun SelectorTriggerTestContent() {
    var expandedA by remember { mutableStateOf(false) }
    var expandedB by remember { mutableStateOf(false) }
    var expandedC by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.width(320.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("1. 默认（点击开合）")
        SelectorTrigger(
            content = { Text(if (expandedA) "已展开" else "未展开") },
            expanded = expandedA,
            onClick = { expandedA = !expandedA },
        )

        Text("2. 默认值覆盖：自定义内容 + 自定义图标")
        SelectorTrigger(
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Filter)
                    Text("筛选条件")
                }
            },
            expandIcon = { _, _ -> Icon(Icons.Expand) },
            expanded = expandedB,
            onClick = { expandedB = !expandedB },
        )

        Text("3. 初始展开（图标应为 Up）")
        SelectorTrigger(
            content = { Text("默认展开") },
            expanded = expandedC,
            onClick = { expandedC = !expandedC },
        )

        Text("4. 禁用态")
        SelectorTrigger(
            content = { Text("不可用") },
            enabled = false,
            onClick = {},
        )

        Text("5. 禁用 + 自定义禁用配色")
        SelectorTrigger(
            content = { Text("禁用配色") },
            enabled = false,
            disableColors = SelectorTriggerDefaults.disableColors(contentColor = Color(0xFFFF5555)),
            onClick = {},
        )

        Text("6. 传参覆盖配色")
        SelectorTrigger(
            content = { Text("主色皮肤") },
            colors = SelectorTriggerDefaults.colors(
                color = Color(0xFF2E4A7D),
                contentColor = Color(0xFFFFFFFF),
                selectedOutlineColor = Color(0xFFFFD54F),
            ),
            onClick = {},
        )

        Text("7. 无图标槽位")
        SelectorTrigger(
            content = { Text("纯内容") },
            expandIcon = { _, _ -> },
            onClick = {},
        )
    }
}

fun SelectorTriggerTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                SelectorTriggerTestContent()
            }
        }
    }
}
