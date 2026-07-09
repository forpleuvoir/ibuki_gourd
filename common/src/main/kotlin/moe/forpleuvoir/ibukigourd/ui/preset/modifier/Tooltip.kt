@file:Suppress("NOTHING_TO_INLINE")
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package moe.forpleuvoir.ibukigourd.ui.preset.modifier

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import moe.forpleuvoir.ibukigourd.ui.scene.LocalPopupHost
import moe.forpleuvoir.ibukigourd.ui.scene.PopupEntry
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import net.minecraft.network.chat.Component
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tooltip 相对于锚组件的偏好方向。
 *
 * 定位策略按"最佳适配"回退：偏好方向 → 反向 → 正交方向（上下/左右互转）→ 空间更大的一侧，
 * 最终再用 `coerceIn` 防止溢出 root。
 */
enum class TooltipAnchorPosition {
    /** 锚组件上方：上方不足→下方→左右更宽一侧。 */
    Above,

    /** 锚组件下方：下方不足→上方→左右更宽一侧。 */
    Below,

    /** 锚组件左侧（RTL 下为右侧）：左侧不足→右侧→上下更高一侧。 */
    Start,

    /** 锚组件右侧（RTL 下为左侧）：右侧不足→左侧→上下更高一侧。 */
    End,
}

/**
 * [tooltip] content 的作用域，暴露当前展示状态，外部据此自行驱动进出场动画。
 */
@Stable
interface TooltipScope {
    /**
     * 是否应处于展示态。
     *
     * - `mounted` 首帧必定为 `false`，紧接下一切到 `true`，使外部 `AnimatedVisibility` 能从隐藏态真正过渡出现；
     * - 鼠标移出/点外部时切回 `false`，外部由此播放退出动画（保持 `false` 直到 Popup 卸载）。
     */
    val visible: Boolean
}

@Stable
private class TooltipScopeImpl(override val visible: Boolean) : TooltipScope

/**
 * 基于锚组件在 root 坐标系中的 [Rect] 定位 Tooltip：
 * 沿 [TooltipAnchorPosition] 放置，垂直/水平方向均相对锚组件**居中对齐**。
 *
 * 回退链：偏好方向 → 反向 → 正交方向（上/下 ↔ 左/右互转）→ 空间更大的一侧，
 * 最后用 `coerceIn` 夹取避免溢出 root。
 */
