package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.CenterBox
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.editdialog.EditDialog
import moe.forpleuvoir.ibukigourd.ui.editdialog.EditDialogContent
import moe.forpleuvoir.ibukigourd.ui.editdialog.RemoveConfirmButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlexibleDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.util.FabScrollVisibility
import moe.forpleuvoir.ibukigourd.ui.util.isHideAction
import moe.forpleuvoir.ibukigourd.ui.util.rememberFabScrollVisibility

/** 行内操作形态：无按钮 / 只有图标（无 tooltip）/ 带 tooltip 与二次确认。 */
private const val REMOVE_NONE = 0
private const val REMOVE_ICON = 1
private const val REMOVE_CONFIRM = 2

/**
 * 可伸缩 / 编辑对话框测试屏（按「滚动卡死」二分设计）。
 *
 * 逐个点开、各自连续滚轮到底，报出哪个仍会卡死：
 * 1a 完整（浮动按钮 + 确认删除）/ 1b 无浮动按钮 / 1c 删除按钮无 tooltip / 1d 纯文本行 /
 * 2a 同样内容的**内联列表**（不在弹窗里，带读数）/ 2b 内联列表但**不读滚动状态** /
 * 3a、3b FlexibleDialog 宽度行为。
 *
 * 另外验证：确认提交或取消丢弃副本、列表区上限内滚动、浮动按钮随滚动与隐藏动作键收起。
 */
@Composable
fun EditDialogTestContent() {
    var lastAction by remember { mutableStateOf("（还没操作）") }
    var items by remember { mutableStateOf(List(24) { "条目 ${it + 1}" }) }
    var seed by remember { mutableStateOf(0) }
    var case by remember { mutableStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("最近操作：$lastAction")
        Text("卡死二分：逐个滚轮到底，看哪个还卡")
        Button({ case = 1 }) { Text("1a. 弹窗 完整（浮动按钮 + 确认删除）") }
        Button({ case = 2 }) { Text("1b. 弹窗 无浮动按钮") }
        Button({ case = 3 }) { Text("1c. 弹窗 删除按钮无 tooltip") }
        Button({ case = 4 }) { Text("1d. 弹窗 纯文本行") }
        Button({ case = 5 }) { Text("2a. 内联列表（无弹窗，完整行 + 读数）") }
        Button({ case = 6 }) { Text("2b. 内联列表（不读滚动状态）") }
        Button({ case = 7 }) { Text("3a. FlexibleDialog 不限宽") }
        Button({ case = 8 }) { Text("3b. FlexibleDialog maxWidth = 720dp") }
    }

    when (case) {
        1, 2, 3, 4 -> {
            val listState = rememberLazyListState()
            val fabState = rememberFabScrollVisibility(listState)
            val removeMode = when (case) {
                4 -> REMOVE_NONE
                3 -> REMOVE_ICON
                else -> REMOVE_CONFIRM
            }
            EditDialog(
                onDismissRequest = { case = 0 },
                onConfirm = { result ->
                    items = result
                    lastAction = "弹窗：提交 ${result.size} 项"
                    true
                },
                initial = items,
                title = { Text("编辑列表") },
                maxWidth = 800.dp,
            ) { draft ->
                EditDialogContent(
                    modifier = Modifier.fillMaxWidth(),
                    lazyListState = listState,
                    addButton = if (case == 1) {
                        { Button({ seed++; draft.add("新增 $seed") }) { Icon(Icons.Add) } }
                    } else null,
                ) { state ->
                    EditorRows(draft, removeMode, state)
                    Text("共 ${draft.size} 项")
                    ScrollReadout(state, fabState)
                }
            }
        }

        5 -> {
            val values = remember { List(24) { "条目 ${it + 1}" }.toMutableStateList() }
            val listState = rememberLazyListState()
            val fabState = rememberFabScrollVisibility(listState)
            Surface(Modifier.width(600.dp).height(400.dp)) {
                Column(Modifier.padding(8.dp)) {
                    EditorRows(values, REMOVE_CONFIRM, listState)
                    ScrollReadout(listState, fabState)
                }
            }
        }

        6 -> {
            val values = remember { List(24) { "条目 ${it + 1}" }.toMutableStateList() }
            Surface(Modifier.width(600.dp).height(400.dp)) {
                Column(Modifier.padding(8.dp)) {
                    EditorRows(values, REMOVE_CONFIRM, rememberLazyListState())
                }
            }
        }

        7, 8 -> FlexibleDialog(
            onDismissRequest = { case = 0; lastAction = "FlexibleDialog：已关闭" },
            onConfirmRequest = { lastAction = "FlexibleDialog：确认"; true },
            title = { Text(if (case == 7) "宽度由内容决定" else "maxWidth = 720dp") },
            maxWidth = if (case == 8) 720.dp else Dp.Unspecified,
            content = {
                Text(
                    "这段正文刻意写得很长：不限宽时面板可用宽度即窗口宽度，" +
                            "给了 maxWidth 则在此宽度内换行，两个按钮的差别就是这一条。"
                )
            },
        )
    }
}

/** 列表行：文本 + 可选的删除操作，形态由 [removeMode] 决定。 */
@Composable
private fun EditorRows(
    values: SnapshotStateList<String>,
    removeMode: Int,
    listState: LazyListState,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        itemsIndexed(values, key = { index, _ -> index }) { index, value ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(value, modifier = Modifier.weight(1f))
                when (removeMode) {
                    REMOVE_CONFIRM -> RemoveConfirmButton(value, { values.removeAt(index) })
                    REMOVE_ICON -> IconButton({ values.removeAt(index) }) { Icon(Icons.Delete) }
                }
            }
        }
    }
}

/** 滚动读数：单独成一个可以独立重组的小组件，避免滚动时把弹窗内容整块重组。 */
@Composable
private fun ScrollReadout(state: LazyListState, visibility: FabScrollVisibility) {
    Text("index=${state.firstVisibleItemIndex} offset=${state.firstVisibleItemScrollOffset} fab=$visibility hideKey=${isHideAction}")
}

fun EditDialogTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        CenterBox {
            EditDialogTestContent()
        }
    }
}
