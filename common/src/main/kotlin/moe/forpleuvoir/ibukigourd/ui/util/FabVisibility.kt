package moe.forpleuvoir.ibukigourd.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.ScrollerAdapter
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** 浮动按钮的显隐状态。 */
enum class FabVisibilityState {
    Visible,
    Hidden,
}

/**
 * 方向感知的滚动显隐状态：**向下**滚动累计超过 `hideDistance` 收起，一检测到**向上**滚动立即恢复，
 * 回到起始端恒为可见；单次位移不超过 2 像素的抖动不计入。
 *
 * 状态由 [Modifier.fabScrollVisibility] 在嵌套滚动回调（`onPreScroll`）里维护 ——
 * 该回调发生在滚动事件派发期，既不在组合期、也不在帧时钟派发器上：
 * 滚动**不得**触发组合期或协程里的快照写入，否则会与本平台的测量 / 快照锁撞成永久死等。
 *
 * 位移取「首项索引 / 偏移」编码后的单调标记差值（[lazyScrollMarker]），同索引内按像素累计，
 * 索引变化视为一次大位移。
 */
@Stable
class FabScrollVisibility internal constructor(
    private val hideDistancePx: Float,
    private val position: () -> Long,
) : NestedScrollConnection {

    private var last = position()
    private var accumulated = 0f

    private var visible by mutableStateOf(true)

    /** 当前显隐状态。 */
    val state: FabVisibilityState
        get() = if (visible) FabVisibilityState.Visible else FabVisibilityState.Hidden

    /** 每次滚动派发时按最新位移推进一次判定。 */
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val current = position()
        val delta = current - last
        last = current

        when {
            current <= 0L -> {
                accumulated = 0f
                visible = true
            }

            abs(delta) <= SCROLL_DEAD_ZONE -> Unit

            delta > 0L -> {
                accumulated += delta.toFloat()
                if (accumulated >= hideDistancePx) visible = false
            }

            else -> {
                accumulated = 0f
                visible = true
            }
        }

        return Offset.Zero
    }
}

/**
 * 为 [LazyListState] 创建方向感知的滚动显隐状态；把返回对象交给 [Modifier.fabScrollVisibility]
 * 挂在**滚动容器的祖先**上即可。
 *
 * [state] 为 `null` 时恒为 [FabVisibilityState.Visible]。
 */
@Composable
fun rememberFabScrollVisibility(
    state: LazyListState?,
    hideDistance: Dp = FabVisibilityDefaults.hideDistance,
): FabScrollVisibility {
    val hideDistancePx = with(LocalDensity.current) { hideDistance.toPx() }
    return remember(state, hideDistancePx) {
        FabScrollVisibility(hideDistancePx) {
            state?.let { lazyScrollMarker(it.firstVisibleItemIndex, it.firstVisibleItemScrollOffset) } ?: 0L
        }
    }
}

/** 为 [LazyGridState] 创建方向感知的滚动显隐状态，语义同 [rememberFabScrollVisibility] 的列表版。 */
@Composable
fun rememberFabScrollVisibility(
    state: LazyGridState?,
    hideDistance: Dp = FabVisibilityDefaults.hideDistance,
): FabScrollVisibility {
    val hideDistancePx = with(LocalDensity.current) { hideDistance.toPx() }
    return remember(state, hideDistancePx) {
        FabScrollVisibility(hideDistancePx) {
            state?.let { lazyScrollMarker(it.firstVisibleItemIndex, it.firstVisibleItemScrollOffset) } ?: 0L
        }
    }
}

/** 为 [LazyStaggeredGridState] 创建方向感知的滚动显隐状态，语义同 [rememberFabScrollVisibility] 的列表版。 */
@Composable
fun rememberFabScrollVisibility(
    state: LazyStaggeredGridState?,
    hideDistance: Dp = FabVisibilityDefaults.hideDistance,
): FabScrollVisibility {
    val hideDistancePx = with(LocalDensity.current) { hideDistance.toPx() }
    return remember(state, hideDistancePx) {
        FabScrollVisibility(hideDistancePx) {
            state?.let { lazyScrollMarker(it.firstVisibleItemIndex, it.firstVisibleItemScrollOffset) } ?: 0L
        }
    }
}

