package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ProvideContentColorTextStyle
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.uiSprite

/**
 * 提示对话框：模态浮层 + 面板容器 + 标题 / 正文 / 按钮行的槽位式排版，语义对齐 Material3 的 `AlertDialog`。
 *
 * 分工：
 * - **模态与遮罩**由平台 [Dialog] 提供（场景内图层，非 OS 窗口）：居中、平台默认限宽、
 *   scrim 遮罩、Esc 与点击遮罩关闭，全部来自 [properties]；
 * - **面板**由 [Surface] 提供（容器渲染整块委托，与 [Button] 委托 Surface 的分工一致），
 *   精灵取 [AlertDialogDefaults.sprite]，缺省即浮动面板素材（凸起带阴影）；
 * - 本组件只负责把 [icon] / [title] / [text] / 按钮行按 M3 的次序与间距排进面板，
 *   并逐段下发内容色与文本样式。
 *
 * 三个内容槽位都可空：为 null 时该段**整块不占位**（不画空 Box、不留间距）。
 * 按钮由调用方通过 [confirmButton] / [dismissButton] 传入，组件不预设按钮外观。
 * 文本样式取主题 [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.Typography] 的 `subtitle`（标题）与
 * `body`（正文、图标），两者差一档即可拉开层次。
 *
 * **显隐照 M3 的写法用 `if` 控制组合**：`if (show) { AlertDialog(onDismissRequest = { show = false }, ...) }`。
 * 出现 / 消失动画由平台 `Dialog` 承担（图层快照重放）：它会在本组件被移出组合**之后**继续把退场播完，
 * 因此按钮、遮罩、Esc 三条关闭路径都有完整退场，调用方不需要为动画做任何事。
 *
 * 两段过渡（时长 / 纵向位移 / 缓动）来自主题 meta 的 `enter_animation` / `exit_animation` 段，
 * 经 `toDialogTransition` 映射成平台的过渡参数；改 meta 即可全局调，
 * 单个弹窗可用 [enterAnimation] / [exitAnimation] 覆盖。
 *
 * 配色回退链：`调用点传参` > [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuColor] 作用域 >
 * [AlertDialogTokens] > [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorScheme]；
 * 尺寸默认值来自主题 meta 的 `alert_dialog` 段（见 [AlertDialogMeta]），资源包可覆盖。
 *
 * @param onDismissRequest 关闭请求（Esc / 点击遮罩 / 按钮自行调用）
 * @param confirmButton 确认按钮（必填，M3 同样把确认按钮设为必需的槽位）
 * @param dismissButton 取消按钮，null 时不显示
 * @param icon 图标，null 时不显示
 * @param title 标题，null 时不显示
 * @param text 正文，null 时不显示
 * @param colors 配色集，默认 [AlertDialogDefaults.colors]
 * @param sprite 面板精灵，默认 [AlertDialogDefaults.sprite]（浮动面板素材）
 * @param contentPadding 面板内容内边距，默认取 meta
 * @param minWidth 面板最小宽度，默认取 meta
 * @param maxWidth 面板最大宽度，默认取 meta
 * @param buttonsTopPadding 按钮行与上方内容的间距，默认取 meta
 * @param buttonSpacing 按钮间距，默认取 meta
 * @param enterAnimation 入场过渡，默认取 meta（从下方滑入 + 淡入，减速抵达）
 * @param exitAnimation 退场过渡，默认取 meta（滑回下方 + 淡出，时长更短、加速离场）
 * @param properties 平台对话框属性（关闭策略、限宽、scrim 色等）
 */
