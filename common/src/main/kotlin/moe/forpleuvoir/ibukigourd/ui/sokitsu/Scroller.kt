package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.snapToDensity
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.uiSprite
import moe.forpleuvoir.ibukigourd.util.codec.dpSize
import moe.forpleuvoir.ibukigourd.util.codec.ibukigourdIdentifier
import moe.forpleuvoir.ibukigourd.util.contrasting
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.resources.Identifier
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
 * [ScrollerAdapter.maxScrollOffset] 忽略 `containerSize` 是因为 `ScrollState.maxValue` 已由列表在测量期
 * 写为 `contentSize − viewportSize`，与容器尺寸同源。
 */
fun ScrollerAdapter(scrollState: ScrollState): ScrollerAdapter = ScrollStateScrollerAdapter(scrollState)

private class ScrollStateScrollerAdapter(
    private val scrollState: ScrollState,
) : ScrollerAdapter {

    override val scrollOffset: Float get() = scrollState.value.toFloat()

    override suspend fun scrollTo(containerSize: Int, scrollOffset: Float) {
        scrollState.scrollTo(scrollOffset.roundToInt())
    }

    override fun maxScrollOffset(containerSize: Int): Float = scrollState.maxValue.toFloat()
}

/** 按 [ScrollState] 记住一个 [ScrollerAdapter]，实例随 state 变化重建。 */
@Composable
fun rememberScrollerAdapter(scrollState: ScrollState): ScrollerAdapter = remember(scrollState) {
    ScrollerAdapter(scrollState)
}

/**
 * 竖直滚动条：**轨道 + 滑块**两层精灵叠放，滑块沿 y 轴滑动。
 *
 * 结构与 compose-multiplatform 的 `VerticalScrollbar` 同构：轨道是节点自身的背景精灵，
 * 唯一子节点是滑块 `Box`（尺寸与位置由 [MeasurePolicy] 给定 —— 子节点不声明尺寸，
 * 不给死约束就测成 0×0）。方向差异全部收敛进 [ScrollerAxis]，与 [HorizontalScroller]
 * 共用同一份几何、手势与测量实现。
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
    reverseLayout = reverseLayout,
    onValueChangeFinished = onValueChangeFinished,
    colors = colors,
    trackSprite = trackSprite,
    thumbSprite = thumbSprite,
    interactionSource = interactionSource,
)

/**
 * 水平滚动条：**轨道 + 滑块**两层精灵叠放，滑块沿 x 轴滑动。
 *
 * 与 [VerticalScroller] 共用同一实现（[Scroller] + [ScrollerAxis.Horizontal]），仅轴向不同：
 * 厚度（高）恒取 meta 细轴尺寸，宽度铺满；滚轮同样读 `scrollDelta.y`
 * —— 鼠标滚轮只有垂直分量，横向滚动条只是把滚动投影到 x 轴，外观才是横向。
 *
 * @param adapter 与滚动组件通信的桥；见 [ScrollerAdapter] / [rememberScrollerAdapter]
 * @param modifier 修饰；宽度有界时铺满，无界时回落 meta 主轴下限
 * @param enabled 是否可交互
 * @param reverseLayout 反转方向
 * @param onValueChangeFinished 手势结束回调
 * @param colors 配色集，默认 [ScrollerDefaults.colors]
 * @param trackSprite 轨道精灵
 * @param thumbSprite 滑块精灵（四态）
 * @param interactionSource 交互源
 */