private class AnchorBoundsPositionProvider(
    private val anchorBounds: () -> Rect,
    private val position: TooltipAnchorPosition,
    private val spacing: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val bounds = this@AnchorBoundsPositionProvider.anchorBounds().roundToIntRect()
        val tooltipW = popupContentSize.width.coerceAtLeast(0)
        val tooltipH = popupContentSize.height.coerceAtLeast(0)
        val rootW = windowSize.width
        val rootH = windowSize.height

        val spaceAbove = bounds.top - spacing
        val spaceBelow = rootH - bounds.bottom - spacing
        val rtl = layoutDirection == LayoutDirection.Rtl
        val spaceStart = if (rtl) rootW - bounds.right - spacing else bounds.left - spacing
        val spaceEnd = if (rtl) bounds.left - spacing else rootW - bounds.right - spacing

        fun pickVertical(pref: TooltipAnchorPosition, opp: TooltipAnchorPosition): TooltipAnchorPosition = when {
            (if (pref == TooltipAnchorPosition.Above) spaceAbove else spaceBelow) >= tooltipH -> pref
            (if (opp == TooltipAnchorPosition.Above) spaceAbove else spaceBelow) >= tooltipH  -> opp
            // 上下都放不下，转向左右
            spaceStart >= tooltipW                                                            -> TooltipAnchorPosition.Start
            spaceEnd >= tooltipW                                                              -> TooltipAnchorPosition.End
            else                                                                              -> if (spaceStart >= spaceEnd) TooltipAnchorPosition.Start else TooltipAnchorPosition.End
        }

        fun pickHorizontal(pref: TooltipAnchorPosition, opp: TooltipAnchorPosition): TooltipAnchorPosition = when {
            (if (pref == TooltipAnchorPosition.Start) spaceStart else spaceEnd) >= tooltipW -> pref
            (if (opp == TooltipAnchorPosition.Start) spaceStart else spaceEnd) >= tooltipW  -> opp
            // 左右都放不下，转向上下
            spaceAbove >= tooltipH                                                          -> TooltipAnchorPosition.Above
            spaceBelow >= tooltipH                                                          -> TooltipAnchorPosition.Below
            else                                                                            -> if (spaceAbove >= spaceBelow) TooltipAnchorPosition.Above else TooltipAnchorPosition.Below
        }

        val effective = when (position) {
            TooltipAnchorPosition.Above -> pickVertical(TooltipAnchorPosition.Above, TooltipAnchorPosition.Below)
            TooltipAnchorPosition.Below -> pickVertical(TooltipAnchorPosition.Below, TooltipAnchorPosition.Above)
            TooltipAnchorPosition.Start -> pickHorizontal(TooltipAnchorPosition.Start, TooltipAnchorPosition.End)
            TooltipAnchorPosition.End   -> pickHorizontal(TooltipAnchorPosition.End, TooltipAnchorPosition.Start)
        }

        val xRange = spacing..(rootW - tooltipW - spacing).coerceAtLeast(spacing)
        val yRange = spacing..(rootH - tooltipH - spacing).coerceAtLeast(spacing)

        return when (effective) {
            TooltipAnchorPosition.Above -> {
                val y = (bounds.top - tooltipH - spacing).coerceIn(yRange)
                val x = (bounds.left + (bounds.width - tooltipW) / 2).coerceIn(xRange)
                IntOffset(x, y)
            }

            TooltipAnchorPosition.Below -> {
                val y = (bounds.bottom + spacing).coerceIn(yRange)
                val x = (bounds.left + (bounds.width - tooltipW) / 2).coerceIn(xRange)
                IntOffset(x, y)
            }

            TooltipAnchorPosition.Start -> {
                val x = if (rtl) {
                    (bounds.right + spacing).coerceIn(xRange)
                } else {
                    (bounds.left - tooltipW - spacing).coerceIn(xRange)
                }
                val y = (bounds.top + (bounds.height - tooltipH) / 2).coerceIn(yRange)
                IntOffset(x, y)
            }

            TooltipAnchorPosition.End   -> {
                val x = if (rtl) {
                    (bounds.left - tooltipW - spacing).coerceIn(xRange)
                } else {
                    (bounds.right + spacing).coerceIn(xRange)
                }
                val y = (bounds.top + (bounds.height - tooltipH) / 2).coerceIn(yRange)
                IntOffset(x, y)
            }
        }
    }
}

/**
 * 为组件附加悬停 Tooltip。
 *
 * 用法（完全自定义外观与动画）：
 * ```
 * Modifier.tooltip {          // TooltipScope
 *     AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
 *         Surface { Text("提示") }
 *     }
 * }
 * ```
 *
 * 该 Modifier **不提供默认外观与动画**：
 * - 不内置 `Surface`，由 content 自行决定容器/背景/形状；
 * - 不内置进出场动画，通过 [TooltipScope.visible] 将展示状态交给外部，
 *   外部可用 `AnimatedVisibility`、`transition` 等自行驱动；
 * - Popup 首帧 [TooltipScope.visible] 必为 `false`，紧接下一帧才切到 `true`，
 *   使外部进入动画能真正从隐藏态过渡（不经这层隔离，`AnimatedVisibility` 会把首帧即显示当作"已显示"、跳过 enter）；
 * - 鼠标移出/点外部时 `TooltipScope.visible` 切回 `false`，并保留 [exitDuration] 再卸载 Popup，
 *   为外部退出动画留出锅;**[exitDuration] 应不小于外部退出动画时长**，否则会被截断。
 *
 * 定位：默认相对锚组件居中对齐并沿 [position] 偏移，空间不足自动反向翻转，
 * 翻转后仍居中对齐。`spacing` 同时充当与锚组件的间距和与 root 边缘的安全间距。
 *
 * @param delay 鼠标悬停到展示之间的延迟，用于避免抖动时频繁弹窗；默认 100ms。
 * @param position 锚点偏好方向，空间不足时自动反向翻转。
 * @param spacing Tooltip 与锚组件之间的间距。
 * @param exitDuration 退出动画预留时长；`visible` 变 `false` 后再过此时长卸载 Popup。
 * @param content Tooltip 内容，接收 [TooltipScope]，自行包装外观与动画。
 */
