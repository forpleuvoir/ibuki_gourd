package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.scrollBy
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.snapToDensity
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.util.mc
import kotlin.math.roundToInt

/**
 * 滚动条与滚动组件之间的桥：把"滚到哪儿了 / 能滚多远 / 滚到某处"抽象成三个像素域成员。
 *
 * 形态与 compose-multiplatform 的 `ScrollbarAdapter` 一致 —— 滚动条只认这三个成员，
 * 不认识具体是列表、`ScrollState` 还是 lazy 列表，因此天然覆盖各种滚动源。
 * 全部单位为**内容像素**（`scrollOffset` 与 `maxScrollOffset` 同量纲）。
 */
interface ScrollerAdapter {

    /** 当前滚动位置（内容像素，0 = 起始端）。 */
    val scrollOffset: Float

    /**
     * 滚动到 [scrollOffset]（内容像素），越界值由实现方收敛到 `0..maxScrollOffset`。
     *
     * @param containerSize 滚动容器在主轴上的尺寸（像素）—— `maxScrollOffset` 依赖它，
     *   因为同一份内容在不同视口下可滚行程不同
     */
    suspend fun scrollTo(containerSize: Int, scrollOffset: Float)

    /** 可滚动行程（内容像素）= 内容主轴长 − [containerSize]。 */
    fun maxScrollOffset(containerSize: Int): Float
}

/**
 * 由 [ScrollState] 构造 [ScrollerAdapter]：三项全部直读 `ScrollState`，不做任何换算。
 *
 * [ScrollerAdapter.maxScrollOffset] 忽略 `containerSize` 是因为 `ScrollState.maxValue` 已由滚动容器
 * 在测量期写为 `contentSize − viewportSize`，与容器尺寸同源；唯一例外是 CMP 的
 * `Int.MAX_VALUE` 初始哨兵（"尚未测量、值未知"），按 0（无可滚动空间）处理，见实现内注释。
 */
fun ScrollerAdapter(scrollState: ScrollState): ScrollerAdapter = ScrollStateScrollerAdapter(scrollState)

private class ScrollStateScrollerAdapter(
    private val scrollState: ScrollState,
) : ScrollerAdapter {

    override val scrollOffset: Float get() = scrollState.value.toFloat()

    override suspend fun scrollTo(containerSize: Int, scrollOffset: Float) {
        scrollState.scrollTo(scrollOffset.roundToInt())
    }

    override fun maxScrollOffset(containerSize: Int): Float {
        // CMP 的 ScrollState 初始 maxValue = Int.MAX_VALUE（"尚未测量、值未知"哨兵，首次测量
        // 才写入真值）。未知按"无可滚动空间"处理：首帧（滚动源未测量）保持空组合，测量写入
        // 真值后再正确表态；否则首帧会被未知值误判为"有滚动空间"，闪一帧死滚动条。
        val max = scrollState.maxValue
        return if (max == Int.MAX_VALUE) 0f else max.toFloat()
    }
}

/** 按 [ScrollState] 记住一个 [ScrollerAdapter]，实例随 state 变化重建。 */
@Composable
fun rememberScrollerAdapter(scrollState: ScrollState): ScrollerAdapter = remember(scrollState) {
    ScrollerAdapter(scrollState)
}

/**
 * 由 [LazyListState] 构造 [ScrollerAdapter]。
 *
 * 懒列表不持有"内容总长 / 已滚像素"这类精确值，各成员只能从最近一次布局的
 * [androidx.compose.foundation.lazy.LazyListLayoutInfo] 估计：以可见条目的平均主轴尺寸
 * 为换算单位 —— 当前滚动位置 ≈ `首条索引 × 平均尺寸 − 首条偏移`，
 * 内容总长 ≈ `总条数 × 平均尺寸`。条目尺寸越均匀，滑块位置与实际内容越贴合。
 * 可滚行程对**列表自身的 viewportSize**（而非传入的 containerSize）作差：
 * 该值由列表在测量期与 layoutInfo 一起写入，滚动条尚未测量（组合期判定）时也能拿到。
 */
fun ScrollerAdapter(lazyListState: LazyListState): ScrollerAdapter = LazyListStateScrollerAdapter(lazyListState)

/** 按 [LazyListState] 记住一个 [ScrollerAdapter]，实例随 state 变化重建。 */
@Composable
fun rememberScrollerAdapter(lazyListState: LazyListState): ScrollerAdapter = remember(lazyListState) {
    ScrollerAdapter(lazyListState)
}

