package moe.forpleuvoir.ibukigourd.ui.preset.state

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.runtime.*
import kotlin.math.abs

@Composable
private fun rememberScrollFabVisibilityProgressImpl(
    key: Any?,
    threshold: Int,
    hideDistance: Int,
    reverse: Boolean,
    valueProvider: () -> Double
): Float {
    var lastScroll by remember(key) { mutableDoubleStateOf(0.0) }
    var showFab by remember(key) { mutableStateOf(true) }
    var accumulatedHide by remember(key) { mutableDoubleStateOf(0.0) }

    LaunchedEffect(key) {
        if (key == null) return@LaunchedEffect

        var initialized = false

        snapshotFlow { valueProvider() }
            .collect { value ->

                if (!initialized) {
                    initialized = true
                    lastScroll = value
                    showFab = true
                    accumulatedHide = 0.0
                    return@collect
                }

                val delta = value - lastScroll

                if (abs(delta) < threshold) return@collect

                val goingDown = delta > 0
                val hideDirection = if (reverse) !goingDown else goingDown

                if (hideDirection) {
                    accumulatedHide += abs(delta)
                    if (accumulatedHide >= hideDistance) {
                        showFab = false
                    }
                } else {
                    accumulatedHide = 0.0
                    showFab = true
                }

                lastScroll = value
            }
    }

    return animateFloatAsState(
        targetValue = if (showFab) 1f else 0f,
        label = "fab"
    ).value
}

@Composable
fun rememberScrollFabVisibilityProgress(
    scrollState: ScrollState?,
    threshold: Int = 8,
    hideDistance: Int = 100,
    reverse: Boolean = false
): Float = rememberScrollFabVisibilityProgressImpl(scrollState, threshold, hideDistance, reverse) {
    scrollState?.value?.toDouble() ?: 0.0
}

@Composable
fun rememberScrollFabVisibilityProgress(
    state: LazyGridState?,
    threshold: Int = 8,
    hideDistance: Int = 100,
    reverse: Boolean = false
): Float = rememberScrollFabVisibilityProgressImpl(state, threshold, hideDistance, reverse) {
    state?.let { it.firstVisibleItemIndex * 10000 + it.firstVisibleItemScrollOffset }?.toDouble() ?: 0.0
}

@Composable
fun rememberScrollFabVisibilityProgress(
    state: LazyListState?,
    threshold: Int = 8,
    hideDistance: Int = 100,
    reverse: Boolean = false
): Float = rememberScrollFabVisibilityProgressImpl(state, threshold, hideDistance, reverse) {
    state?.let { it.firstVisibleItemIndex * 10000 + it.firstVisibleItemScrollOffset }?.toDouble() ?: 0.0
}

@Composable
fun rememberScrollFabVisibilityProgress(
    adapter: ScrollbarAdapter?,
    threshold: Int = 8,
    hideDistance: Int = 100,
    reverse: Boolean = false
): Float = rememberScrollFabVisibilityProgressImpl(adapter, threshold, hideDistance, reverse) {
    adapter?.scrollOffset ?: 0.0
}