@Composable
fun HorizontalScroller(
    adapter: ScrollerAdapter,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
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
 * 滚动条共用实现，由 [VerticalScroller] / [HorizontalScroller] 转发。
 *
 * 不是对外 API —— 两个具名组件才是。
 */
@Composable
private fun Scroller(
    adapter: ScrollerAdapter,
    axis: ScrollerAxis,
    modifier: Modifier,
    enabled: Boolean,
    reverseLayout: Boolean,
    onValueChangeFinished: (() -> Unit)?,
    colors: ScrollerColors,
    trackSprite: SokitsuSprite,
    thumbSprite: UiStateSprite,
    interactionSource: MutableInteractionSource?,
) {
    val pixelScale = LocalSokitsuPixelScale.current
    val density = LocalDensity.current

    val trackMinSize = ScrollerDefaults.trackMinSize
    val thumbMinSize = ScrollerDefaults.thumbMinSize

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
    val trackTone = colors.trackColor(enabled)
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
        modifier = modifier
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
            .defaultMinSize(trackMinSize.width, trackMinSize.height)
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
            width = if (axis.isVertical) cross else main,
            height = if (axis.isVertical) main else cross,
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

/**
 * 滚动条的主题接入声明：token 映射（"什么颜色"）+ meta（"多大、用哪张图"）。
 *
 * 竖直与水平两个组件**共用同一份 meta** —— 细轴分量即滚动条厚度，两个方向各自取用。
 */
object ScrollerTokens {

    /** 轨道底色：弱化容器色，比面板深一档但不抢主体。 */
    val Track = ColorSchemeToken.SurfaceVariant

    /** 滑块底色：主色，滚动位置是最该被一眼看到的信息。 */
    val Thumb = ColorSchemeToken.Primary

    /**
     * 禁用态轨道 / 滑块的色源。
     *
     * 两者都取**不透明**的语义槽位色，不叠加 alpha —— 素材的 `base` 层走 Multiply
     * 合成（顶点色 × 纹理），压 alpha 会让整层在深色底上消失，只剩 outline 层可见。
     */
    val DisabledTrack = ColorSchemeToken.SurfaceVariant
    val DisabledThumb = ColorSchemeToken.Surface
}

/**
 * 滚动条的 meta：**尺寸单位均为 dp**（逻辑像素），精灵为图集 id。
 *
 * ```jsonc
 * ui_meta: { scroller: {
 *   track_min_size: [28, 28],
 *   track_sprite: "ui/scroller/track",
 *   thumb_min_size: [28, 28],
 *   thumb_sprite: {
 *     normal: "ui/scroller/thumb/normal",
 *     pressed: "ui/scroller/thumb/pressed",
 *     focused: "ui/scroller/thumb/focused",
 *     disabled: "ui/scroller/thumb/disabled"
 *   }
 * } }
 * ```
 *
 * 两个尺寸都是**尺寸下限**，且**两个方向共用一份**（meta 不区分横竖）：
 * - [trackMinSize] 的细轴分量 = 滚动条厚度，是**恒定值**（竖直条取宽、水平条取高），
 *   不吃父级约束；主轴分量只在主轴无界时兜底。
 * - [thumbMinSize] 的主轴分量 = 滑块最短长度（内容极多时滑块仍可见）；交叉轴分量即厚度。
 */
data class ScrollerMeta(
    /** 轨道最小尺寸：细轴分量恒为滚动条厚度。 */
    val trackMinSize: DpSize,
    /** 轨道精灵图集 id（单张，无状态。交互状态差异由染色承担，不加描边）。 */
    val trackSprite: Identifier,
    /** 滑块最小尺寸：主轴分量 = 滑块最短长度。 */
    val thumbMinSize: DpSize,
    /** 滑块四态精灵图集 id。 */
    val thumbSprite: UiStateIdentifier,
) {

    companion object : Codec<ScrollerMeta> {

        val default = ScrollerMeta(
            trackMinSize = DpSize(28.dp, 28.dp),
            trackSprite = identifier("ui/scroller/track"),
            thumbMinSize = DpSize(28.dp, 28.dp),
            thumbSprite = UiStateIdentifier(
                normal = identifier("ui/scroller/thumb/normal"),
                pressed = identifier("ui/scroller/thumb/pressed"),
                focused = identifier("ui/scroller/thumb/focused"),
                disabled = identifier("ui/scroller/thumb/disabled"),
            ),
        )

        private val codec = Codec.create<ScrollerMeta>()
            .field(ScrollerMeta::trackMinSize).default(default.trackMinSize)
            .codec(Codec.dpSize(1.dp..1024.dp, 1.dp..1024.dp))
            .field(ScrollerMeta::trackSprite).default(default.trackSprite)
            .codec(Codec.ibukigourdIdentifier)
            .field(ScrollerMeta::thumbMinSize).default(default.thumbMinSize)
            .codec(Codec.dpSize(1.dp..1024.dp, 1.dp..4096.dp))
            .field(ScrollerMeta::thumbSprite).default(default.thumbSprite)
            .codec(UiStateIdentifier)
            .build(::ScrollerMeta)

        override fun serialization(target: ScrollerMeta): SerializeElement = codec.serialization(target)

        override fun deserialization(data: SerializeElement): Result<ScrollerMeta> =
            codec.deserialization(data)
    }
}

/**
 * 滚动条的主题桥接：组合内不直接读取 [SokitsuThemeMeta.scroller]。
 */
object ScrollerDefaults {

    /** 当前主题的 scroller meta。 */
    inline val meta get() = SokitsuThemeMeta.scroller

    /** 轨道最小尺寸：内联转发 [ScrollerMeta.trackMinSize]。细轴分量即厚度。 */
    inline val trackMinSize: DpSize get() = meta.trackMinSize

    /** 滑块最小尺寸：内联转发 [ScrollerMeta.thumbMinSize]。主轴分量即最短长度。 */
    inline val thumbMinSize: DpSize get() = meta.thumbMinSize

    /** 轨道精灵：经 UI 图集解析。 */
    fun trackSprite(): SokitsuSprite = SokitsuThemeMeta.uiSprite(meta.trackSprite)

    /** 滑块四态精灵：经 UI 图集解析。 */
    fun thumbSprite(): UiStateSprite = meta.thumbSprite.toSprite()

    /** 按住拖动时的鼠标指针形状。 */
    val LocalHoverIcon = compositionLocalOf { PointerIcon.Hand }

    /** 禁用时的鼠标指针形状。 */
    val LocalDisableIcon = compositionLocalOf { PointerIcon.NotAllowed }

    /** 按住轨道空白时，第一次与第二次滚动之间的停顿（毫秒）。 */
    const val TrackPressDelayBeforeRepeat = 300L

    /** 按住轨道空白时，后续两次滚动之间的间隔（毫秒）。 */
    const val TrackPressRepeatInterval = 100L

    /** 悬停 / 拖动高亮的渐变时长（毫秒）。 */
    const val HighlightDurationMillis = 300

    /**
     * 默认滚动条配色：轨道 → 弱化容器色，滑块 → 主色；两个禁用色取不透明语义槽位色。
     *
     * 参数默认 [Color.Unspecified] 语义是"按 [ScrollerTokens] 映射表结合当前主题解析"；
     * 回退顺序：`调用点传参` > [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuColor]
     * 作用域 > [ScrollerTokens] > [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorScheme]。
     *
     * @param trackColor 轨道底色 → [ScrollerTokens.Track]
     * @param thumbColor 滑块底色 → [ScrollerTokens.Thumb]
     * @param disabledTrackColor 禁用轨道底色 → [ScrollerTokens.DisabledTrack]（不透明）
     * @param disabledThumbColor 禁用滑块底色 → [ScrollerTokens.DisabledThumb]（不透明）
     * @param highlightThumbColor 悬停 / 拖动中的滑块底色，默认与 [thumbColor] 同源
     * @param selectedOutlineColor 悬停 / 拖动时滑块描边色，默认取 [thumbColor] 底色的对比色
     */
    @Composable
    fun colors(
        trackColor: Color = Color.Unspecified,
        thumbColor: Color = Color.Unspecified,
        disabledTrackColor: Color = Color.Unspecified,
        disabledThumbColor: Color = Color.Unspecified,
        highlightThumbColor: Color = Color.Unspecified,
        selectedOutlineColor: Color = Color.Unspecified,
    ): ScrollerColors {
        val resolvedThumb = thumbColor.resolve(ScrollerTokens.Thumb)
        return ScrollerColors(
            trackColor = trackColor.resolve(ScrollerTokens.Track),
            thumbColor = resolvedThumb,
            disabledTrackColor = disabledTrackColor.resolve(ScrollerTokens.DisabledTrack),
            disabledThumbColor = disabledThumbColor.resolve(ScrollerTokens.DisabledThumb),
            highlightThumbColor = highlightThumbColor.takeIf { it != Color.Unspecified } ?: resolvedThumb,
            selectedOutlineColor = selectedOutlineColor.takeOrElse { resolvedThumb.contrasting() },
        )
    }
}

/**
 * 滚动条配色集：五个底色槽位（轨道 / 滑块的启用、禁用、高亮）+ 选中描边色。
 *
 * 字段**全部已解析**，因此是普通 data class，`copy(...)` 即精准覆盖；
 * "槽位映射到主题哪里"由 [ScrollerDefaults.colors] 承担。
 * "状态 → 取值"的映射由 [trackColor] / [thumbColor] 两个方法承担，
 * 调用点不自行按启用状态做 if 判断。
 *
 * @param highlightThumbColor 悬停 / 拖动中的滑块底色
 * @param selectedOutlineColor 悬停 / 拖动时滑块 outline 层的覆盖色（选中描边）
 */
@Immutable
data class ScrollerColors(
    val trackColor: Color,
    val thumbColor: Color,
    val disabledTrackColor: Color,
    val disabledThumbColor: Color,
    val highlightThumbColor: Color,
    val selectedOutlineColor: Color,
) {

    /** 轨道底色（按启用状态二选一）。 */
    internal fun trackColor(enabled: Boolean): Color =
        if (enabled) trackColor else disabledTrackColor

    /** 滑块底色（禁用 > 高亮 > 常态）。 */
    internal fun thumbColor(enabled: Boolean, highlighted: Boolean): Color = when {
        !enabled    -> disabledThumbColor
        highlighted -> highlightThumbColor
        else        -> thumbColor
    }
}

/** 主题 meta 的滚动条段：缺失 / 解码失败回落 [ScrollerMeta] 内置默认。 */
val SokitsuThemeMeta.scroller: ScrollerMeta
    get() = decodeComponent("scroller", ScrollerMeta, ScrollerMeta.default)
