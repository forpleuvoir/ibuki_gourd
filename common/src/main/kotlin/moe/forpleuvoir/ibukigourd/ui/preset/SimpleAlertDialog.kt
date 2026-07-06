package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IGCompositionLocalProvider
import moe.forpleuvoir.ibukigourd.ui.util.ProvideContentColorTextStyle

/**
 * 通用提示对话框，扩展自 Material3 [AlertDialog]
 *
 * 确认按钮回调 [onConfirmRequest] 返回 `Boolean`，若返回 `false` 则不关闭对话框，
 * 可用于前端校验场景（如输入不合法时阻止关闭）。
 *
 * @param onDismissRequest 取消/关闭对话框时回调
 * @param onConfirmRequest 确认按钮点击时回调，返回 `true` 关闭对话框，`false` 保持打开
 * @param modifier 应用到对话框的 [Modifier]
 * @param confirmButton 确认按钮内容，默认使用 [IGLang.Misc.confirm] 文案
 * @param dismissButton 取消按钮内容，默认使用 [IGLang.Misc.cancel] 文案，`null` 时不显示
 * @param icon 对话框图标，`null` 时不显示
 * @param title 对话框标题，`null` 时不显示
 * @param content 对话框正文内容，`null` 时不显示
 * @param shape 对话框形状
 * @param containerColor 容器背景色
 * @param iconContentColor 图标着色
 * @param titleContentColor 标题文本色
 * @param textContentColor 正文文本色
 * @param tonalElevation 色调高度
 * @param properties 对话框窗口属性
 */
@Composable
fun SimpleAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Boolean,
    modifier: Modifier = Modifier,
    confirmButton: @Composable () -> Unit = {
        TextButton(onClick = {
            if (onConfirmRequest()) {
                onDismissRequest()
            }
        }) {
            Text(IGLang.Misc.confirm)
        }
    },
    dismissButton: @Composable (() -> Unit)? = {
        TextButton(onClick = onDismissRequest) {
            Text(IGLang.Misc.cancel)
        }
    },
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
) = AlertDialog(
    onDismissRequest,
    confirmButton,
    modifier,
    dismissButton,
    icon,
    title,
    text = content?.let { c -> { IGCompositionLocalProvider(content = c) } },
    shape,
    containerColor,
    iconContentColor,
    titleContentColor,
    textContentColor,
    tonalElevation,
    properties
)


@Composable
fun FlexibleDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Boolean,
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit = {
        TextButton(onClick = {
            if (onConfirmRequest()) {
                onDismissRequest()
            }
        }) {
            Text(IGLang.Misc.confirm)
        }
    },
    dismissButton: @Composable (() -> Unit)? = {
        TextButton(onClick = onDismissRequest) {
            Text(IGLang.Misc.cancel)
        }
    },
    contentPadding: PaddingValues = PaddingValues(24.dp),
    buttonsPadding: PaddingValues = PaddingValues(top = 16.dp),
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    contentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            usePlatformInsets = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onDismissRequest()
                },
            contentAlignment = Alignment.Center
        ) {
            IGCompositionLocalProvider {
                Surface(
                    modifier = modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { },
                    shape = shape,
                    color = containerColor,
                    tonalElevation = tonalElevation,
                ) {
                    Column(
                        modifier = Modifier.padding(contentPadding),
                    ) {
                        title?.let { title ->
                            Box(Modifier.padding(bottom = 12.dp)) {
                                ProvideContentColorTextStyle(
                                    contentColor = titleContentColor,
                                    textStyle = MaterialTheme.typography.headlineSmall
                                ) {
                                    title()
                                }
                            }

                        }

                        content?.let { content ->
                            CompositionLocalProvider(
                                LocalContentColor provides contentColor,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f, false)
                                ) {
                                    content()
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(buttonsPadding),
                            horizontalArrangement = Arrangement.End
                        ) {
                            dismissButton?.let {
                                Box(Modifier.padding(end = 8.dp)) {
                                    it()
                                }
                            }
                            confirmButton()
                        }
                    }
                }
            }
        }
    }
}