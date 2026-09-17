package moe.forpleuvoir.ibukigourd.ui.sokitsu.toast

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.time.Duration

/**
 * 一条提示的进出场动画。
 *
 * 由 [ToastDefaults.animation] 按主题 meta（时长 / 位移比例）组装成默认值；
 * 调用方可在 [Toast.animation] 上整条覆盖，或用 [LocalToastAnimation] 在子树内覆盖。
 */
data class ToastAnimation(
    val enter: EnterTransition,
    val exit: ExitTransition,
)

/**
 * 子树内的动画覆盖：null = 用 [ToastDefaults.animation]（主题 meta 驱动）。
 *
 * 默认值为 null 而非 `ToastAnimation()` 实例，是因为 [staticCompositionLocalOf] 的默认值
 * 只在首次求值时构造一次，资源重载后不会跟随 meta 更新。
 */
val LocalToastAnimation = staticCompositionLocalOf<ToastAnimation?> { null }

/** 当前提示的总展示时长，供内容自绘进度等使用（不自动消失时为 [Duration.ZERO]）。 */
val LocalToastDuration = staticCompositionLocalOf { Duration.ZERO }

/** 当前提示被原地刷新的次数（每次 [ToastHandler] 刷新自增），可用作 `LaunchedEffect` 的重启键。 */
val LocalToastRefreshCounter = staticCompositionLocalOf { 0 }
