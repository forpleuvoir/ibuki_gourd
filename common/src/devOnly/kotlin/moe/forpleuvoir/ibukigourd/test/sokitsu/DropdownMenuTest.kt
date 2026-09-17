package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.HorizontalDivider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.menu.DropdownMenu
import moe.forpleuvoir.ibukigourd.ui.sokitsu.menu.DropdownMenuItem
import moe.forpleuvoir.ibukigourd.ui.sokitsu.menu.dropdownMenuAnchor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.menu.rememberDropdownMenuState

/**
 * 下接菜单测试屏：覆盖定位、对齐、滚动、翻转与条目状态。
 *
 * 验证点：
 * 1. 基础 —— 点击触发器弹出、点击条目后记录并自动关闭、点击外部关闭；
 * 2. 图标槽位 —— leading / trailing 两侧图标与文本的对齐；
 * 3. 禁用项 —— 不可点击、不响应悬停、内容色弱化；
 * 4. 长列表 —— 超出 `maxHeight` 时菜单内部滚动，面板自身不再变高；
 * 5. 靠右触发器 —— 面板与触发器中心对齐，靠右时由夹取保证不溢出窗口；
 * 6. 高菜单 —— 下方放不下时翻到触发器上方；
 * 7. 可滚动列表（场景 8）—— 打开某项菜单后滚动，菜单应跟随触发器移动（验证定位器随锚点刷新）。
 *
 * 菜单面板复用气泡体素材，配色与 tooltip 同族；触发器与菜单是分开的两个元素，
 * 不要求两者尺寸对齐。
 */
@Composable
fun DropdownMenuTestContent() {
    var lastAction by remember { mutableStateOf("（还没操作）") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("最近操作：$lastAction")

        // 1. 基础
        val basic = rememberDropdownMenuState()
        Button(
            onClick = basic::toggle,
            modifier = Modifier.dropdownMenuAnchor(basic),
        ) { Text("1. 基础菜单") }
        DropdownMenu(basic) {
            repeat(3) { index ->
                DropdownMenuItem(onClick = { lastAction = "基础 · 选项 ${index + 1}" }) {
                    Text("选项 ${index + 1}")
                }
            }
        }

        // 2. 图标槽位
        val withIcon = rememberDropdownMenuState()
        Button(
            onClick = withIcon::toggle,
            modifier = Modifier.dropdownMenuAnchor(withIcon),
        ) { Text("2. 带图标") }
        DropdownMenu(withIcon) {
            DropdownMenuItem(
                onClick = { lastAction = "复制" },
                leadingIcon = { Icon(Icons.Copy) },
            ) { Text("复制") }
            DropdownMenuItem(
                onClick = { lastAction = "删除" },
                leadingIcon = { Icon(Icons.Delete) },
                trailingIcon = { Icon(Icons.Close) },
            ) { Text("删除（两侧图标）") }
        }

        // 3. 禁用项
        val disabled = rememberDropdownMenuState()
        Button(
            onClick = disabled::toggle,
            modifier = Modifier.dropdownMenuAnchor(disabled),
        ) { Text("3. 含禁用项") }
        DropdownMenu(disabled) {
            DropdownMenuItem(onClick = { lastAction = "可用项" }) { Text("可用项") }
            DropdownMenuItem(onClick = { lastAction = "这一项不该被点到" }, enabled = false) {
                Text("禁用项")
            }
        }

        // 4. 长列表：验证内部滚动
        val long = rememberDropdownMenuState()
        Button(
            onClick = long::toggle,
            modifier = Modifier.dropdownMenuAnchor(long),
        ) { Text("4. 长列表（内部滚动）") }
        DropdownMenu(long) {
            repeat(12) { index ->
                DropdownMenuItem(onClick = { lastAction = "长列表 · 第 ${index + 1} 项" }) {
                    Text("第 ${index + 1} 项")
                }
            }
            HorizontalDivider()
            DropdownMenuItem(onClick = { lastAction = "长列表 · 末项" }) { Text("末项") }
        }

        // 5. 靠右触发器：菜单会右溢出 → 应改为右对齐
        Row(
            modifier = Modifier.width(300.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            val rightAligned = rememberDropdownMenuState()
            Button(
                onClick = rightAligned::toggle,
                modifier = Modifier.dropdownMenuAnchor(rightAligned),
            ) { Text("5. 靠右（中心对齐）") }
            DropdownMenu(rightAligned) {
                DropdownMenuItem(onClick = { lastAction = "右对齐 · 一项很长的菜单项" }) {
                    Text("一项很长的菜单项")
                }
            }
        }

        // 6. 高菜单：下方放不下 → 应翻到上方
        val tall = rememberDropdownMenuState()
        Button(
            onClick = tall::toggle,
            modifier = Modifier.dropdownMenuAnchor(tall),
        ) { Text("6. 高菜单（应向上翻）") }
        DropdownMenu(tall, maxHeight = 900.dp) {
            repeat(24) { index ->
                DropdownMenuItem(onClick = { lastAction = "高菜单 · 第 ${index + 1} 项" }) {
                    Text("第 ${index + 1} 项")
                }
            }
        }

        // 7. 完全自定义内容：不使用 DropdownMenuItem
        val custom = rememberDropdownMenuState()
        Button(
            onClick = custom::toggle,
            modifier = Modifier.dropdownMenuAnchor(custom),
        ) { Text("7. 自定义内容") }
        DropdownMenu(custom) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Edit, scale = 2)
                Text("自己拼的一行")
            }
            HorizontalDivider()
            Text("直接放文本也可以", modifier = Modifier.padding(8.dp))
        }

        // 8. 可滚动列表：打开某项菜单后滚动，菜单应跟随触发器移动（验证定位器跟随锚点）
        Text("8. 可滚动列表（打开菜单后滚动验证跟随）")
        Box(
            Modifier
                .height(200.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(20) { index ->
                    val item = rememberDropdownMenuState()
                    Button(
                        onClick = item::toggle,
                        modifier = Modifier.dropdownMenuAnchor(item).fillMaxWidth(),
                    ) { Text("第 ${index + 1} 项触发器") }
                    DropdownMenu(item) {
                        DropdownMenuItem(onClick = { lastAction = "滚动列表 · 第 ${index + 1} 项" }) {
                            Text("操作 ${index + 1}")
                        }
                    }
                }
            }
        }
    }
}

fun DropdownMenuTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            DropdownMenuTestContent()
        }
    }
}