/** 为 [ScrollState] 创建方向感知的滚动显隐状态，语义同 [rememberFabScrollVisibility] 的列表版。 */
@Composable
fun rememberFabScrollVisibility(
    state: ScrollState?,
    hideDistance: Dp = FabVisibilityDefaults.hideDistance,
): FabScrollVisibility {
    val hideDistancePx = with(LocalDensity.current) { hideDistance.toPx() }
    return remember(state, hideDistancePx) {
        FabScrollVisibility(hideDistancePx) { state?.value?.toLong() ?: 0L }
    }
}

/** 为 [ScrollerAdapter] 创建方向感知的滚动显隐状态，语义同 [rememberFabScrollVisibility] 的列表版。 */
@Composable
fun rememberFabScrollVisibility(
    adapter: ScrollerAdapter?,
    hideDistance: Dp = FabVisibilityDefaults.hideDistance,
): FabScrollVisibility {
    val hideDistancePx = with(LocalDensity.current) { hideDistance.toPx() }
    return remember(adapter, hideDistancePx) {
        FabScrollVisibility(hideDistancePx) { adapter?.scrollOffset?.toLong() ?: 0L }
    }
}

/**
 * 把 [visibility] 接入嵌套滚动：挂在滚动容器的**祖先**节点上，滚动派发时更新显隐判定，不消费位移。
 */
fun Modifier.fabScrollVisibility(visibility: FabScrollVisibility): Modifier =
    nestedScroll(visibility)

/**
 * 浮动按钮显隐动画：按 [state] 与 [shouldHide] 在透明度 / 纵向位移 / 缩放上过渡。
 *
 * 该修饰符每帧重录图层，滚动驱动的高频显隐（如列表滚动）不要使用；
 * 那种场景直接用 [FabVisibilityState] 控制组合即可。
 *
 * @param state 滚动推导出的显隐状态
 * @param shouldHide 额外的强制隐藏条件，默认取隐藏动作键的按下状态
 * @param hideDuration 过渡时长
 * @param translationY 隐藏态的纵向位移
 */
@Composable
fun Modifier.fabVisibilityAnimation(
    state: FabVisibilityState,
    shouldHide: Boolean = rememberHideActionState(),
    hideDuration: Duration = FabVisibilityDefaults.hideDuration,
    translationY: Dp = FabVisibilityDefaults.translationY,
): Modifier {
    val progress by animateFloatAsState(
        targetValue = if (state == FabVisibilityState.Hidden || shouldHide) 0f else 1f,
        animationSpec = tween(hideDuration.inWholeMilliseconds.toInt()),
        label = "fabVisibilityAnimation",
    )
    val translationPx = with(LocalDensity.current) { translationY.toPx() }

    return graphicsLayer {
        alpha = progress
        this.translationY = (1f - progress) * translationPx
        scaleX = progress
        scaleY = progress
    }
}

/** [rememberFabScrollVisibility] / [Modifier.fabVisibilityAnimation] 的默认参数。 */
object FabVisibilityDefaults {

    /** 触发隐藏所需的累计向下滚动距离。 */
    val hideDistance: Dp = 100.dp

    /** 显隐过渡时长。 */
    val hideDuration: Duration = 200.milliseconds

    /** 隐藏态的纵向位移。 */
    val translationY: Dp = 30.dp
}

/** 索引位宽：偏移量小于 4096 像素时，标记在索引内与像素一一对应。 */
private const val LAZY_MARKER_SHIFT = 12

/** 单次位移的容差：不超过该像素数的抖动不改变方向判定。 */
private const val SCROLL_DEAD_ZONE = 2L

/** 把「首项索引 + 首项偏移」编码成单调递增的滚动标记：索引变化必然越过常规 [FabVisibilityDefaults.hideDistance]。 */
private fun lazyScrollMarker(index: Int, offset: Int): Long =
    (index.toLong() shl LAZY_MARKER_SHIFT) or (offset.toLong() and ((1L shl LAZY_MARKER_SHIFT) - 1))