private class LazyListStateScrollerAdapter(
    private val state: LazyListState,
) : ScrollerAdapter {

    /**
     * 条目间距（像素；"主轴尺寸 + 条目间距"，即相邻条目起点的距离）；无可见条目时为 0。
     *
     * 以可见条目为样本估计全表，口径与上游 `visibleItemsAverageSize()` 一致。
     * **排除粘性表头**：`stickyHeader` 被钉在视口顶端时会出现在 `visibleItemsInfo` 里，
     * 但它的 index 落在滚动位置之前（不属于滚动区间）；把它算进样本，平均值会随可见行数
     * 每帧变化，滑块的**位置与长度**随之抖动。
     */
    private val itemPitch: Float
        get() {
            val info = state.layoutInfo
            val firstIndex = state.firstVisibleItemIndex
            var sum = 0
            var count = 0
            info.visibleItemsInfo.forEach { item ->
                if (item.index >= firstIndex) {
                    sum += item.size
                    count++
                }
            }
            if (count == 0) return 0f
            return sum.toFloat() / count + info.mainAxisItemSpacing
        }

    /**
     * 当前滚动位置（估计像素）。
     *
     * 位置取自列表自身的滚动位置（[LazyListState.firstVisibleItemIndex] /
     * [LazyListState.firstVisibleItemScrollOffset]），**不**取
     * `layoutInfo.visibleItemsInfo` 的第一项：粘性表头（`stickyHeader`）被钉在视口顶端时也会出现在
     * `visibleItemsInfo` 里（index 0 / offset 0），拿它当位置会让滑块永远停在顶端。
     *
     * 注意 [LazyListState.firstVisibleItemScrollOffset] 的语义是**向前滚动为正**
     * （与 `LazyListItemInfo.offset` 的视口内坐标相反），故这里是相加。
     */
    override val scrollOffset: Float
        get() {
            val pitch = itemPitch
            if (pitch <= 0f) return 0f
            return state.firstVisibleItemIndex * pitch + state.firstVisibleItemScrollOffset
        }

    override suspend fun scrollTo(containerSize: Int, scrollOffset: Float) {
        val pitch = itemPitch
        if (pitch <= 0f) return
        // 估计像素 → 估计条目索引：跳到该条目并带上条目内偏移
        val index = (scrollOffset / pitch).toInt()
            .coerceIn(0, (state.layoutInfo.totalItemsCount - 1).coerceAtLeast(0))
        val withinItem = (scrollOffset - index * pitch).roundToInt()
        state.scrollToItem(index, withinItem)
    }

    override fun maxScrollOffset(containerSize: Int): Float {
        // 视口直接取列表最近一次布局写入的 viewportSize，而不用调用方传入的 containerSize：
        // 组合期判定发生在滚动条自身测量之前（containerSize 还是初值 0），若拿全内容长
        // 对 0 比较会误判"有滚动空间"，显示一帧后再隐藏 —— 闪一帧。viewportSize 与
        // visibleItemsInfo 同源（列表测量期同步写入），隐藏期间判定依然正确。
        // 列表未测量（viewport 为 0）时无数据，按"无可滚动空间"处理（保持隐藏）。
        val info = state.layoutInfo
        val viewport = if (info.orientation == Orientation.Vertical) {
            info.viewportSize.height
        } else {
            info.viewportSize.width
        }
        if (viewport <= 0) return 0f
        val pitch = itemPitch
        if (pitch <= 0f) return 0f
        // 全内容长 = 条目尺寸总和 + 条目间距总和 + 首尾内容内边距（同上游 calculateContentSize）
        val itemSize = pitch - info.mainAxisItemSpacing
        val contentSize = itemSize * info.totalItemsCount +
                info.mainAxisItemSpacing * (info.totalItemsCount - 1).coerceAtLeast(0) +
                info.beforeContentPadding + info.afterContentPadding
        return (contentSize - viewport).coerceAtLeast(0f)
    }
}

/**
 * 竖直滚动条（常规样式）：**轨道 + 滑块**两层精灵叠放，滑块沿 y 轴滑动。
 *
 * 结构与 compose-multiplatform 的 `VerticalScrollbar` 同构：轨道是节点自身的背景精灵，
 * 唯一子节点是滑块 `Box`（尺寸与位置由 [MeasurePolicy] 给定 —— 子节点不声明尺寸，
 * 不给死约束就测成 0×0）。方向差异全部收敛进 [ScrollerAxis]，与 [HorizontalScroller]
 * 共用同一份几何、手势与测量实现。
 *
 * 浮于内容之上、不占布局宽度的场景用 [VerticalOverlayScroller]。
 *
 * 几何（内容/轨道均为像素，与 CMP `SliderAdapter` 同式）：
 * - `contentSize = maxScrollOffset + containerSize`；`visiblePart = containerSize / contentSize`；
 * - 滑块长度 = `containerSize × visiblePart`，吸附像素块后夹进 `[滑块最短长, containerSize]`；
 * - 滑块偏移 = `scrollScale × scrollOffset`，`scrollScale = (containerSize − 滑块长) / maxScrollOffset`
 *   —— 两端直接贴边。
 *
 * 三种输入：
 * - **拖滑块**：按**位置增量**叠加（`position += delta`），反算回 `scrollOffset` 交给 adapter；
 * - **点轨道空白**：向该方向滚一屏（`scrollOffset ± containerSize`），**按住不放则连续滚动**
 *   （首次隔 [ScrollerDefaults.TrackPressDelayBeforeRepeat] 后按
 *   [ScrollerDefaults.TrackPressRepeatInterval] 重复），指针移到滑块上即停；
 * - **滚轮**：`scrollDelta` 已是内容像素（输入桥把 GLFW 一格乘成 54），
 *   原样叠加到 adapter，与列表内滚轮走过相同距离。
 *
 * @param adapter 与滚动组件通信的桥；见 [ScrollerAdapter] / [rememberScrollerAdapter]
 * @param modifier 修饰；主轴（高度）有界时铺满，无界时回落 meta 主轴下限
 * @param enabled 是否可交互（禁用态压暗配色且不接收手势）
 * @param autoHide 无可滚动空间（`maxScrollOffset ≤ 0`）时自动隐藏：**完全不组合**（不产生任何节点）。判定在组合期进行，而判定数据（`maxValue` / `layoutInfo`）由滚动容器在测量期写入，故首次出现 / 溢出状态翻转时会晚一帧。默认 false（常驻显示）
 * @param reverseLayout 反转方向（滑块贴末端、滚轮与点击方向取反）
 * @param onValueChangeFinished 手势结束回调，可用于提交 / 保存；null = 不回调
 * @param colors 配色集，默认 [ScrollerDefaults.colors]
 * @param trackSprite 轨道精灵，默认 [ScrollerDefaults.trackSprite]（禁止悬浮/聚焦描边加在轨道上）
 * @param thumbSprite 滑块精灵（四态），默认 [ScrollerDefaults.thumbSprite]
 * @param interactionSource 交互源，不传则内部新建；驱动悬停高亮与 [DragInteraction] 上报
 */
