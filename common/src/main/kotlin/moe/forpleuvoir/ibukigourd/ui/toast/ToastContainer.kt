@file:OptIn(InternalComposeUiApi::class, ExperimentalComposeUiApi::class)

package moe.forpleuvoir.ibukigourd.ui.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import kotlin.time.Duration
import kotlin.time.DurationUnit

private val ToastPlacement = Alignment { size, space, _ ->
    IntOffset(
        x = (space.width - size.width) / 2,
        y = ((space.height * 0.85f - size.height / 2f)).toInt()
    )
}

@Composable
fun ToastContainer() {
    Box(Modifier.fillMaxSize()) {
        ToastHandler.active.forEach { state ->
            key(state) {
                Box(Modifier.align(ToastPlacement)) {
                    ToastItem(state)
                }
            }
        }
    }
}

@Composable
fun ToastContent(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    propagateMinConstraints: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val duration = LocalToastDuration.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        tonalElevation = 3.dp,
    ) {
        Column(Modifier.width(IntrinsicSize.Max), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment, propagateMinConstraints) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.inverseOnSurface,
                ) {
                    content()
                }
            }
            if (duration > Duration.ZERO) {
                ToastProgressBar(duration)
            }
        }
    }
}

@Composable
private fun ToastProgressBar(duration: Duration) {
    val refreshCounter = LocalToastRefreshCounter.current
    val animatable = remember { Animatable(1f) }

    LaunchedEffect(duration, refreshCounter) {
        animatable.snapTo(1f)
        animatable.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                duration.toLong(DurationUnit.MILLISECONDS).toInt(),
                easing = LinearEasing
            )
        )
    }

    LinearProgressIndicator(
        progress = { animatable.value },
        modifier = Modifier.fillMaxWidth().height(2.dp),
        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.6f),
        trackColor = Color.Transparent,
    )
}

@Composable
private fun ToastItem(state: ToastHandler.ToastState) {
    val anim = LocalToastAnimation.current
    val toastAnim = state.toast.animation ?: anim
    val transitionState = remember { MutableTransitionState(false) }

    LaunchedEffect(state, state.refreshCounter) {
        transitionState.targetState = state.remaining > Duration.ZERO
        if (state.remaining > Duration.ZERO) {
            snapshotFlow { state.remaining }
                .first { it <= Duration.ZERO }
            transitionState.targetState = false
        }
    }

    AnimatedVisibility(
        visibleState = transitionState,
        enter = toastAnim.enter,
        exit = toastAnim.exit
    ) {
        CompositionLocalProvider(
            LocalToastAnimation provides toastAnim,
            LocalToastDuration provides state.toast.duration,
            LocalToastRefreshCounter provides state.refreshCounter
        ) {
            state.toast.content()
        }
    }
}