@Composable
fun Modifier.tooltip(
    delay: Duration = 100.milliseconds,
    position: TooltipAnchorPosition = TooltipAnchorPosition.Above,
    spacing: Int = 4,
    exitDuration: Duration = 150.milliseconds,
    content: @Composable TooltipScope.() -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    var anchorBounds by remember { mutableStateOf(Rect.Zero) }
    // 控制是否在 Popup 内 active：false 时外部应播 exit。
    // 首帧 Popup 内会强制为 false 一次，无论它的初值如何，确保 AnimatedVisibility 能播 enter。
    var active by remember { mutableStateOf(false) }
    var mounted by remember { mutableStateOf(false) }

    // 进入：等 delay 后挂载 Popup；Popup 内部会自己把 visible 从 false 切到 true 触发 enter。
    // 退出：直接关掉 active，外部立即播 exit；再等 exitDuration 后卸载 Popup。
    LaunchedEffect(isHovered) {
        if (isHovered) {
            delay(delay)
            active = true
            mounted = true
        } else if (mounted) {
            // 进入未完成后离开时不播 exit
            active = false
            delay(exitDuration)
            mounted = false
        }
    }

    val positionProvider = remember(position, spacing) {
        AnchorBoundsPositionProvider(
            anchorBounds = { anchorBounds },
            position = position,
            spacing = spacing,
        )
    }

    val popupHost = LocalPopupHost.current
    val popupKey = remember { Any() }

    val colorScheme = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    val typography = MaterialTheme.typography
    val scrollbarStyle = LocalScrollbarStyle.current

    if (popupHost != null) {
        LaunchedEffect(mounted) {
            if (mounted) {
                popupHost.show(
                    PopupEntry(
                        key = popupKey,
                        positionProvider = positionProvider,
                        onDismissRequest = { active = false },
                        properties = PopupProperties(
                            focusable = false,
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true,
                        ),
                        content = {
                            MaterialTheme(
                                colorScheme = colorScheme,
                                shapes = shapes,
                                typography = typography,
                            ) {
                                CompositionLocalProvider(
                                    LocalScrollbarStyle provides scrollbarStyle,
                                ) {
                                    var firstFrame by remember { mutableStateOf(true) }
                                    LaunchedEffect(Unit) {
                                        firstFrame = false
                                    }
                                    val scopeVisible = active && !firstFrame
                                    TooltipScopeImpl(scopeVisible).content()
                                }
                            }
                        },
                    )
                )
                try {
                    awaitCancellation()
                } finally {
                    popupHost.hide(popupKey)
                }
            }
        }
    }

    return this
        .hoverable(interactionSource)
        .onGloballyPositioned { anchorBounds = it.boundsInRoot() }
}

