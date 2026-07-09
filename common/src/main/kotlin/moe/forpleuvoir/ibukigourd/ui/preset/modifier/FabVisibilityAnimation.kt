package moe.forpleuvoir.ibukigourd.ui.preset.modifier

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import moe.forpleuvoir.ibukigourd.ui.preset.state.FabVisibilityState
import moe.forpleuvoir.ibukigourd.ui.preset.state.rememberHideActionState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun Modifier.fabVisibilityAnimation(
    state: FabVisibilityState,
    shouldHide: Boolean = rememberHideActionState(),
    hideDuration: Duration = 200.milliseconds,
    translationY: Float = 60f,
): Modifier {
    val displayProgress by animateFloatAsState(
        targetValue = if (state == FabVisibilityState.Hidden || shouldHide) 0f else 1f,
        animationSpec = tween(hideDuration.inWholeMilliseconds.toInt()),
        label = "fabVisibilityAnimation"
    )
    return graphicsLayer {
        alpha = displayProgress
        this.translationY = (1f - displayProgress) * translationY
        scaleX = displayProgress
        scaleY = displayProgress
    }
}