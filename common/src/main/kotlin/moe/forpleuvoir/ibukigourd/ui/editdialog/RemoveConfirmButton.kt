package moe.forpleuvoir.ibukigourd.ui.editdialog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButtonDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.SimpleAlertDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip.tooltip
import moe.forpleuvoir.ibukigourd.ui.util.isQuickAction

/**
 * 删除按钮：`delete` 图标 + [IGLang.Misc.remove] 气泡提示，点击直接执行 [onClick]，不做确认。
 *
 * @param onClick 点击回调
 * @param modifier 应用到按钮的 Modifier
 * @param enabled 是否可用
 */
@Composable
fun RemoveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconScale: Int = 2,
    contentPadding: PaddingValues = IconButtonDefaults.contentPadding,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.tooltip { Text(IGLang.Misc.remove) },
        enabled = enabled,
        contentPadding = contentPadding,
    ) {
        Icon(Icons.Delete, scale = iconScale)
    }
}

/**
 * 带二次确认的删除按钮：点击弹确认对话框，确认后才执行 [onConfirm]。
 *
 * [quickAction] 在**点击时**求值，返回 `true` 则跳过确认直接执行（默认读 [isQuickAction]，
 * 即按住 [moe.forpleuvoir.ibukigourd.mod.config.IGConfig.Gui.quickActionKeyCode] 时跳过）。
 *
 * 确认按钮用 [FlatButton] 的默认配色，标题为 [IGLang.Misc.removeConfirm]。
 *
 * @param message 确认标题里的目标描述
 * @param onConfirm 确认后的动作
 * @param modifier 应用到按钮的 Modifier
 * @param enabled 是否可用
 * @param quickAction 点击时求值的"跳过确认"判定，默认 [isQuickAction]
 * @param content 确认对话框的补充正文，null 时不显示
 */
@Composable
fun RemoveConfirmButton(
    message: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    quickAction: () -> Boolean = { isQuickAction },
    iconScale: Int = 2,
    contentPadding: PaddingValues = IconButtonDefaults.contentPadding,
    content: (@Composable () -> Unit)? = null,
) {
    var showDialog by remember { mutableStateOf(false) }

    RemoveButton(
        onClick = { if (quickAction()) onConfirm() else showDialog = true },
        modifier = modifier,
        enabled = enabled,
        iconScale = iconScale,
        contentPadding = contentPadding,
    )

    if (showDialog) {
        SimpleAlertDialog(
            onDismissRequest = { showDialog = false },
            onConfirmRequest = {
                onConfirm()
                true
            },
            title = { Text(IGLang.Misc.removeConfirm(message)) },
            content = content,
            confirmButton = {
                FlatButton(
                    onClick = {
                        onConfirm()
                        showDialog = false
                    },
                ) {
                    Text(IGLang.Misc.confirm)
                }
            },
        )
    }
}
