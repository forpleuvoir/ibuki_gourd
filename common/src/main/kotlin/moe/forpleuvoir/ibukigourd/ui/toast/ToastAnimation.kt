package moe.forpleuvoir.ibukigourd.ui.toast

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.time.Duration

data class ToastAnimation(
    val enter: EnterTransition = fadeIn(tween(300)) + slideInVertically(tween(300)) { it },
    val exit: ExitTransition = fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it / 4 }
)

val LocalToastAnimation = staticCompositionLocalOf { ToastAnimation() }
val LocalToastDuration = staticCompositionLocalOf { Duration.ZERO }
val LocalToastRefreshCounter = staticCompositionLocalOf { 0 }
