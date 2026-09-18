package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.selector.Selector
import moe.forpleuvoir.ibukigourd.ui.selector.SelectorExpandStyle

/**
 * 单选选择器测试屏：验证三种载体样式、搜索栏、空选与选中高亮。
 *
 * 验证点：
 * 1. Auto 少选项 —— 数量 ≤ 10 → 下拉菜单；
 * 2. Auto 多选项 —— 数量 > 10 → 弹窗；
 * 3. 强制下拉菜单 —— `Dropdown` 恒定走菜单，无视数量；
 * 4. 强制弹窗 —— `Dialog` 恒定走弹窗；
 * 5. 搜索过滤器 —— 非 null 时强制弹窗，顶部出现搜索栏；
 * 6. 可空选项 —— 列表内含 `null` 项，选中它即空选，触发器显示占位文案；
 * 7. 选中高亮 —— 选中项以辅色染 `focused` 底，与悬停项区分。
 */
@Composable
fun SelectorTestContent() {
    var single by remember { mutableStateOf<String?>("选项 2") }
    var many by remember { mutableStateOf<String?>(null) }
    var forced by remember { mutableStateOf<String?>("A") }
    var searched by remember { mutableStateOf<String?>(null) }
    var nullable by remember { mutableStateOf<String?>("选项 1") }

    val fewItems: List<String?> = List(5) { "选项 ${it + 1}" }
    val manyItems: List<String?> = List(30) { "选项 ${it + 1}" }
    // 首项为 null：选中它即"空选"，用来验证可空选的表现
    val nullableItems: List<String?> = listOf(null, "选项 1", "选项 2", "选项 3")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("1. Auto（5 项 ≤ 10）→ 下拉菜单")
        Selector(
            selected = single,
            onSelect = { single = it },
            items = fewItems,
            content = { Text(it ?: "请选择") },
            modifier = Modifier.width(200.dp),
            itemContent = { item, _ -> Text(item ?: "") },
        )

        Text("2. Auto（30 项 > 10）→ 弹窗")
        Selector(
            selected = many,
            onSelect = { many = it },
            items = manyItems,
            content = { Text(it ?: "请选择") },
            modifier = Modifier.width(200.dp),
            itemContent = { item, _ -> Text(item ?: "") },
        )

        Text("3. 强制下拉菜单（30 项）")
        Selector(
            selected = forced,
            onSelect = { forced = it },
            items = manyItems,
            content = { Text(it ?: "请选择") },
            modifier = Modifier.width(200.dp),
            expandStyle = SelectorExpandStyle.Dropdown,
            itemContent = { item, _ -> Text(item ?: "") },
        )

        Text("4. 强制弹窗（5 项）")
        Selector(
            selected = forced,
            onSelect = { forced = it },
            items = fewItems,
            content = { Text(it ?: "请选择") },
            modifier = Modifier.width(200.dp),
            expandStyle = SelectorExpandStyle.Dialog,
            itemContent = { item, _ -> Text(item ?: "") },
        )

        Text("5. 搜索过滤器 → 强制弹窗 + 搜索栏")
        Selector(
            selected = searched,
            onSelect = { searched = it },
            items = manyItems,
            content = { Text(it ?: "请选择") },
            modifier = Modifier.width(200.dp),
            searchFilter = { item, query -> item?.contains(query, ignoreCase = true) == true },
            itemContent = { item, _ -> Text(item ?: "") },
        )

        Text("6. 可空选项（首项为 null → 选中即空选）")
        Selector(
            selected = nullable,
            onSelect = { nullable = it },
            items = nullableItems,
            content = { Text(it ?: "请选择") },
            modifier = Modifier.width(200.dp),
            itemContent = { item, _ -> Text(item ?: "（空）") },
        )

        Text("7. 前后置图标槽位")
        Selector(
            selected = forced,
            onSelect = { forced = it },
            items = fewItems,
            content = { Text(it ?: "请选择") },
            modifier = Modifier.width(200.dp),
            itemContent = { item, _ -> Text(item ?: "") },
            itemLeadingIcon = { isSelected -> if (isSelected) { { Icon(Icons.Filter) } } else null },
            itemTrailingIcon = { _ -> { Icon(Icons.ArrowRight) } },
        )
    }
}

fun SelectorTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.width(320.dp)) {
                SelectorTestContent()
            }
        }
    }
}
