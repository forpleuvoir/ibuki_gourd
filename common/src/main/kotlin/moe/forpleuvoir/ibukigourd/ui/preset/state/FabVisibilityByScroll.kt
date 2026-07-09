package moe.forpleuvoir.ibukigourd.ui.preset.state

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

enum class FabVisibilityState {
    Visible,
    Hidden
}

@Composable
private fun rememberFabVisibilityByScrollImpl(
    key: Any?,
    hideDistancePx: Float,
    valueProvider: () -> Double
): FabVisibilityState {
    var lastValue by remember(key) { mutableDoubleStateOf(0.0) }
    var hideAccum by remember(key) { mutableDoubleStateOf(0.0) }
    var hidden by remember(key) { mutableStateOf(false) }

    val currentValue = valueProvider()
    val delta = currentValue - lastValue

    if (abs(delta) >= 8) {
        lastValue = currentValue
        if (delta > 0) {
            hideAccum += delta
            if (hideAccum >= hideDistancePx) {
                hidden = true
            }
        } else {
            hideAccum = 0.0
            hidden = false
        }
    }

    return if (hidden) FabVisibilityState.Hidden else FabVisibilityState.Visible
}

@Composable
fun rememberFabVisibilityByScroll(
    scrollState: ScrollState?,
    hideDistance: Dp = 100.dp,
): FabVisibilityState {
    val density = LocalDensity.current
    val hideDistancePx = with(density) { hideDistance.toPx() }
    return rememberFabVisibilityByScrollImpl(scrollState, hideDistancePx) {
        scrollState?.value?.toDouble() ?: 0.0
    }
}

@Composable
private fun lazyFabVisibilityByScroll(
    state: Any,
    getIndex: () -> Int,
    getOffset: () -> Int,
    hideDistancePx: Float,
): FabVisibilityState {
    var lastIndex by remember(state) { mutableIntStateOf(0) }
    var lastOffset by remember(state) { mutableIntStateOf(0) }
    var hideAccum by remember(state) { mutableDoubleStateOf(0.0) }
    var hidden by remember(state) { mutableStateOf(false) }

    val index = getIndex()
    val offset = getOffset()

    if (index == lastIndex) {
        val delta = offset - lastOffset
        if (abs(delta) >= 8) {
            if (delta > 0) {
                hideAccum += delta
            } else {
                hideAccum = 0.0
                hidden = false
            }
        }
    } else if (index > lastIndex) {
        hideAccum += offset.toDouble()
        lastIndex = index
    } else {
        hideAccum = 0.0
        hidden = false
        lastIndex = index
    }
    lastOffset = offset

    if (!hidden && hideAccum >= hideDistancePx) {
        hidden = true
    }

    return if (hidden) FabVisibilityState.Hidden else FabVisibilityState.Visible
}

@Composable
fun rememberFabVisibilityByScroll(
    state: LazyGridState?,
    hideDistance: Dp = 100.dp,
): FabVisibilityState {
    if (state == null) return FabVisibilityState.Visible
    val density = LocalDensity.current
    val hideDistancePx = with(density) { hideDistance.toPx() }
    return lazyFabVisibilityByScroll(
        state = state,
        getIndex = { state.firstVisibleItemIndex },
        getOffset = { state.firstVisibleItemScrollOffset },
        hideDistancePx = hideDistancePx,
    )
}

@Composable
fun rememberFabVisibilityByScroll(
    state: LazyListState?,
    hideDistance: Dp = 100.dp,
): FabVisibilityState {
    if (state == null) return FabVisibilityState.Visible
    val density = LocalDensity.current
    val hideDistancePx = with(density) { hideDistance.toPx() }
    return lazyFabVisibilityByScroll(
        state = state,
        getIndex = { state.firstVisibleItemIndex },
        getOffset = { state.firstVisibleItemScrollOffset },
        hideDistancePx = hideDistancePx,
    )
}

@Composable
fun rememberFabVisibilityByScroll(
    adapter: ScrollbarAdapter?,
    hideDistance: Dp = 100.dp,
): FabVisibilityState {
    val density = LocalDensity.current
    val hideDistancePx = with(density) { hideDistance.toPx() }
    return rememberFabVisibilityByScrollImpl(adapter, hideDistancePx) {
        adapter?.scrollOffset ?: 0.0
    }
}