@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    colors: AlertDialogColors = AlertDialogDefaults.colors(),
    sprite: SokitsuSprite = AlertDialogDefaults.sprite,
    contentPadding: PaddingValues = AlertDialogDefaults.contentPadding,
    minWidth: Dp = AlertDialogDefaults.minWidth,
    maxWidth: Dp = AlertDialogDefaults.maxWidth,
    buttonsTopPadding: Dp = AlertDialogDefaults.buttonsTopPadding,
    buttonSpacing: Dp = AlertDialogDefaults.buttonSpacing,
    enterAnimation: AlertDialogAnimationMeta = AlertDialogDefaults.enterAnimation,
    exitAnimation: AlertDialogAnimationMeta = AlertDialogDefaults.exitAnimation,
    properties: DialogProperties = DialogProperties(),
) {
    val meta = AlertDialogDefaults.meta

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties.withTransitions(enterAnimation, exitAnimation),
    ) {
        Surface(
            modifier = modifier,
            color = colors.containerColor,
            contentColor = colors.textContentColor,
            sprite = sprite,
        ) {
            // 宽度约束落在 Column 上而非 Surface：面板宽度由内容（含内边距）决定，
            // 按钮行才能对齐到面板内容的右边缘，而不是对齐到 Surface 外框。
            Column(
                modifier = Modifier
                    .widthIn(min = minWidth, max = maxWidth)
                    .padding(contentPadding),
            ) {
                icon?.let { iconContent ->
                    Box(Modifier.padding(bottom = meta.iconBottomPadding)) {
                        ProvideContentColorTextStyle(colors.iconContentColor, SokitsuTheme.typography.body) {
                            iconContent()
                        }
                    }
                }

                title?.let { titleContent ->
                    Box(Modifier.padding(bottom = meta.titleBottomPadding)) {
                        ProvideContentColorTextStyle(colors.titleContentColor, SokitsuTheme.typography.subtitle) {
                            titleContent()
                        }
                    }
                }

                text?.let { textContent ->
                    ProvideContentColorTextStyle(colors.textContentColor, SokitsuTheme.typography.body) {
                        textContent()
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

/**
 * 把 meta 的两段动画写进平台 `DialogProperties`：平台 `DialogProperties` 没有 `copy`，
 * 这里原样透传其余字段，只覆盖 `enterTransition` / `exitTransition`
 * （即调用方在 properties 里自带的过渡会被本组件的 enterAnimation / exitAnimation 接管）。
 */
private fun DialogProperties.withTransitions(
    enter: AlertDialogAnimationMeta,
    exit: AlertDialogAnimationMeta,
): DialogProperties = DialogProperties(
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    usePlatformDefaultWidth = usePlatformDefaultWidth,
    usePlatformInsets = usePlatformInsets,
    useSoftwareKeyboardInset = useSoftwareKeyboardInset,
    scrimColor = scrimColor,
    animateTransition = animateTransition,
    enterTransition = enter.toDialogTransition(),
    exitTransition = exit.toDialogTransition(),
)

/**
 * 提示对话框配色集：容器色板 + 三处内容色。
 *
 * 与 [ButtonColors] 的差别：对话框没有交互四态（面板本身不可点），
 * 因此这里是"平坦"的一组颜色而非 [UiStateColor]。
 *
 * 各项默认由 [AlertDialogDefaults.colors] 按 [AlertDialogTokens] 解析；
 * 自定义时用 data class `copy` 精准微调（如只换标题色：`colors.copy(titleContentColor = ...)`）。
 */
@Immutable
data class AlertDialogColors(
    /** 面板容器精灵的染色色板。 */
    val containerColor: Color,
    /** 图标着色。 */
    val iconContentColor: Color,
    /** 标题文本色。 */
    val titleContentColor: Color,
    /** 正文文本色。 */
    val textContentColor: Color,
)

object AlertDialogDefaults {

    /** 当前主题的提示对话框 meta（尺寸 + 面板精灵 id）。 */
    inline val meta get() = SokitsuThemeMeta.alertDialog

    /** 面板精灵，默认取 [AlertDialogMeta.panelSprite]（`ui/surface/float_panel`，凸起带阴影）。 */
    val sprite: SokitsuSprite get() = SokitsuThemeMeta.uiSprite(meta.panelSprite)

    /** 面板最小宽度，来自 meta 的 `alert_dialog` 段（**单位 dp**）。 */
    val minWidth: Dp get() = meta.minWidth

    /** 面板最大宽度，来自 meta 的 `alert_dialog` 段。 */
    val maxWidth: Dp get() = meta.maxWidth

    /** 面板内容内边距，来自 meta 的 `alert_dialog` 段。 */
    val contentPadding: PaddingValues get() = meta.contentPadding

    /** 按钮行与上方内容的间距，来自 meta 的 `alert_dialog` 段。 */
    val buttonsTopPadding: Dp get() = meta.buttonsTopPadding

    /** 按钮间距，来自 meta 的 `alert_dialog` 段。 */
    val buttonSpacing: Dp get() = meta.buttonSpacing

    /** 入场动画，来自 meta 的 `alert_dialog.enter_animation` 段。 */
    val enterAnimation: AlertDialogAnimationMeta get() = meta.enterAnimation

    /** 退场动画，来自 meta 的 `alert_dialog.exit_animation` 段。 */
    val exitAnimation: AlertDialogAnimationMeta get() = meta.exitAnimation

    /**
     * 默认配色集：四个参数**全部默认为未指定**（[Color.Unspecified]），
     * 语义是"调用方没意见，请按 [AlertDialogTokens] 映射表结合当前主题解析"。
     *
     * 显式传值即完全接管该槽位（见 [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve]）。
     */
    @Composable
    fun colors(
        containerColor: Color = Color.Unspecified,
        iconContentColor: Color = Color.Unspecified,
        titleContentColor: Color = Color.Unspecified,
        textContentColor: Color = Color.Unspecified,
    ): AlertDialogColors = AlertDialogColors(
        containerColor = containerColor.resolve(AlertDialogTokens.Container),
        iconContentColor = iconContentColor.resolve(AlertDialogTokens.IconContent),
        titleContentColor = titleContentColor.resolve(AlertDialogTokens.TitleContent),
        textContentColor = textContentColor.resolve(AlertDialogTokens.TextContent),
    )
}