@Composable
fun VerticalScroller(
    adapter: ScrollerAdapter,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    autoHide: Boolean = false,
    reverseLayout: Boolean = false,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: ScrollerColors = ScrollerDefaults.colors(),
    trackSprite: SokitsuSprite = ScrollerDefaults.trackSprite(),
    thumbSprite: UiStateSprite = ScrollerDefaults.thumbSprite(),
    interactionSource: MutableInteractionSource? = null,
) = Scroller(
    adapter = adapter,
    axis = ScrollerAxis.Vertical,
    modifier = modifier,
    enabled = enabled,
    autoHide = autoHide,
    autoFade = false,
    overlay = false,
    reverseLayout = reverseLayout,
    onValueChangeFinished = onValueChangeFinished,
    colors = colors,
    trackSprite = trackSprite,
    thumbSprite = thumbSprite,
    interactionSource = interactionSource,
)

/**
 * 水平滚动条（常规样式）：**轨道 + 滑块**两层精灵叠放，滑块沿 x 轴滑动。
 *
 * 与 [VerticalScroller] 共用同一实现（[Scroller] + [ScrollerAxis.Horizontal]），仅轴向不同：
 * 厚度（高）恒取 meta 细轴尺寸，宽度铺满；滚轮同样读 `scrollDelta.y`
 * —— 鼠标滚轮只有垂直分量，横向滚动条只是把滚动投影到 x 轴，外观才是横向。
 * 浮于内容之上、不占布局高度的场景用 [HorizontalOverlayScroller]。
 *
 * @param adapter 与滚动组件通信的桥；见 [ScrollerAdapter] / [rememberScrollerAdapter]
 * @param modifier 修饰；宽度有界时铺满，无界时回落 meta 主轴下限
 * @param enabled 是否可交互
 * @param autoHide 无可滚动空间（`maxScrollOffset ≤ 0`）时自动隐藏：**完全不组合**（不产生任何节点）。判定在组合期进行，而判定数据（`maxValue` / `layoutInfo`）由滚动容器在测量期写入，故首次出现 / 溢出状态翻转时会晚一帧。默认 false（常驻显示）
 * @param reverseLayout 反转方向
 * @param onValueChangeFinished 手势结束回调
 * @param colors 配色集，默认 [ScrollerDefaults.colors]
 * @param trackSprite 轨道精灵，默认 [ScrollerDefaults.trackSprite]
 * @param thumbSprite 滑块精灵（四态），默认 [ScrollerDefaults.thumbSprite]
 * @param interactionSource 交互源
 */
@Composable
fun HorizontalScroller(
    adapter: ScrollerAdapter,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    autoHide: Boolean = false,
    reverseLayout: Boolean = false,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: ScrollerColors = ScrollerDefaults.colors(),
    trackSprite: SokitsuSprite = ScrollerDefaults.trackSprite(),
    thumbSprite: UiStateSprite = ScrollerDefaults.thumbSprite(),
    interactionSource: MutableInteractionSource? = null,
) = Scroller(
    adapter = adapter,
    axis = ScrollerAxis.Horizontal,
    modifier = modifier,
    enabled = enabled,
    autoHide = autoHide,
    autoFade = false,
    overlay = false,
    reverseLayout = reverseLayout,
    onValueChangeFinished = onValueChangeFinished,
    colors = colors,
    trackSprite = trackSprite,
    thumbSprite = thumbSprite,
    interactionSource = interactionSource,
)

/**
 * 竖直滚动条（叠加样式）：外观与交互与 [VerticalScroller] 完全一致，
 * 仅精灵与最小尺寸取 meta 的 `overlay*` 套装，供**浮于列表内容之上、不占布局宽度**的
 * 场景使用 —— 放置位置（通常贴滚动容器右缘内侧）与"盖在内容上"的层序由调用方的布局决定。
 *
 * @param adapter 与滚动组件通信的桥；见 [ScrollerAdapter] / [rememberScrollerAdapter]
 * @param modifier 修饰；主轴（高度）有界时铺满，无界时回落叠加 meta 主轴下限
 * @param enabled 是否可交互（禁用态压暗配色且不接收手势）
 * @param autoHide 无可滚动空间（`maxScrollOffset ≤ 0`）时自动隐藏：**完全不组合**（不产生任何节点）。判定在组合期进行，而判定数据（`maxValue` / `layoutInfo`）由滚动容器在测量期写入，故首次出现 / 溢出状态翻转时会晚一帧。默认 false（常驻显示）
 * @param autoFade 自动降低可见度：悬浮、拖动、滚动位置变化任一视为活跃；全部退出 [ScrollerDefaults.AutoFadeDelay] 后经动画降到 [ScrollerDefaults.AutoFadeAlpha]，再次活跃即恢复。默认 false（常亮）
 * @param reverseLayout 反转方向（滑块贴末端、滚轮与点击方向取反）
 * @param onValueChangeFinished 手势结束回调，可用于提交 / 保存；null = 不回调
 * @param colors 配色集，默认 [ScrollerDefaults.colors]
 * @param trackSprite 轨道精灵，默认 [ScrollerDefaults.trackSprite]（`overlay = true`，叠加套装）
 * @param thumbSprite 滑块精灵（四态），默认 [ScrollerDefaults.thumbSprite]（`overlay = true`）
 * @param interactionSource 交互源，不传则内部新建；驱动悬停高亮与 [DragInteraction] 上报
 */
