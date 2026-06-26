@file:OptIn(ExperimentalComposeUiApi::class)

package moe.forpleuvoir.ibukigourd.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.gui.screens.Screen

fun ComposePopupScreen(
    onDismissRequest: (() -> Unit)? = null,
    pauseGame: Boolean = mc.screen?.isPauseScreen ?: IGConfig.Gui.Screen.pauseGame,
    renderParent: Boolean = true,
    parentScreen: Screen? = mc.screen,
    shouldRenderLevel: (ComposeScreen) -> Boolean = {
        renderParent && if (parentScreen.isComposeScreen()) {
            (parentScreen as ComposeScreen).shouldRenderLevel(parentScreen)
        } else true
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
    onDismissRequest?.let { this@apply.onClose(it) }
    parentScreen?.let { this@apply.onInit(it::initScreen) }
}

fun openComposePopupScreen(
    onDismissRequest: (() -> Unit)? = null,
    pauseGame: Boolean = mc.screen?.isPauseScreen ?: IGConfig.Gui.Screen.pauseGame,
    renderParent: Boolean = true,
    parentScreen: Screen? = mc.screen,
    shouldRenderLevel: (ComposeScreen) -> Boolean = {
        renderParent && if (parentScreen.isComposeScreen()) {
            (parentScreen as ComposeScreen).shouldRenderLevel(parentScreen)
        } else true
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

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun DefaultAnimatedDialogEntry(content: @Composable () -> Unit) {
    val enterEasing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
    val durationMs = IGConfig.Gui.Screen.fadeInDuration.inWholeMilliseconds.toInt()
    val duration = IGConfig.Gui.Screen.fadeInDuration
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = visible,
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
                        visible = false
                        GlobalScope.launch {
                            delay(duration)
                            closeScreen()
                        }
                    },
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMs, easing = enterEasing)) +
                    scaleIn(initialScale = 0.8f, animationSpec = tween(durationMs, easing = enterEasing)),
            exit = fadeOut(animationSpec = tween(durationMs, easing = enterEasing)) +
                    scaleOut(targetScale = 0.8f, animationSpec = tween(durationMs, easing = enterEasing)),
        ) {
            content()
        }
    }
}
