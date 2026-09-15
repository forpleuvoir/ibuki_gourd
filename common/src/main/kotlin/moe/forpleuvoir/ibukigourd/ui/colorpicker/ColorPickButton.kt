package moe.forpleuvoir.ibukigourd.ui.colorpicker

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.sokitsu.AlertDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.ColorButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text

/**
 * 取色按钮：[ColorButton] 作按钮本体（底色就是当前颜色），点击弹出 [ColorPicker] 编辑。
 *
 * 交互与上一版 `ColorSettingButton` 一致：弹窗内编辑的是**副本**，改动只提交在"确认"——
 * 取消 / 关闭弹窗都丢弃，所以在弹窗里怎么拖都不会影响外部的值；确认时才回调 [onValueChange]。
 *
 * 按钮内容默认显示色值文本（`#RRGGBB`；带透明度时 `#AARRGGBB`），可用 [content] 覆盖
 * （例如 `{ Icon(Icons.Palette); Text(...) }`）。
 *
 * 弹窗宽度按 [ColorPickerDefaults.DialogWidth] 放宽——取色器内容宽 664dp，
 * 超出 `alert_dialog` 默认的 `max_width`（560dp）。
 *
 * @param color 当前颜色（同时是弹窗打开时的初始值）
 * @param onValueChange 确认后回调
 * @param enabled 按钮是否可用
 * @param title 弹窗标题，null 时不显示
 * @param content 按钮内容，默认色值文本
 */
@Composable
fun ColorPickButton(
    color: Color,
    onValueChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    title: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit = { Text(color.toHexString()) },
) {
    var showDialog by remember { mutableStateOf(false) }

    ColorButton(
        onClick = { showDialog = true },
        color = color,
        modifier = modifier,
        enabled = enabled,
        content = content,
    )

    if (showDialog) {
        // 编辑副本：弹窗内的改动不回写外部，确认时才提交
        var editingColor by remember { mutableStateOf(color) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                FlatButton(onClick = {
                    onValueChange(editingColor)
                    showDialog = false
                }) {
                    Text(IGLang.Misc.confirm)
                }
            },
            dismissButton = {
                FlatButton(onClick = { showDialog = false }) {
                    Text(IGLang.Misc.cancel)
                }
            },
            title = title,
            text = {
                ColorPicker(
                    color = editingColor,
                    onValueChange = { editingColor = it },
                )
            },
            minWidth = ColorPickerDefaults.DialogWidth,
            maxWidth = ColorPickerDefaults.DialogWidth,
        )
    }
}