@Composable
fun VerticalOverlayScroller(
    adapter: ScrollerAdapter,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    autoHide: Boolean = false,
    autoFade: Boolean = false,
    reverseLayout: Boolean = false,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: ScrollerColors = ScrollerDefaults.colors(),
    trackSprite: SokitsuSprite = ScrollerDefaults.trackSprite(overlay = true),
    thumbSprite: UiStateSprite = ScrollerDefaults.thumbSprite(overlay = true),
    interactionSource: MutableInteractionSource? = null,
) = Scroller(
    adapter = adapter,
    axis = ScrollerAxis.Vertical,
    modifier = modifier,
    enabled = enabled,
    autoHide = autoHide,
    autoFade = autoFade,
    overlay = true,
    reverseLayout = reverseLayout,
    onValueChangeFinished = onValueChangeFinished,
    colors = colors,
    trackSprite = trackSprite,
    thumbSprite = thumbSprite,
    interactionSource = interactionSource,
)

/**
 * 水平滚动条（叠加样式）：外观与交互与 [HorizontalScroller] 完全一致，
 * 仅精灵与最小尺寸取 meta 的 `overlay*` 套装，供**浮于内容之上、不占布局高度**的场景使用，
 * 放置位置与层序由调用方的布局决定。
 *
 * @param adapter 与滚动组件通信的桥；见 [ScrollerAdapter] / [rememberScrollerAdapter]
 * @param modifier 修饰；宽度有界时铺满，无界时回落叠加 meta 主轴下限
 * @param enabled 是否可交互
 * @param autoHide 无可滚动空间（`maxScrollOffset ≤ 0`）时自动隐藏：**完全不组合**（不产生任何节点）。判定在组合期进行，而判定数据（`maxValue` / `layoutInfo`）由滚动容器在测量期写入，故首次出现 / 溢出状态翻转时会晚一帧。默认 false（常驻显示）
 * @param autoFade 自动降低可见度：悬浮、拖动、滚动位置变化任一视为活跃；全部退出 [ScrollerDefaults.AutoFadeDelay] 后经动画降到 [ScrollerDefaults.AutoFadeAlpha]，再次活跃即恢复。默认 false（常亮）
 * @param reverseLayout 反转方向
 * @param onValueChangeFinished 手势结束回调
 * @param colors 配色集，默认 [ScrollerDefaults.colors]
 * @param trackSprite 轨道精灵，默认 [ScrollerDefaults.trackSprite]（`overlay = true`）
 * @param thumbSprite 滑块精灵（四态），默认 [ScrollerDefaults.thumbSprite]（`overlay = true`）
 * @param interactionSource 交互源
 */
@Composable
fun HorizontalOverlayScroller(
    adapter: ScrollerAdapter,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    autoHide: Boolean = false,
    autoFade: Boolean = false,
    reverseLayout: Boolean = false,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: ScrollerColors = ScrollerDefaults.colors(),
    trackSprite: SokitsuSprite = ScrollerDefaults.trackSprite(overlay = true),
    thumbSprite: UiStateSprite = ScrollerDefaults.thumbSprite(overlay = true),
    interactionSource: MutableInteractionSource? = null,
) = Scroller(
    adapter = adapter,
    axis = ScrollerAxis.Horizontal,
    modifier = modifier,
    enabled = enabled,
    autoHide = autoHide,
    autoFade = autoFade,
    overlay = true,
    reverseLayout = reverseLayout,
    onValueChangeFinished = onValueChangeFinished,
    colors = colors,
    trackSprite = trackSprite,
    thumbSprite = thumbSprite,
    interactionSource = interactionSource,
)

/**
 * 滚动条轴向：两个方向的全部分歧都收敛在这里，几何 / 手势 / 测量代码不再出现方向判断。
 *
 * 除主轴取向外还有一条规则：[crossFixedSize] 取"细"那一轴的 meta 尺寸 ——
 * 竖直条宽恒定、水平条高恒定，两者都不吃父级的交叉轴约束（否则会被撑成一大块）。
 */
private enum class ScrollerAxis(val isVertical: Boolean) {

    Vertical(true),
    Horizontal(false);

    /** 指针位置在主轴上的投影。 */
    fun mainPosition(position: Offset): Float = if (isVertical) position.y else position.x

    /** 交叉轴上的固定尺寸（meta 的对应分量）。 */
    fun crossFixedSize(track: DpSize): Dp = if (isVertical) track.width else track.height

    /** 主轴上的尺寸分量。 */
    fun mainSize(size: DpSize): Dp = if (isVertical) size.height else size.width

    /**
     * 滑块子节点的固定约束：主轴 = [thumbMain]、交叉轴 = [cross]。
     * 子节点自身不声明尺寸，两个方向的尺寸都必须由父级给死。
     */
    fun thumbConstraints(thumbMain: Int, cross: Int): Constraints =
        if (isVertical) Constraints.fixed(cross, thumbMain) else Constraints.fixed(thumbMain, cross)