/**
 * 默认 Tooltip 容器，参数与 material3 `TooltipScope.PlainTooltip` 对齐（去掉 caret 相关）。
 *
 * 在 [tooltip] 的 content 中使用：
 * ```
 * Modifier.tooltip {
 *     PlainTooltip { Text("提示") }
 * }
 * ```
 *
 * 仅负责容器外观（`Surface` + 内容内边距 + 默认文字样式与颜色），
 * **不含进出场动画**——动画由外部自行选择（参见 [fadeScaleTooltip]）。
 *
 * @param modifier 应用到容器 `Surface` 的 [Modifier]。
 * @param maxWidth 最大宽度；超出会换行。
 * @param shape 容器形状，默认取主题 `Shapes.extraSmall`。
 * @param contentColor 文字颜色，默认 `colorScheme.inverseOnSurface`。
 * @param containerColor 容器背景色，默认 `colorScheme.inverseSurface`。
 * @param tonalElevation 容器色调高度。
 * @param shadowElevation 容器阴影高度。
 * @param content Tooltip 内容。
 */
@Composable
fun TooltipScope.PlainTooltip(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 600.dp,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    contentColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    containerColor: Color = MaterialTheme.colorScheme.inverseSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    ) {
        Box(
            modifier = Modifier
                .sizeIn(minWidth = 40.dp, maxWidth = maxWidth, minHeight = 24.dp)
                .padding(PaddingValues(horizontal = 8.dp, vertical = 4.dp))
        ) {
            CompositionLocalProvider(
                LocalContentColor provides contentColor,
                LocalTextStyle provides MaterialTheme.typography.bodySmall,
                content = content,
            )
        }
    }
}

/**
 * 默认 fade+scale 进出场动画包装，搭配 [tooltip] / [PlainTooltip] 使用。
 *
 * - 进入：`scaleIn(initialScale = 0.8f)` + `fadeIn`
 * - 退出：`scaleOut(targetScale = 0.8f)` + `fadeOut`
 *
 * 用法：
 * ```
 * Modifier.tooltip {
 *     fadeScaleTooltip {
 *         PlainTooltip { Text("提示") }
 *     }
 * }
 * ```
 *
 * @param content 任意 Tooltip 内容；通常为 [PlainTooltip]。
 */
@Composable
fun TooltipScope.fadeScaleTooltip(content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(150)) +
                scaleIn(initialScale = 0.8f, animationSpec = tween(200)),
        // 退出比进入更快，避免 hover 移出后视觉滞后
        exit = fadeOut(animationSpec = tween(75)) +
                scaleOut(targetScale = 0.8f, animationSpec = tween(100)),
    ) {
        content()
    }
}

/**
 * 一键 Plain Tooltip：默认容器 + 默认 fade/scale 动画 + 单行文本。
 *
 * 等价于：
 * ```
 * Modifier.tooltip {
 *     fadeScaleTooltip {
 *         PlainTooltip(modifier, maxWidth, shape, contentColor, containerColor, tonalElevation, shadowElevation) {
 *             Text(text)
 *         }
 *     }
 * }
 * ```
 *
 * @param text 展示的文本。
 * @param modifier 应用到容器 `Surface` 的 [Modifier]。
 * @param maxWidth 最大宽度。
 * @param shape 容器形状。
 * @param contentColor 文字颜色。
 * @param containerColor 容器背景色。
 * @param tonalElevation 容器色调高度。
 * @param shadowElevation 容器阴影高度。
 * @param delay 鼠标悬停到展示之间的延迟。
 * @param position 锚点偏好方向。
 * @param spacing 与锚组件的间距。
 * @param exitDuration 退出动画预留时长。
 */
@Composable
fun Modifier.plainTooltip(
    text: String,
    modifier: Modifier = Modifier,
    maxWidth: Dp = 600.dp,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    contentColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    containerColor: Color = MaterialTheme.colorScheme.inverseSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    delay: Duration = 100.milliseconds,
    position: TooltipAnchorPosition = TooltipAnchorPosition.Above,
    spacing: Int = 4,
    exitDuration: Duration = 150.milliseconds,
): Modifier = tooltip(
    delay = delay,
    position = position,
    spacing = spacing,
    exitDuration = exitDuration,
) {
    fadeScaleTooltip {
        PlainTooltip(
            modifier = modifier,
            maxWidth = maxWidth,
            shape = shape,
            contentColor = contentColor,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            shadowElevation = shadowElevation,
        ) {
            Text(text)
        }
    }
}

