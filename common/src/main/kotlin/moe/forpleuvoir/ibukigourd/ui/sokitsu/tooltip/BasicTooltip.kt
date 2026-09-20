package moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import moe.forpleuvoir.compose_minecraft.platform.ui.popup.LocalPopupHost
import moe.forpleuvoir.compose_minecraft.platform.ui.popup.register
import kotlin.time.Duration

/**
 * [tooltip] content 的作用域，暴露当前展示状态，外部据此自行驱动进出场动画。
 */
interface TooltipScope {

    /**
     * 是否应处于展示态。
     *
     * - 挂载首帧恒为 `false`，紧接下一切到 `true`，使外部 `AnimatedVisibility` 能从隐藏态真正过渡出现；
     * - 指针移出时切回 `false`，外部由此播放退出动画（保持 `false` 直到弹层卸载）。
     */
    val visible: Boolean
}

private class TooltipScopeImpl(override val visible: Boolean) : TooltipScope

/**
 * 为组件附加悬停提示：指针悬停 [delay] 后按 [positionProvider] 定位弹出 [content]。
 *
 * **基础原语**：只负责触发、定位与弹层生命周期，不提供任何外观。带气泡外观、内边距、
 * 内容色与进出场动画的封装见 [tooltip]。
 *
 * 本 Modifier **不提供任何外观**：
 * - 不内置容器、背景、内边距、文字样式，由 [content] 自行决定；
 * - 不内置进出场动画，展示状态经 [TooltipScope.visible] 交给外部，
 *   外部可用 `AnimatedVisibility` / `transition` 自行驱动；
 * - 不读取任何主题 / meta：偏好方向、与锚组件的间距、翻转与夹取策略全部由
 *   [positionProvider] 决定（例如 [TooltipPositionProvider] 的 `position` 与 `spacing`）；
 * - 弹层经 [LocalPopupHost] 注册在场景根，**不产生布局节点**（无需 `Box` 包裹锚组件）；
 *   注册处的主题与自定义 CompositionLocal 由 [register] 自动透传。
 *
 * 生命周期：悬停 → 等待 [delay] → 挂载弹层（首帧 [TooltipScope.visible] 为 `false`）→
 * 指针移出 → [TooltipScope.visible] 转 `false` → 等待 [exitDuration] → 卸载弹层。
 * [exitDuration] 应不小于外部退出动画时长，否则退出动画会被截断。
 *
 * @param interactionSource 交互源；为 null 时内部自建并挂上 [hoverable]，
 *   传入则由调用方负责挂 `hoverable`（多个提示共享同一交互源时用）
 * @param delay 悬停到展示之间的延迟，用于避免指针划过时频繁弹窗
 * @param positionProvider 定位器工厂；入参是锚组件在 root 坐标系下 bounds 的惰性读取器
 *   （内部由 `boundsInRoot` 记录，首帧可能是 `Rect.Zero`），返回的定位器决定弹层落点。
 *   写成工厂而非实例，是因为锚点只能由本 Modifier 自己的 `onGloballyPositioned` 捕获
 * @param exitDuration 退出动画预留时长；[TooltipScope.visible] 转 `false` 后再过此时长卸载弹层
 * @param pinned 钉住展示：为 `true` 时忽略悬停与 [delay] 直接展示，且弹层不再被外部点击关闭
 *   （适用于"点击后进入某种状态、期间需要持续看到提示"的场景）
 * @param properties 弹层行为配置；默认非聚焦（不拦截下层指针，避免多图层悬停闪烁）
 * @param content 提示内容，接收 [TooltipScope]；外观与动画全部由它决定
 */
@Composable
fun Modifier.basicTooltip(
    interactionSource: MutableInteractionSource? = null,
    delay: Duration,
    positionProvider: (anchorBounds: () -> Rect) -> PopupPositionProvider,
    exitDuration: Duration,
    pinned: Boolean = false,
    properties: PopupProperties = PopupProperties(focusable = false),
    content: @Composable TooltipScope.() -> Unit,
): Modifier {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isHovered by source.collectIsHoveredAsState()

    var anchorBounds by remember { mutableStateOf(Rect.Zero) }
    // 弹层内是否处于展示态：false 时外部应播退出动画
    var active by remember { mutableStateOf(false) }
    // 弹层是否已挂载：挂载期独立于 active，为退出动画留出时间
    var mounted by remember { mutableStateOf(false) }

    LaunchedEffect(isHovered, pinned) {
        if (pinned || isHovered) {
            if (!pinned) delay(delay)
            active = true
            mounted = true
        } else if (mounted) {
            active = false
            delay(exitDuration)
            mounted = false
        }
    }

    val provider = remember(positionProvider) { positionProvider { anchorBounds } }

    val popupKey = remember { Any() }
    val popupHost = LocalPopupHost.current

    if (popupHost != null && mounted) {
        popupHost.register(
            key = popupKey,
            positionProvider = provider,
            onDismissRequest = { if (!pinned) active = false },
            properties = if (pinned) properties.withoutClickDismiss() else properties,
            content = {
                var firstFrame by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) { firstFrame = false }
                TooltipScopeImpl(active && !firstFrame).content()
            },
        )
    }

    return this
        .let { if (interactionSource == null) it.hoverable(source) else it }
        .onGloballyPositioned { anchorBounds = it.boundsInRoot() }
}

/** 复制 [PopupProperties] 并关闭「点击外部关闭」：该类不是 data class，需逐项重建。 */
private fun PopupProperties.withoutClickDismiss(): PopupProperties = PopupProperties(
    focusable = focusable,
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = false,
    clippingEnabled = clippingEnabled,
    usePlatformDefaultWidth = usePlatformDefaultWidth,
    usePlatformInsets = usePlatformInsets,
)