    /**
     * 滚轮增量在主轴上的投影。**两个方向都取 `scrollDelta.y`** ——
     * 鼠标滚轮只有垂直分量，横向滚动条只是把滚动投影到 x 轴。
     */
    fun wheelDelta(scrollDelta: Offset): Float = scrollDelta.y
}

/**
 * 把滑块摆到轨道主轴的 [offset] 处（仅竖直/水平两个方向的落点不同）。
 *
 * 声明为 [Placeable.PlacementScope] 的扩展而不是 [ScrollerAxis] 的成员：
 * `placeRelative` 由 `PlacementScope` 提供，成员函数里拿不到这个 receiver。
 */
private fun ScrollerAxis.placeThumb(
    scope: Placeable.PlacementScope,
    placeable: Placeable,
    offset: Int,
) = with(scope) {
    if (isVertical) placeable.placeRelative(0, offset) else placeable.placeRelative(offset, 0)
}

/**
 * 滚动条共用实现，由 [VerticalScroller] / [HorizontalScroller] /
 * [VerticalOverlayScroller] / [HorizontalOverlayScroller] 转发。
 *
 * 不是对外 API —— 四个具名组件才是；[overlay] 只负责选常规 / 叠加两套 meta 尺寸。
 */
@Composable
private fun Scroller(
    adapter: ScrollerAdapter,
    axis: ScrollerAxis,
    modifier: Modifier,
    enabled: Boolean,
    autoHide: Boolean,
    autoFade: Boolean,
    overlay: Boolean,
    reverseLayout: Boolean,
    onValueChangeFinished: (() -> Unit)?,
    colors: ScrollerColors,
    trackSprite: SokitsuSprite,
    thumbSprite: UiStateSprite,
    interactionSource: MutableInteractionSource?,
) {
    val pixelScale = LocalSokitsuPixelScale.current
    val density = LocalDensity.current

    // 精灵与最小尺寸按常规 / 叠加两套 meta 取用
    val trackMinSize = if (overlay) ScrollerDefaults.overlayTrackMinSize else ScrollerDefaults.trackMinSize
    val thumbMinSize = if (overlay) ScrollerDefaults.overlayThumbMinSize else ScrollerDefaults.thumbMinSize

    // 滑块最短长度（屏幕像素）：取 meta 主轴分量，与轨道交叉轴的 roundToPx() 同源
    val thumbMinMainPx = with(density) { axis.mainSize(thumbMinSize).toPx() }
    val trackCrossPx = with(density) { axis.crossFixedSize(trackMinSize).toPx() }
    val trackMinMainPx = with(density) { axis.mainSize(trackMinSize).toPx() }

    val currentOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)

    // 只读展示 = 无交互：不装手势，也不给"手型"指针。
    // 判定不依赖任何滚动数据，因此可以在决定是否订阅滚动前定下来。
    val interactive = enabled

    val source = interactionSource ?: remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    val scope = rememberCoroutineScope()
    // 容器主轴尺寸（像素）由测量期写回 —— 即 M3 的 containerSize，
    // 滚动行程与滑块长度都要按它算，手势协程也据此定几何
    var containerSize by remember { mutableStateOf(0) }

    // 自动隐藏：无可滚动空间时**完全不组合**（不产生任何节点）。
    // 判定在组合期经 derivedStateOf 收敛：adapter 内部的快照读（如 ScrollState.maxValue、
    // LazyListState.layoutInfo）变化时才重算，Boolean 结果不变则不触发重组；
    // 隐藏期间订阅仍然存活，滚动范围由 0 变正时滚动条会重新出现。
    // 判定数据（maxValue / layoutInfo）由滚动容器在测量期写入，而组合先于测量发生，
    // 故首次出现 / 溢出状态翻转时会晚一帧 —— 这是"空组合"语义下的固有代价。
    if (autoHide) {
        val noScrollSpace by remember(adapter) {
            derivedStateOf { adapter.maxScrollOffset(containerSize) <= 0f }
        }
        if (noScrollSpace) return
    }

    // 滚动位置不在组合里另存一份：与 M3 同法，测量期经几何对象直接读 adapter。
    // `ScrollState` 的 value / maxValue 是 State，在测量期读会自动注册观察，
    // 故列表一动就触发重新测量 —— 无需按帧轮询。
    // key 必须含 adapter：几何对象的 readXxx lambda 捕获它，不重建就会一直读旧的滚动源
    val scroller = remember(adapter, axis, pixelScale, thumbMinMainPx, reverseLayout) {
        ScrollerGeometry(
            axis = axis,
            pixelScale = pixelScale,
            thumbMinMainPx = thumbMinMainPx,
            reverseLayout = reverseLayout,
            readContainerSize = { containerSize },
            readScrollOffset = { adapter.scrollOffset },
            readMaxScrollOffset = { adapter.maxScrollOffset(containerSize) },
            onScroll = { offset -> scope.launch { adapter.scrollTo(containerSize, offset) } },
            coroutineScope = scope,
        )
    }
    // 滑块被拖动 / 悬停时高亮：底色做渐变（与 CMP 的 hoverColor 渐变同手法）
    val highlighted = hovered || scroller.dragging
    val thumbTone by animateColorAsState(
        targetValue = colors.thumbColor(enabled, highlighted),
        animationSpec = TweenSpec(durationMillis = ScrollerDefaults.HighlightDurationMillis),
    )
    // 悬停 / 拖动（选中态）：仅 outline 层覆盖为选中描边色，其余层沿用主题描边色
    val thumbOutline = if (highlighted) colors.selectedOutlineColor else Color.Unspecified
    // 轨道底色：常规取弱化容器色，叠加样式取主色容器（ScrollerTokens.OverlayTrack）
    val trackTone = colors.trackColor(overlay, enabled)

    // 按下音效：仅常规样式发声，叠加样式静音
    val pressSound = if (overlay) null else ScrollerDefaults.LocalPressSound.current

    // 自动降可见度（仅 overlay）：悬浮 / 拖动 / 滚动位置变化任一视为活跃。
    // scrollOffset 读入组合作为 key：滚动期间每次位移都重启计时（活跃即恢复不透明）；
    // 全部退出后经 AutoFadeDelay 延迟置 faded，透明度经动画过渡到 AutoFadeAlpha。
    val fadeModifier = if (autoFade) {
        val scrollOffset = adapter.scrollOffset
        val active = hovered || scroller.dragging
        var faded by remember { mutableStateOf(false) }
        LaunchedEffect(active, scrollOffset) {
            faded = false
            if (!active) {
                delay(ScrollerDefaults.AutoFadeDelay)
                faded = true
            }
        }
        val fadeAlpha by animateFloatAsState(
            targetValue = if (faded) ScrollerDefaults.AutoFadeAlpha else 1f,
            animationSpec = TweenSpec(durationMillis = ScrollerDefaults.FadeDurationMillis),
        )
        modifier.alpha(fadeAlpha)
    } else {
        modifier
    }
    val thumb = thumbSprite[UiState.resolve(enabled, scroller.dragging, hovered, false)]

    // 可交互 = 手型（暗示可拖）；禁用 = 禁止图标
    val icon = if (enabled) {
        ScrollerDefaults.LocalHoverIcon.current
    } else {
        ScrollerDefaults.LocalDisableIcon.current
    }

    val measurePolicy = remember(scroller, axis, trackCrossPx, trackMinMainPx) {
        ScrollerMeasurePolicy(
            axis = axis,
            scroller = scroller,
            trackCrossPx = trackCrossPx,
            trackMinMainPx = trackMinMainPx,
            reportContainerSize = { containerSize = it },
        )
    }

    Layout(
        content = {
            // 唯一子节点：滑块（轨道由本节点自身的 sokitsuSprite 绘制）
            Box(
                Modifier
                    .defaultMinSize(thumbMinSize.width, thumbMinSize.height)
                    .sokitsuSprite(thumb, thumbTone, thumbOutline),
            )
        },
        modifier = fadeModifier
            .pointerHoverIcon(icon)
            .semantics {
                if (!enabled) disabled()
                // 无障碍滚动：入参与 adapter 同为内容像素，直接叠加
                scrollBy { x, y ->
                    val delta = if (axis.isVertical) y else x
                    if (delta == 0f) false
                    else {
                        scroller.scrollByContent(delta)
                        true
                    }
                }
            }
            // 滚轮：单独一个 pointerInput —— 滚轮事件不带按压，混进 awaitEachGesture 会被当噪音丢弃
            .pointerInput(interactive, axis) {
                if (!interactive) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        // Initial 阶段先于列表的滚动逻辑拿到滚轮，避免被外层容器吞掉
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.type != PointerEventType.Scroll) continue
                        // 增量须累加全部 change：首个 change 的 scrollDelta 未必非零
                        var raw = 0f
                        event.changes.forEach { raw += axis.wheelDelta(it.scrollDelta) }
                        if (raw == 0f) continue
                        // delta 已是内容像素（输入桥把 GLFW 一格乘成 54，列表吃到的就是它），
                        // 与 adapter 同量纲，原样叠加即与列表同步。
                        // 符号：MC 滚轮 delta 为正 = 向上滚，内容往起始端走，故取负。
                        scroller.scrollByContent(-raw)
                        event.changes.forEach { it.consume() }
                    }
                }
            }
            // 滑块拖动 + 轨道点击：一个 pointerInput 覆盖整段手势，按落点分流
            .pointerInput(interactive, axis) {
                if (!interactive) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // 按下即响一次（拖动开始或轨道点按），轨道按住的连滚不重复播放
                    pressSound?.let { sound -> mc.soundManager.play(sound) }
                    val main = axis.mainPosition(down.position)
                    if (scroller.isOnThumb(main)) {
                        dragThumb(down.id, main, source, scroller)
                    } else {
                        pressTrack(main, scroller)
                    }
                    // 整段手势结束（拖动或轨道连滚收尾）后回调一次
                    currentOnValueChangeFinished?.invoke()
                }
            }
            .hoverable(source, enabled = interactive)
            .sokitsuSprite(trackSprite, trackTone),
        measurePolicy = measurePolicy,
    )
}

