package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun AutoHideVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    autoHideDelay: Duration = 2.seconds,
) {
    val isScrolling = scrollState.isScrollInProgress
    var visible by remember { mutableStateOf(isScrolling) }
    LaunchedEffect(isScrolling) {
        if (isScrolling) {
            visible = true
        } else {
            delay(autoHideDelay)
            visible = false
        }
    }
    val scrollbarAlpha by animateFloatAsState(if (visible) 1f else 0f)
    if (visible || scrollbarAlpha > 0f) {
        VerticalScrollbar(
            rememberScrollbarAdapter(scrollState),
            modifier = modifier.alpha(scrollbarAlpha),
        )
    }
}

@Composable
fun AutoHideHorizontalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    autoHideDelay: Duration = 2.seconds,
) {
    val isScrolling = scrollState.isScrollInProgress
    var visible by remember { mutableStateOf(isScrolling) }
    LaunchedEffect(isScrolling) {
        if (isScrolling) {
            visible = true
        } else {
            delay(autoHideDelay)
            visible = false
        }
    }
    val scrollbarAlpha by animateFloatAsState(if (visible) 1f else 0f)
    if (visible || scrollbarAlpha > 0f) {
        HorizontalScrollbar(
            rememberScrollbarAdapter(scrollState),
            modifier = modifier.alpha(scrollbarAlpha),
        )
    }
}
