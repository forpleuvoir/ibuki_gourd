package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlexibleDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TextField
import moe.forpleuvoir.nebula.config.Config

/**
 * 字符串：单行输入框 + 「多行编辑」按钮（打开 [FlexibleDialog]）。
 *
 * 与旧版的差别：旧版是在行内展开一个"随内容长高"的编辑器（`ExpandableStringContentEditor`），
 * 行高会跳变；本版把多行编辑放进浮层，行高恒定、也不挤占其它行。
 *
 * @param config 字符串配置项
 * @param modifier 作用于整行
 */
@Composable
fun StringConfigWrapper(config: Config<String>, modifier: Modifier = Modifier) {
    val value by config.asState()
    var editing by remember(config) { mutableStateOf(false) }

    ConfigRowWrapper(config, modifier) {
        ConfigControlBlock(
            action = {
                IconButton(
                    onClick = { editing = true },
                    contentPadding = ConfigControlDefaults.IconButtonPadding,
                ) {
                    Icon(Icons.Edit, scale = configIconScale())
                }
            },
        ) {
            StringValueField(
                value = value,
                onValueChange = { config.setValue(it) },
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (editing) {
        StringEditDialog(
            title = { ConfigDialogTitle(config) },
            initial = value,
            onDismiss = { editing = false },
            onConfirm = {
                config.setValue(it)
                editing = false
            },
        )
    }
}

/**
 * 受控单行输入框。
 *
 * 与 `NumberField` 相同的同步策略：外部值变化才回写文本（`lastSynced` 守卫，不打断正在进行的输入）；
 * 文本变化只回调外部、不回写。
 *
 * @param isError 错误态，描边改染错误色
 */
@Composable
internal fun StringValueField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val state = rememberTextFieldState(value)
    var lastSynced by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (value != lastSynced) {
            lastSynced = value
            state.setTextAndPlaceCursorAtEnd(value)
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }.collect { text ->
            if (text != lastSynced) {
                lastSynced = text
                onValueChange(text)
            }
        }
    }

    TextField(
        state = state,
        modifier = modifier,
        isError = isError,
        lineLimits = TextFieldLineLimits.SingleLine,
    )
}

/**
 * 多行字符串编辑弹窗：确认时才把整段文本交回（取消 / 遮罩关闭直接丢弃）。
 */
@Composable
internal fun StringEditDialog(
    title: @Composable () -> Unit,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val state = rememberTextFieldState(initial)

    FlexibleDialog(
        onDismissRequest = onDismiss,
        onConfirmRequest = {
            onConfirm(state.text.toString())
            true
        },
        title = title,
        content = {
            TextField(
                state = state,
                modifier = Modifier.width(840.dp).height(360.dp),
                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 6, maxHeightInLines = 12),
            )
        },
    )
}
