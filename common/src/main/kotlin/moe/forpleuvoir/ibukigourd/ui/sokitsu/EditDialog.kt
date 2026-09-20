package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import moe.forpleuvoir.ibukigourd.lang.IGLang

/**
 * 编辑对话框：以可编辑副本承载内容，确认时把副本交回调用方。
 *
 * 语义：
 * - 组合时把 [initial] 复制成 [SnapshotStateList] 副本交给 [content]，副本的增删改都不写回 [initial]；
 * - 确认按钮触发 [onConfirm]，参数是**当前副本**的内容快照 `List<E>`；
 *   返回 `true` 关闭对话框、`false` 保持打开（供提交前校验）；
 * - 取消 / 遮罩 / Esc 关闭时副本直接丢弃，调用方不需要回滚。
 *
 * 副本只在首次组合时创建（`remember` 无 key），[initial] 之后的变化不会写进已打开的对话框。
 *
 * 面板由 [FlexibleDialog] 提供（宽度由内容决定，正文区可放列表）。
 *
 * @param onDismissRequest 关闭请求
 * @param onConfirm 确认回调，返回 `true` 关闭对话框
 * @param modifier 应用到面板的 Modifier
 * @param initial 初始内容，复制成可编辑副本
 * @param title 标题，null 时不显示
 * @param confirmButton 确认按钮槽位，参数为"提交并关闭"的动作
 * @param dismissButton 取消按钮槽位，参数为关闭动作，null 时不显示
 * @param content 正文槽位，参数为可编辑副本
 */
@Composable
fun <E : Any> EditDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (List<E>) -> Boolean,
    modifier: Modifier = Modifier,
    initial: List<E> = emptyList(),
    title: (@Composable () -> Unit)? = null,
    minWidth: Dp = AlertDialogDefaults.minWidth,
    maxWidth: Dp = AlertDialogDefaults.maxWidth,
    confirmButton: @Composable (confirm: () -> Unit) -> Unit = { confirm ->
        FlatButton(onClick = confirm) { Text(IGLang.Misc.confirm) }
    },
    dismissButton: (@Composable (dismiss: () -> Unit) -> Unit)? = { dismiss ->
        FlatButton(onClick = dismiss) { Text(IGLang.Misc.cancel) }
    },
    content: @Composable (SnapshotStateList<E>) -> Unit,
) {
    val editing = remember { initial.toMutableStateList() }

    FlexibleDialog(
        onDismissRequest = onDismissRequest,
        onConfirmRequest = { onConfirm(editing.toList()) },
        modifier = modifier,
        title = title,
        minWidth = minWidth,
        maxWidth = maxWidth,
        confirmButton = {
            confirmButton {
                if (onConfirm(editing.toList())) onDismissRequest()
            }
        },
        dismissButton = dismissButton?.let { slot -> { slot(onDismissRequest) } },
        content = { content(editing) },
    )
}