/**
 * 滑块拖动：抓取偏移恒定，位置只由指针主坐标与按下时的基准推出 ——
 * 不读"当前值"，因此跟手且不受外部改值干扰。
 *
 * 结束时按 [DragInteraction] 惯例上报 Stop；协程被取消或回调抛错时补发 Cancel，
 * 避免交互源残留未结束的 Start。
 */
private suspend fun androidx.compose.ui.input.pointer.AwaitPointerEventScope.dragThumb(
    pointerId: androidx.compose.ui.input.pointer.PointerId,
    downMain: Float,
    source: MutableInteractionSource,
    scroller: ScrollerGeometry,
) {
    val interaction = DragInteraction.Start()
    source.tryEmit(interaction)
    scroller.onDragStart()
    try {
        scroller.onDrag(downMain, downMain)
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Main)
            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
            if (!change.pressed) break
            // CMP 无 positionChanged()：直接比对前后坐标判断是否真的移动了
            if (change.position != change.previousPosition) {
                scroller.onDrag(downMain, scroller.axis.mainPosition(change.position))
                change.consume()
            }
        }
        scroller.onDragEnd()
        source.tryEmit(DragInteraction.Stop(interaction))
    } catch (e: Throwable) {
        scroller.onDragEnd()
        source.tryEmit(DragInteraction.Cancel(interaction))
        throw e
    }
}

