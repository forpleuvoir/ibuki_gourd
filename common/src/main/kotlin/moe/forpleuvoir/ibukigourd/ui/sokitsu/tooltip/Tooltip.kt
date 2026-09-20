package moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import moe.forpleuvoir.ibukigourd.render.extension.AnchorPosition
import moe.forpleuvoir.ibukigourd.ui.sokitsu.LocalTextStyle
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.BubblePanel
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ProvideContentColorTextStyle
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.fromToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import kotlin.time.Duration

/**
 * 带气泡外观的悬停提示：[basicTooltip] 的主题化封装 —— 补上精灵、尺寸、内边距、内容色与进出场动画。
 *
 * [basicTooltip] 只管触发 / 定位 / 弹层生命周期，参数全显式、不读主题；本 Modifier 读
 * [TooltipDefaults]（气泡体与箭头精灵、[TooltipDefaults.minSize]、[TooltipDefaults.padding]、
 * [TooltipDefaults.spacing]、[TooltipDefaults.delay]、[TooltipDefaults.exitDuration]），
 * 自建 [TooltipPositionProvider]（并负责捕获锚点 bounds）交给 [basicTooltip]，
 * 弹层内容是一枚 [BubblePanel]。
 *
 * 落点与箭头**以定位器的结果为准**：[TooltipPositionProvider.resolvedPosition] 决定箭头贴哪条边
 * （空间不足时可能与 [position] 不同），[TooltipPositionProvider.arrowRatio] 决定箭头沿该边的位置
 * —— 后者取自目标中心的投影，故气泡被屏幕边缘夹偏离目标时，箭头仍指向目标。
 *
 * 动画：进场淡入 + 自 [TooltipPositionProvider.resolvedPosition] 一侧滑入（幅度为该方向尺寸的
 * 四分之一），出场反向收回；进出场时长均为 [exitDuration]，弹层在 [TooltipScope.visible] 转
 * `false` 后再过 [exitDuration] 卸载，故 [exitDuration] 不得小于退出动画时长。
 *
 * @param interactionSource 交互源；为 null 时内部自建并挂上 `hoverable`，
 *   传入则由调用方负责挂 `hoverable`
 * @param position 气泡偏好位置（气泡在目标的 X 侧），空间不足时由 [TooltipPositionProvider] 翻转
 * @param delay 悬停到展示之间的延迟，默认 [TooltipDefaults.delay]
 * @param exitDuration 进出场动画时长，兼弹层卸载预留时长，默认 [TooltipDefaults.exitDuration]
 * @param color 气泡体染色色板；未指定按 [TooltipTokens.Body] 解析
 * @param contentColor 气泡内内容色；未指定按 [TooltipTokens.Content] 解析
 * @param textStyle 下发给气泡内文本的样式；默认沿用当前 [LocalTextStyle]
 * @param pinned 钉住展示：为 `true` 时忽略悬停与 [TooltipDefaults.delay] 直接展示，
 *   且弹层不再被外部点击关闭 —— 适用于"点击后进入某状态、期间要持续看到提示"的场景
 * @param content 气泡内内容
 */
@Composable
fun Modifier.tooltip(
    interactionSource: MutableInteractionSource? = null,
    position: AnchorPosition = AnchorPosition.Above,
    delay: Duration = TooltipDefaults.delay,
    exitDuration: Duration = TooltipDefaults.exitDuration,
    color: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    textStyle: TextStyle = LocalTextStyle.current,
    pinned: Boolean = false,
    content: @Composable () -> Unit,
): Modifier {
    val density = LocalDensity.current
    val body = TooltipDefaults.bodySprite()
    val resolvedColor = color.resolve(TooltipTokens.Body)
    val resolvedContent = contentColor.takeOrElse {
        LocalColorScheme.current.fromToken(TooltipTokens.Content)
    }

    // 锚点 bounds 由本 Modifier 捕获，供定位器惰性读取（定位器另需回写落点/箭头位置）
    var anchorBounds by remember { mutableStateOf(Rect.Zero) }
    val positionProvider = remember(position, density) {
        TooltipPositionProvider(
            anchorBounds = { anchorBounds },
            position = position,
            density = density,
            spacing = TooltipDefaults.spacing,
        )
    }

    return basicTooltip(
        interactionSource = interactionSource,
        delay = delay,
        positionProvider = { positionProvider },
        exitDuration = exitDuration,
        pinned = pinned,
    ) {
        val anchor = positionProvider.resolvedPosition
        val arrow = TooltipDefaults.arrowSprite(anchor)
        AnimatedVisibility(
            visible = this.visible,
            enter = bubbleEnter(anchor, exitDuration),
            exit = bubbleExit(anchor, exitDuration),
        ) {
            ProvideContentColorTextStyle(
                contentColor = resolvedContent,
                textStyle = textStyle,
            ) {
                BubblePanel(
                    body = body,
                    arrow = arrow,
                    arrowAnchor = anchor,
                    arrowRatio = positionProvider.arrowRatio,
                    color = resolvedColor,
                    minSize = TooltipDefaults.minSize,
                    contentPadding = TooltipDefaults.padding,
                ) { content() }
            }
        }
    }
        .onGloballyPositioned { anchorBounds = it.boundsInRoot() }
}

/**
 * 进场：淡入 + 自 [position] 一侧滑入，位移幅度为该方向尺寸的四分之一。
 *
 * 气泡在 [AnchorPosition.Above]（位于目标上方）时自上方（-Y）滑向目标，
 * [AnchorPosition.Left] 自左侧（-X）滑入，其余方向取反。
 */
private fun bubbleEnter(position: AnchorPosition, duration: Duration): EnterTransition {
    val ms = duration.inWholeMilliseconds.toInt()
    return fadeIn(tween(ms)) + when (position) {
        AnchorPosition.Above -> slideInVertically(tween(ms)) { -it / 4 }
        AnchorPosition.Below -> slideInVertically(tween(ms)) { it / 4 }
        AnchorPosition.Left -> slideInHorizontally(tween(ms)) { -it / 4 }
        AnchorPosition.Right -> slideInHorizontally(tween(ms)) { it / 4 }
    }
}

/** 出场：[bubbleEnter] 的逆向 —— 淡出并滑回 [position] 一侧。 */
private fun bubbleExit(position: AnchorPosition, duration: Duration): ExitTransition {
    val ms = duration.inWholeMilliseconds.toInt()
    return fadeOut(tween(ms)) + when (position) {
        AnchorPosition.Above -> slideOutVertically(tween(ms)) { -it / 4 }
        AnchorPosition.Below -> slideOutVertically(tween(ms)) { it / 4 }
        AnchorPosition.Left -> slideOutHorizontally(tween(ms)) { -it / 4 }
        AnchorPosition.Right -> slideOutHorizontally(tween(ms)) { it / 4 }
    }
}
