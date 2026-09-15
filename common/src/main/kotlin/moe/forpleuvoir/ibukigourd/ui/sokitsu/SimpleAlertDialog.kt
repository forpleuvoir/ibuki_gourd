package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
import moe.forpleuvoir.ibukigourd.lang.IGLang

/**
 * 简易提示对话框：[AlertDialog] 的参数化包装，适合"一句提示 + 确认/取消"的常规场景。
 *
 * 与 [AlertDialog] 的差别：
 * - 三个内容槽位固定为"标题 / 正文"文本槽，按钮默认用 [FlatButton] + [IGLang] 的确认 / 取消文案，
 *   调用方只给内容即可开一个对话框；
 * - 确认回调返回 `Boolean`：返回 `true` 才关闭对话框，返回 `false` 保持打开 ——
 *   用于提交前校验（如输入不合法时不放行）；
 * - 配色按单个槽位传参（[containerColor] 等），内部再组装成 [AlertDialogColors]。
 *
 * 需要图标、自定义按钮外观或多段内容时直接用 [AlertDialog]。
 *
 * 显隐照 M3 的写法用 `if` 控制组合；出现 / 消失动画由平台 `Dialog` 承担（见 [AlertDialog]）。
 *
 * @param onDismissRequest 取消 / 关闭时回调
 * @param onConfirmRequest 确认按钮点击时回调，返回 `true` 关闭对话框、`false` 保持打开
 * @param confirmButton 确认按钮内容，默认 [IGLang.Misc.confirm] 文案
 * @param dismissButton 取消按钮内容，默认 [IGLang.Misc.cancel] 文案，传 null 则不显示取消按钮
 * @param title 标题，null 时不显示
 * @param content 正文，null 时不显示
 * @param containerColor 面板容器色板覆盖，未指定按 [AlertDialogTokens.Container] 解析
 * @param iconContentColor 图标着色覆盖，未指定按 [AlertDialogTokens.IconContent] 解析
 * @param titleContentColor 标题文本色覆盖，未指定按 [AlertDialogTokens.TitleContent] 解析
 * @param textContentColor 正文文本色覆盖，未指定按 [AlertDialogTokens.TextContent] 解析
 * @param properties 平台对话框属性
 */
@Composable
fun SimpleAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Boolean,
    modifier: Modifier = Modifier,
    confirmButton: @Composable () -> Unit = {
        FlatButton(onClick = { if (onConfirmRequest()) onDismissRequest() }) {
            Text(IGLang.Misc.confirm)
        }
    },
    dismissButton: (@Composable () -> Unit)? = {
        FlatButton(onClick = onDismissRequest) {
            Text(IGLang.Misc.cancel)
        }
    },
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
    containerColor: Color = Color.Unspecified,
    iconContentColor: Color = Color.Unspecified,
    titleContentColor: Color = Color.Unspecified,
    textContentColor: Color = Color.Unspecified,
    properties: DialogProperties = DialogProperties(),
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = content,
        colors = AlertDialogDefaults.colors(
            containerColor = containerColor,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
        ),
        properties = properties,
    )
}