/** 轨道空白按下：向点击方向连续滚动，直到指针移过滑块或松手。 */
private suspend fun androidx.compose.ui.input.pointer.AwaitPointerEventScope.pressTrack(
    downMain: Float,
    scroller: ScrollerGeometry,
) {
    scroller.onTrackPress(downMain)
    try {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Main)
            val change = event.changes.firstOrNull() ?: break
            if (!change.pressed) break
            scroller.onTrackMovePressed(scroller.axis.mainPosition(change.position))
        }
        scroller.onTrackRelease()
    } catch (e: Throwable) {
        scroller.onTrackRelease()
        throw e
    }
}

/**
 * 滚动条测量：交叉轴**恒取 meta 细轴尺寸**（竖直条宽 / 水平条高，不吃父级约束），
 * 主轴取父级有界约束、无界时回落 meta 主轴下限；滑块按几何式测出后摆到偏移处。
 * 最小尺寸下限由本测量承担（主轴 `coerceAtLeast(meta 主轴下限)`）。
 *
 * 测量期把容器主轴尺寸写回组合（[Scroller] 的几何与手势都要读），
 * 因此该状态变化必须能触发重新测量 —— `LayoutNode.measurePolicy` 的 setter 按 lambda
 * 身份比较，每次重组都会 `invalidateMeasurements()`，故可正常传播。
 */
private class ScrollerMeasurePolicy(
    private val axis: ScrollerAxis,
    private val scroller: ScrollerGeometry,
    private val trackCrossPx: Float,
    private val trackMinMainPx: Float,
    private val reportContainerSize: (Int) -> Unit,
) : MeasurePolicy {

    override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
        val cross = trackCrossPx.roundToInt()
        val mainLimit = if (axis.isVertical) constraints.maxHeight else constraints.maxWidth
        val main = if (mainLimit == Constraints.Infinity) trackMinMainPx.roundToInt() else mainLimit
        reportContainerSize(main)

        val thumbMain = scroller.thumbLength(main.toFloat()).roundToInt().coerceIn(0, main)
        val offset = scroller.thumbOffset(main.toFloat()).roundToInt().coerceIn(0, main)

        val thumbPlaceable = measurables.first().measure(axis.thumbConstraints(thumbMain, cross))
        return layout(
            width = if (axis.isVertical) cross else main.coerceAtLeast(trackMinMainPx.roundToInt()),
            height = if (axis.isVertical) main.coerceAtLeast(trackMinMainPx.roundToInt()) else cross,
        ) {
            axis.placeThumb(this, thumbPlaceable, offset)
        }
    }
}

/**
 * 滚动条交互内核：滑块几何（长度 / 偏移 / 命中）与三种输入的滚动计算。
 *
 * 全程**像素域**，几何式与 compose-multiplatform 的 `SliderAdapter` 一致：
 * - `contentSize = maxScrollOffset + containerSize`；`visiblePart = containerSize / contentSize`；
 * - 滑块长度 = `containerSize × visiblePart`，吸附像素块后夹进 `[滑块最短长, containerSize]`；
 * - `scrollScale = (containerSize − 滑块长) / maxScrollOffset`，滑块偏移 = `scrollScale × scrollOffset`。
 *
 * 拖拽与滚轮都改在**位置域**累计（`position += delta`），再经 [scrollScale] 反算回
 * `scrollOffset` 交给 adapter —— 因此到端点后被忽略的位移不会在反向时立刻生效。
 *
 * 读组合期状态一律走 `readXxx` lambda 而非构造参数快照：本对象在 `remember` 里长存，
 * lambda 每次都能取到最新值。
 */
