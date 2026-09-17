package moe.forpleuvoir.ibukigourd.ui.sokitsu.toast

import androidx.compose.runtime.Composable
import kotlin.time.Duration

/**
 * 一条提示的数据载体：内容 + 展示时长 + 可选标识与自定义动画。
 *
 * 时长语义见 [ToastState.remaining]：`[ToastHandler.tick]` 每帧扣减，扣到 `-EXIT_GRACE` 后才移出
 * 活动列表，故退场动画期间对象仍然存活。
 *
 * @param content 提示内容（按 [ToastContainer] 提供的主题作用域组合）
 * @param duration 展示时长；`<= Duration.ZERO` 表示不自动消失，且不绘制倒计时条
 * @param tag 策略标识，仅 [ToastStrategy.Tagged] 系列会读取
 * @param animation 本条提示专用的进出场动画；null = 用主题 meta 组的默认动画
 */
class Toast(
    val content: @Composable () -> Unit,
    val duration: Duration,
    val tag: String? = null,
    val animation: ToastAnimation? = null,
)
