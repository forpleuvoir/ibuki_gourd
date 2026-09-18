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
import moe.forpleuvoir.ibukigourd.ui.selector.Selector
import moe.forpleuvoir.ibukigourd.ui.selector.SelectorCheckbox
import moe.forpleuvoir.ibukigourd.ui.selector.SelectorExpandStyle
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text

/**
 * 多选选择器测试屏：验证集合语义、切换行为与两种载体的收场方式。
 *
 * 验证点：
 * 1. Auto 少选项 —— 数量 ≤ 10 → 下拉菜单，点某项切换选中态、不收起，点外部才收起；
 * 2. Auto 多选项 —— 数量 > 10 → 弹窗，底部出现取消 / 确定；
 * 3. 搜索过滤器 —— 强制弹窗 + 搜索栏，勾选后确定生效；
 * 4. 取消回滚 —— 弹窗里改若干项后点取消，外部状态应回到展开前的样子（由 onCancel 承担）；
 * 5. 触发器回显 —— 选中集合变化后触发器文案即时反映；
 * 6. 前置复选框 —— 用 [SelectorCheckbox] 表达选中态（素材由调用方提供）。
 */
@Composable
fun MultiSelectorTestContent() {
    var few by remember { mutableStateOf(setOf("选项 1")) }
    var many by remember { mutableStateOf(emptySet<String>()) }
    var searched by remember { mutableStateOf(emptySet<String>()) }
    var cancelled by remember { mutableStateOf(setOf("A")) }

    val fewItems: List<String> = List(5) { "选项 ${it + 1}" }
    val manyItems: List<String> = List(30) { "选项 ${it + 1}" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("1. Auto（5 项 ≤ 10）→ 下拉菜单，点外部收起")
        Selector(
            selected = few,
            onSelectionChange = { before, item ->
                few = if (item in before) before - item else before + item
            },
            items = fewItems,
            content = { Text(if (it.isEmpty()) "未选择" else it.joinToString()) },
            modifier = Modifier.width(220.dp),
            itemContent = { item, _ -> Text(item) },
        )

        Text("2. Auto（30 项 > 10）→ 弹窗 + 取消 / 确定")
        Selector(
            selected = many,
            onSelectionChange = { before, item ->
                many = if (item in before) before - item else before + item
            },
            items = manyItems,
            content = { Text(if (it.isEmpty()) "未选择" else "已选 ${it.size} 项") },
            modifier = Modifier.width(220.dp),
            itemContent = { item, _ -> Text(item) },
        )

        Text("3. 搜索过滤器 → 强制弹窗 + 搜索栏")
        Selector(
            selected = searched,
            onSelectionChange = { before, item ->
                searched = if (item in before) before - item else before + item
            },
            items = manyItems,
            content = { Text(if (it.isEmpty()) "未选择" else "已选 ${it.size} 项") },
            modifier = Modifier.width(220.dp),
            searchFilter = { item, query -> item.contains(query, ignoreCase = true) },
            itemContent = { item, _ -> Text(item) },
        )

        Text("4. 取消回滚（当前：${cancelled.joinToString()}）")
        Selector(
            selected = cancelled,
            onSelectionChange = { before, item ->
                cancelled = if (item in before) before - item else before + item
            },
            items = fewItems,
            content = { Text(if (it.isEmpty()) "未选择" else it.joinToString()) },
            modifier = Modifier.width(220.dp),
            expandStyle = SelectorExpandStyle.Dialog,
            onCancel = { cancelled = setOf("A") },
            itemContent = { item, _ -> Text(item) },
        )

        Text("5. 前置复选框 + 前置图标槽位")
        Selector(
            selected = few,
            onSelectionChange = { before, item ->
                few = if (item in before) before - item else before + item
            },
            items = fewItems,
            content = { Text(if (it.isEmpty()) "未选择" else it.joinToString()) },
            modifier = Modifier.width(220.dp),
            itemContent = { item, _ -> Text(item) },
            itemLeadingIcon = { selected -> { SelectorCheckbox(selected) } },
        )
    }
}

fun MultiSelectorTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.width(320.dp)) {
                MultiSelectorTestContent()
            }
        }
    }
}