/**
 * 一键 Plain Tooltip：默认容器 + 默认 fade/scale 动画 + 单行文本。
 *
 * 等价于：
 * ```
 * Modifier.tooltip {
 *     fadeScaleTooltip {
 *         PlainTooltip(modifier, maxWidth, shape, contentColor, containerColor, tonalElevation, shadowElevation) {
 *             Text(text)
 *         }
 *     }
 * }
 * ```
 *
 * @param component 展示的文本。
 * @param modifier 应用到容器 `Surface` 的 [Modifier]。
 * @param maxWidth 最大宽度。
 * @param shape 容器形状。
 * @param contentColor 文字颜色。
 * @param containerColor 容器背景色。
 * @param tonalElevation 容器色调高度。
 * @param shadowElevation 容器阴影高度。
 * @param delay 鼠标悬停到展示之间的延迟。
 * @param position 锚点偏好方向。
 * @param spacing 与锚组件的间距。
 * @param exitDuration 退出动画预留时长。
 */
@Composable
fun Modifier.plainTooltip(
    component: Component,
    modifier: Modifier = Modifier,
    maxWidth: Dp = 600.dp,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    contentColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    containerColor: Color = MaterialTheme.colorScheme.inverseSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    delay: Duration = 100.milliseconds,
    position: TooltipAnchorPosition = TooltipAnchorPosition.Above,
    spacing: Int = 4,
    exitDuration: Duration = 150.milliseconds,
): Modifier = tooltip(
    delay = delay,
    position = position,
    spacing = spacing,
    exitDuration = exitDuration,
) {
    fadeScaleTooltip {
        PlainTooltip(
            modifier = modifier,
            maxWidth = maxWidth,
            shape = shape,
            contentColor = contentColor,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            shadowElevation = shadowElevation,
        ) {
            Text(component)
        }
    }
}


/**
 * 一键 Plain Tooltip：默认容器 + 默认 fade/scale 动画 + 单行文本。
 *
 * 等价于：
 * ```
 * Modifier.tooltip {
 *     fadeScaleTooltip {
 *         PlainTooltip(modifier, maxWidth, shape, contentColor, containerColor, tonalElevation, shadowElevation) {
 *             content()
 *         }
 *     }
 * }
 * ```
 *
 * @param modifier 应用到容器 `Surface` 的 [Modifier]。
 * @param maxWidth 最大宽度。
 * @param shape 容器形状。
 * @param contentColor 文字颜色。
 * @param containerColor 容器背景色。
 * @param tonalElevation 容器色调高度。
 * @param shadowElevation 容器阴影高度。
 * @param delay 鼠标悬停到展示之间的延迟。
 * @param position 锚点偏好方向。
 * @param spacing 与锚组件的间距。
 * @param exitDuration 退出动画预留时长。
 * @param content 展示的内容。
 */
@Composable
fun Modifier.plainTooltip(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 600.dp,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    contentColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    containerColor: Color = MaterialTheme.colorScheme.inverseSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    delay: Duration = 100.milliseconds,
    position: TooltipAnchorPosition = TooltipAnchorPosition.Above,
    spacing: Int = 4,
    exitDuration: Duration = 150.milliseconds,
    content: @Composable () -> Unit,
): Modifier = tooltip(
    delay = delay,
    position = position,
    spacing = spacing,
    exitDuration = exitDuration,
) {
    fadeScaleTooltip {
        PlainTooltip(
            modifier = modifier,
            maxWidth = maxWidth,
            shape = shape,
            contentColor = contentColor,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            shadowElevation = shadowElevation,
        ) {
            content()
        }
    }
}