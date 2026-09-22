package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ProvideContentColorTextStyle
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme

/**
 * 可伸缩对话框：面板尺寸由内容决定，不受平台默认限宽约束。
 *
 * 与 [AlertDialog] 的差别：
 * - 平台默认限宽关闭（`DialogProperties.usePlatformDefaultWidth = false`），面板宽度只受
 *   [minWidth] / [maxWidth] 约束；两者未指定（[Dp.Unspecified]）时不加宽度修饰；
 * - 正文槽位包在 `weight(1f, fill = false)` 的列中：有富余空间时按需取用，内容更高时受
 *   弹窗可用高度约束，便于内部放可滚动列表；
 * - 内边距拆成面板四边的 [contentPadding] 与按钮行的 [buttonsTopPadding] / [buttonSpacing]。
 *
 * 面板精灵、配色与进出场动画沿用 [AlertDialog] 的主题槽位（[AlertDialogDefaults]），
 * 因此两者共享 `alert_dialog` 段的 meta。显隐同 [AlertDialog]，用 `if` 控制组合。
 *
 * @param onDismissRequest 关闭请求（遮罩 / Esc / 按钮自行调用）
 * @param onConfirmRequest 确认回调，返回 `true` 关闭对话框
 * @param modifier 应用到面板 [Surface] 的 Modifier
 * @param title 标题，null 时不显示
 * @param content 正文，null 时不显示
 * @param confirmButton 确认按钮
 * @param dismissButton 取消按钮，null 时不显示
 * @param contentPadding 面板内容内边距
 * @param buttonsTopPadding 按钮行与上方内容的间距
 * @param buttonSpacing 按钮之间的水平间距
 * @param titleBottomPadding 标题与正文的间距，默认取 `alert_dialog` meta
 * @param minWidth 面板最小宽度，未指定时不约束
 * @param maxWidth 面板最大宽度，未指定时不约束
 * @param maxHeight 面板最大高度（含标题与按钮行），未指定时不约束
 * @param screenPadding 面板与窗口四边的间距：面板的可用尺寸按它收窄，因此内容再高也不会顶到窗口边缘
 * @param colors 面板配色，默认 [AlertDialogDefaults.colors]
 * @param sprite 面板精灵，默认 [AlertDialogDefaults.sprite]
 * @param enterAnimation 入场过渡，默认取 meta
 * @param exitAnimation 退场过渡，默认取 meta
 * @param properties 平台对话框属性
 */
@Composable
fun FlexibleDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Boolean,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
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
    contentPadding: PaddingValues = FlexibleDialogDefaults.contentPadding,
    buttonsTopPadding: Dp = FlexibleDialogDefaults.buttonsTopPadding,
    buttonSpacing: Dp = FlexibleDialogDefaults.buttonSpacing,
    titleBottomPadding: Dp = AlertDialogDefaults.meta.titleBottomPadding,
    minWidth: Dp = Dp.Unspecified,
    maxWidth: Dp = Dp.Unspecified,
    maxHeight: Dp = Dp.Unspecified,
    screenPadding: PaddingValues = FlexibleDialogDefaults.screenPadding,
    colors: AlertDialogColors = AlertDialogDefaults.colors(),
    sprite: SokitsuSprite = AlertDialogDefaults.sprite,
    enterAnimation: AlertDialogAnimationMeta = AlertDialogDefaults.enterAnimation,
    exitAnimation: AlertDialogAnimationMeta = AlertDialogDefaults.exitAnimation,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties.withTransitions(enterAnimation, exitAnimation),
    ) {
        // 外层盒子只负责让出 screenPadding：面板的可用尺寸按它收窄，内容再高也只能用剩下的高度
        Box(
            modifier = Modifier.padding(screenPadding),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = modifier
                    .then(if (minWidth != Dp.Unspecified) Modifier.widthIn(min = minWidth) else Modifier)
                    .then(if (maxWidth != Dp.Unspecified) Modifier.widthIn(max = maxWidth) else Modifier)
                    .then(if (maxHeight != Dp.Unspecified) Modifier.heightIn(max = maxHeight) else Modifier),
                color = colors.containerColor,
                contentColor = colors.textContentColor,
                sprite = sprite,
            ) {
                Column(modifier = Modifier.padding(contentPadding)) {
                    title?.let { titleContent ->
                        Box(Modifier.padding(bottom = titleBottomPadding)) {
                            ProvideContentColorTextStyle(colors.titleContentColor, SokitsuTheme.typography.subtitle) {
                                titleContent()
                            }
                        }
                    }

                    content?.let { contentContent ->
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            ProvideContentColorTextStyle(colors.textContentColor, SokitsuTheme.typography.body) {
                                contentContent()
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = buttonsTopPadding),
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        dismissButton?.invoke()
                        confirmButton()
                    }
                }
            }
        }
    }
}

/** [FlexibleDialog] 的默认参数：面板内边距与按钮行间距。 */
object FlexibleDialogDefaults {

    /** 面板内容内边距（四边统一）。 */
    val contentPadding: PaddingValues = PaddingValues(24.dp)

    /** 按钮行与上方内容的间距。 */
    val buttonsTopPadding: Dp = 16.dp

    /** 按钮之间的水平间距。 */
    val buttonSpacing: Dp = 8.dp

    /** 面板与窗口四边的间距。 */
    val screenPadding: PaddingValues = PaddingValues(24.dp)
}