@Stable
private class ScrollerGeometry(
    val axis: ScrollerAxis,
    private val pixelScale: Int,
    private val thumbMinMainPx: Float,
    private val reverseLayout: Boolean,
    private val readContainerSize: () -> Int,
    private val readScrollOffset: () -> Float,
    private val readMaxScrollOffset: () -> Float,
    private val onScroll: (Float) -> Unit,
    private val coroutineScope: CoroutineScope,
) {

    /** 是否正在拖动滑块：驱动高亮与滑块 pressed 态。 */
    var dragging by mutableStateOf(false)
        private set

    private val containerSize: Float get() = readContainerSize().toFloat()
    private val maxScrollOffset: Float get() = readMaxScrollOffset().coerceAtLeast(0f)

    /** 内容主轴长 = 可滚行程 + 容器长；无行程时等于容器长。 */
    private val contentSize: Float get() = maxScrollOffset + containerSize

    /** 可视占比 = 容器长 / 内容长。 */
    private val visiblePart: Float
        get() {
            val content = contentSize
            return if (content <= 0f) 1f else (containerSize / content).coerceIn(0f, 1f)
        }

    /** 滑块长度（屏幕像素）：容器长 × 可视占比，吸附像素块后夹进允许区间。 */
    fun thumbLength(container: Float = containerSize): Float {
        if (container <= 0f) return 0f
        val min = thumbMinMainPx.coerceAtLeast(0f).coerceAtMost(container)
        return snapToDensity(container * visiblePart, pixelScale).coerceIn(min, container)
    }

    /** 位置域 → 内容域的比例：滑块行程 / 内容行程。 */
    private fun scrollScale(container: Float = containerSize): Float {
        val extraContent = maxScrollOffset
        if (extraContent <= 0f) return 1f
        val extraThumb = container - thumbLength(container)
        return extraThumb / extraContent
    }

    /** 滑块相对轨道起始端的原始偏移（未反转）：`scrollScale × scrollOffset`。 */
    private fun rawThumbOffset(): Float = scrollScale() * readScrollOffset()

    /** 滑块起始偏移（屏幕像素）：反转时贴向末端。 */
    fun thumbOffset(container: Float = containerSize): Float {
        val raw = rawThumbOffset()
        val offset = if (reverseLayout) container - thumbLength(container) - raw else raw
        return snapToDensity(offset, pixelScale).coerceIn(0f, (container - thumbLength(container)).coerceAtLeast(0f))
    }

    /** 指针主坐标是否落在滑块上。 */
    fun isOnThumb(main: Float): Boolean {
        val container = containerSize
        if (container <= 0f) return false
        val length = thumbLength(container)
        val start = thumbOffset(container)
        return main in start..(start + length)
    }

    /**
     * 按内容像素增量滚动 [delta]（正 = 向末端）。
     *
     * 滚轮与无障碍滚动共用：与 adapter 同量纲，直接叠加到当前 `scrollOffset`，
     * 越界由 [ScrollerAdapter.scrollTo] 的实现方收敛。
     */
    fun scrollByContent(delta: Float) {
        if (delta == 0f) return
        val target = readScrollOffset() + if (reverseLayout) -delta else delta
        onScroll(target)
    }

    // ---- 滑块拖动 ----

    /** 按下时的指针主坐标，拖动期间恒定。 */
    private var dragOrigin = 0f

    /** 拖动期间的累计偏移（位置域，起始端为原点、未反转）。 */
    private var dragPosition = 0f

    fun onDragStart() {
        dragOrigin = 0f
        dragPosition = rawThumbOffset()
        dragging = true
    }

    /**
     * 按下与移动的统一入口：[origin] 是按下时的指针主坐标，[main] 是当前指针主坐标。
     *
     * 位移累加在**位置域**（[dragPosition]），到端点后被忽略的部分不会计入，
     * 因此反向拖动时不会出现"先补偿掉被忽略的位移"。累计值再经 [scrollScale] 反算
     * 成 `scrollOffset`，与 compose-multiplatform 的 `SliderAdapter.position` 同法。
     */
    fun onDrag(origin: Float, main: Float) {
        dragOrigin = origin
        val container = containerSize
        if (container <= 0f) return
        val length = thumbLength(container)
        val maxPosition = (container - length).coerceAtLeast(0f)
        if (maxPosition <= 0f) return

        val signed = if (reverseLayout) dragOrigin - main else main - dragOrigin
        val position = (dragPosition + signed).coerceIn(0f, maxPosition)
        val scale = scrollScale(container)
        if (scale <= 0f) return
        // 位置域 → 内容域：reverseLayout 下位置增大对应 scrollOffset 减小
        val raw = if (reverseLayout) container - length - position else position
        onScroll(raw / scale)
    }

    fun onDragEnd() {
        dragging = false
    }

    // ---- 轨道点击 ----

    /** 当前滚动方向（内容域）：1 = 向末端，-1 = 向起始端，0 = 不动。 */
    private var direction = 0

    /** 按住期间的指针主坐标。 */
    private var pressedAt: Float? = null

    /** 按住期间持续滚动的任务。 */
    private var job: Job? = null

    /** 指针在滑块起始端之前 → 向起始端；在末端之后 → 向末端；落在滑块上 → 不动。 */
    private fun directionTowards(main: Float): Int {
        val container = containerSize
        if (container <= 0f) return 0
        val length = thumbLength(container)
        val start = thumbOffset(container)
        val raw = when {
            main < start           -> -1
            main > start + length  -> 1
            else                   -> 0
        }
        // 反转后指针方向与内容方向相反
        return if (reverseLayout) -raw else raw
    }

    /** 向当前按住处滚动一屏（方向与手势方向不一致时不动）。 */
    private fun scrollTowardsPressed() {
        val main = pressedAt ?: return
        if (directionTowards(main) != direction) return
        scrollByContent(direction * containerSize)
    }

    private fun startScrolling() {
        job?.cancel()
        job = coroutineScope.launch {
            scrollTowardsPressed()
            delay(ScrollerDefaults.TrackPressDelayBeforeRepeat)
            while (true) {
                scrollTowardsPressed()
                delay(ScrollerDefaults.TrackPressRepeatInterval)
            }
        }
    }

    /** 首次按下轨道空白：定方向并启动重复滚动。 */
    fun onTrackPress(main: Float) {
        pressedAt = main
        direction = directionTowards(main)
        if (direction != 0) startScrolling()
    }

    /** 按住期间指针移动：只更新位置，方向在下次滚动时重新判定。 */
    fun onTrackMovePressed(main: Float) {
        pressedAt = main
    }

    /** 松手 / 取消：停掉重复滚动。 */
    fun onTrackRelease() {
        job?.cancel()
        job = null
        direction = 0
        pressedAt = null
    }
}
