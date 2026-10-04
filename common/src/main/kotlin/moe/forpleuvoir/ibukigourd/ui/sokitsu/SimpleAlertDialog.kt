package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite

/**
 * 简易提示对话框：[AlertDialog] 的参数化包装，适合"一句提示 + 确认/取消"的常规场景。
 *
 * 与 [AlertDialog] 的差别：
 * - 三个内容槽位固定为"标题 / 正文"文本槽，按钮默认用 [FlatButton] + [IGLang] 的确认 / 取消文案，
 *   调用方只给内容即可开一个对话框；
 * - 确认回调返回 `Boolean`：返回 `true` 才关闭对话框，返回 `false` 保持打开 ——
 *   用于提交前校验（如输入不合法时不放行）；
 * - 配色、面板精灵、内边距与宽度上下限缺省取自 [AlertDialogDefaults]，需要时整体覆盖。
 *
 * 需要图标、自定义按钮外观或多段内容时直接用 [AlertDialog]。
 *
 * 显隐照 M3 的写法用 `if` 控制组合；出现 / 消失动画由平台 `Dialog` 承担（见 [AlertDialog]）。
 *
 * @param onDismissRequest 取消 / 关闭时回调
 * @param onConfirmRequest 确认按钮点击时回调，返回 `true` 关闭对话框、`false` 保持打开
 * @param modifier 应用到对话框的 Modifier
 * @param confirmButton 确认按钮内容，默认 [IGLang.Misc.confirm] 文案
 * @param dismissButton 取消按钮内容，默认 [IGLang.Misc.cancel] 文案，传 null 则不显示取消按钮
 * @param icon 图标，null 时不显示
 * @param title 标题，null 时不显示
 * @param content 正文，null 时不显示
 * @param colors 配色，缺省取 [AlertDialogDefaults.colors]
 * @param sprite 面板精灵，缺省取 [AlertDialogDefaults.sprite]
 * @param contentPadding 内容内边距，缺省取 [AlertDialogDefaults.contentPadding]
 * @param minWidth 面板宽度下限，缺省取 [AlertDialogDefaults.minWidth]
 * @param maxWidth 面板宽度上限，缺省取 [AlertDialogDefaults.maxWidth]
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
    colors: AlertDialogColors = AlertDialogDefaults.colors(),
    sprite: SokitsuSprite = AlertDialogDefaults.sprite,
    contentPadding: PaddingValues = AlertDialogDefaults.contentPadding,
    minWidth: Dp = AlertDialogDefaults.minWidth,
    maxWidth: Dp = AlertDialogDefaults.maxWidth,
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
        colors = colors,
        sprite = sprite,
        contentPadding = contentPadding,
        minWidth = minWidth,
        maxWidth = maxWidth,
        properties = properties,
    )
}
