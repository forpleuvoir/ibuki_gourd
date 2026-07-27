package moe.forpleuvoir.ibukigourd.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.ui.preset.LocalInheritedAlpha
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.gui.screens.Screen
import kotlin.time.Duration.Companion.milliseconds

fun ComposePopupScreen(
    onDismissRequest: (() -> Unit)? = null,
    pauseGame: Boolean = mc.screen?.isPauseScreen ?: IGConfig.Gui.Screen.pauseGame,
    renderParent: Boolean = true,
    parentScreen: Screen? = mc.screen,
    shouldRenderLevel: (ComposeScreen) -> Boolean = {
        renderParent && (!parentScreen.isComposeScreen() || (parentScreen as ComposeScreen).shouldRenderLevel(parentScreen))
    },
    contentWrapper: @Composable (@Composable () -> Unit) -> Unit = { content -> DefaultAnimatedDialogEntry(content) },
    content: @Composable () -> Unit
) = ComposeScreen(
    pauseGame,
    renderParent,
    parentScreen,
    shouldRenderLevel,
    { contentWrapper(content) }
).apply {
    fadeInDuration = (-1).milliseconds
    onDismissRequest?.let { this@apply.onClosed(it) }
    parentScreen?.let {
        this@apply.onResize { width, height -> it.resize(width, height) }
    }
}

fun openComposePopupScreen(
    onDismissRequest: (() -> Unit)? = null,
    pauseGame: Boolean = mc.screen?.isPauseScreen ?: IGConfig.Gui.Screen.pauseGame,
    renderParent: Boolean = true,
    parentScreen: Screen? = mc.screen,
    shouldRenderLevel: (ComposeScreen) -> Boolean = {
        renderParent && (!parentScreen.isComposeScreen() || (parentScreen as ComposeScreen).shouldRenderLevel(parentScreen))
    },
    contentWrapper: @Composable (@Composable () -> Unit) -> Unit = { content -> DefaultAnimatedDialogEntry(content) },
    content: @Composable () -> Unit
) = ComposePopupScreen(
    onDismissRequest,
    pauseGame,
    renderParent,
    parentScreen,
    shouldRenderLevel,
    contentWrapper,
    content
).open()

@Composable
fun DefaultAnimatedDialogEntry(content: @Composable () -> Unit) {
    val enterEasing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
    val durationMs = IGConfig.Gui.Screen.fadeInDuration.inWholeMilliseconds.toInt()
    val overlayState = rememberComposeScreenVisibilityState()
    val contentState = rememberComposeScreenVisibilityState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visibleState = overlayState,
            enter = fadeIn(animationSpec = tween(durationMs, easing = enterEasing)),
            exit = fadeOut(animationSpec = tween(durationMs, easing = enterEasing)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        closeScreen()
                    },
            )
        }

        AnimatedVisibility(
            visibleState = contentState,
            enter = fadeIn(animationSpec = tween(durationMs, easing = enterEasing)) +
                    scaleIn(initialScale = 0.8f, animationSpec = tween(durationMs, easing = enterEasing)),
            exit = fadeOut(animationSpec = tween(durationMs, easing = enterEasing)) +
                    scaleOut(targetScale = 0.8f, animationSpec = tween(durationMs, easing = enterEasing)),
        ) {
            val animProgress by this.transition.animateFloat(label = "dialogAnimProgress") {
                when (it) {
                    EnterExitState.PreEnter -> 0f
                    EnterExitState.Visible  -> 1f
                    EnterExitState.PostExit -> 0f
                }
            }
            CompositionLocalProvider(LocalInheritedAlpha provides animProgress) {
                content()
            }
        }
    }
}
