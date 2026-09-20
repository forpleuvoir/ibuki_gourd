package moe.forpleuvoir.ibukigourd.ui.curve

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.ui.util.rememberPressedKeys
import moe.forpleuvoir.ibukigourd.util.math.easing.CubicBezier
import moe.forpleuvoir.ibukigourd.util.math.easing.Easing
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * 三次贝塞尔缓动曲线的编辑画布：一块 [Canvas] + 两个可拖拽控制点。
 *
 * **不依赖任何纹理素材**：底、单位正方形、网格、对角参考线、曲线、控制点全在同一次绘制里画完
 * （不拆多个叠放节点，避免各层尺寸与坐标对不齐）。
 *
 * 坐标系（[yRange] 决定可见范围）：
 * - x 轴固定是 `0f..1f`，铺满画布可用宽度；
 * - y 轴是 [yRange]，其中 `y = 0f` / `y = 1f` 围出的**单位正方形**用 [BezierCurvePlotTokens.UnitArea]
 *   填充，正方形之外的上下两条带留在 [BezierCurvePlotTokens.Background] 上，
 *   因此"越界 / 过冲"一眼可辨；
 * - 画布四周留 [BezierCurvePlotDefaults.EdgePadding]，控制点贴到边界时仍完整可见。
 *
 * 手势：**命中式拾取**——只有按在控制点的命中半径内才开始拖拽，点空白处不动（避免把曲线误拽变形）；
 * 拖动时 x 夹在 `0f..1f`、y 夹在 [yRange]；拖动过程中支持三个修饰键：
 * - **Alt**：落点吸附到 [snapStep] 网格（按住 Alt 期间才生效）；
 * - **Shift**：锁定角度——控制点只能沿"自身锚点（P1 对 `(0,0)`、P2 对 `(1,1)`）→ 当前位置"这条直线移动，
 *   只改变长度、不改变方向；不允许越过锚点反向（长度夹在 `≥ 0`），并在坐标边界处按比例截断以保持角度；
 *   与 Alt 同时按住时，吸附的是**长度**（曲线坐标下的距离）而不是 x / y 分量。
 * - **Ctrl**：微调——按住期间控制点的位移只有指针位移的 [slowMoveFactor]（缺省 1/10），
 *   便于精确落点；拖拽中途按下 / 松开以当时的位置重新起算，控制点不会跳变。
 *
 * 悬停中的控制点用 [BezierCurvePlotTokens.HandleActive] 高亮。
 *
 * 画布没有内在尺寸：**尺寸必须由调用方通过 [modifier] 给定**（如 `Modifier.size(240.dp)`），
 * 否则会解析为 0 而不可见。
 *
 * @param value 当前曲线（外部状态）
 * @param onValueChange 控制点拖动后的回调；拖拽期间每次移动都会回调一次
 * @param modifier 尺寸与布局修饰符（必须能给出确定的宽高）
 * @param enabled 是否可交互；false 时绘制整体压 [BezierCurvePlotTokens.DisabledOpacity] 且不响应指针
 * @param interactive 是否响应指针（FALSE = 只读预览：**照常满不透明度绘制**，只是拖不动；
 *   与 [enabled] 的区别是 [enabled] 表达"这个配置项不可改"、本参数表达"这里只是展示"）
 * @param yRange y 轴显示区间（须递增）；要严格限制在 `0f..1f` 就传 `0f..1f`
 * @param snapStep 按住 Alt 时的吸附步长
 * @param snapWithAlt 是否启用 Alt 吸附
 * @param lockAngleWithShift 是否启用 Shift 锁角
 * @param slowMoveWithControl 是否启用 Ctrl 微调
 * @param slowMoveFactor 按住 Ctrl 时控制点位移相对指针位移的比例（`0f..1f` 内才有微调意义）
 * @param backgroundColor 画布底色，未指定按 [BezierCurvePlotTokens.Background] 解析
 * @param unitAreaColor 单位正方形填充色，未指定按 [BezierCurvePlotTokens.UnitArea] 解析
 * @param gridColor 网格 / 边框 / 引导线颜色，未指定按 [BezierCurvePlotTokens.Grid] 解析
 * @param curveColor 曲线颜色，未指定按 [BezierCurvePlotTokens.Curve] 解析
 * @param handleColor 控制点颜色（悬停 / 拖拽态自动换 [BezierCurvePlotTokens.HandleActive]）
 */
@Composable
fun BezierCurvePlot(
    value: CubicBezier,
    onValueChange: (CubicBezier) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactive: Boolean = true,
    yRange: ClosedFloatingPointRange<Float> = BezierCurvePlotDefaults.YRange,
    snapStep: Float = BezierCurvePlotDefaults.SnapStep,
    snapWithAlt: Boolean = BezierCurvePlotDefaults.SnapWithAlt,
    lockAngleWithShift: Boolean = BezierCurvePlotDefaults.LockAngleWithShift,
    slowMoveWithControl: Boolean = BezierCurvePlotDefaults.SlowMoveWithControl,
    slowMoveFactor: Float = BezierCurvePlotDefaults.SlowMoveFactor,
    backgroundColor: Color = Color.Unspecified,
    unitAreaColor: Color = Color.Unspecified,
    gridColor: Color = Color.Unspecified,
    curveColor: Color = Color.Unspecified,
    handleColor: Color = Color.Unspecified,
) {
    require(yRange.endInclusive > yRange.start) { "yRange 必须递增，实际为 $yRange" }
    require(snapStep > 0f) { "snapStep 必须为正数，实际为 $snapStep" }
    require(slowMoveFactor > 0f) { "slowMoveFactor 必须为正数，实际为 $slowMoveFactor" }

    // 手势协程要读到最新值与最新回调，避免 pointerInput 因 lambda / 值变化而重启（重启会打断正在进行的拖拽）
    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    // Alt 吸附 / Shift 锁角 / Ctrl 微调所需的按键状态来自仓库自己的输入事件总线（屏幕打开期间同样维护）
    val pressedKeys = rememberPressedKeys()
    val altPressed = Keyboard.LEFT_ALT in pressedKeys.keys || Keyboard.RIGHT_ALT in pressedKeys.keys
    val shiftPressed = Keyboard.LEFT_SHIFT in pressedKeys.keys || Keyboard.RIGHT_SHIFT in pressedKeys.keys
    val controlPressed = Keyboard.LEFT_CONTROL in pressedKeys.keys || Keyboard.RIGHT_CONTROL in pressedKeys.keys
    val currentAlt by rememberUpdatedState(altPressed)
    val currentShift by rememberUpdatedState(shiftPressed)
    val currentControl by rememberUpdatedState(controlPressed)

    val density = LocalDensity.current
    val edgePaddingPx = with(density) { BezierCurvePlotDefaults.EdgePadding.toPx() }
    val handleSizePx = with(density) { BezierCurvePlotDefaults.HandleSize.toPx() }
    val hitRadiusPx = handleSizePx / 2f + with(density) { BezierCurvePlotDefaults.HandleHitPadding.toPx() }
    val curveThicknessPx = with(density) { BezierCurvePlotDefaults.CurveThickness.toPx() }
    val lineThicknessPx = with(density) { BezierCurvePlotDefaults.LineThickness.toPx() }
    val dashLengthPx = with(density) { BezierCurvePlotDefaults.DashLength.toPx() }
    val dashGapPx = with(density) { BezierCurvePlotDefaults.DashGap.toPx() }

    var activeHandle by remember { mutableStateOf<Handle?>(null) }
    var hoveredHandle by remember { mutableStateOf<Handle?>(null) }

    val resolvedBackground = backgroundColor.resolve(BezierCurvePlotTokens.Background)
    val resolvedUnitArea = unitAreaColor.resolve(BezierCurvePlotTokens.UnitArea)
    val resolvedGrid = gridColor.resolve(BezierCurvePlotTokens.Grid)
    val resolvedCurve = curveColor.resolve(BezierCurvePlotTokens.Curve)
    val resolvedHandle = handleColor.resolve(BezierCurvePlotTokens.Handle)
    val resolvedHandleOutline = Color.Unspecified.resolve(BezierCurvePlotTokens.HandleOutline)
    val resolvedHandleActive = Color.Unspecified.resolve(BezierCurvePlotTokens.HandleActive)
    val disabledOpacity = if (enabled) 1f else BezierCurvePlotTokens.DisabledOpacity

    Canvas(
        modifier
            .pointerHoverIcon(
                when {
                    !enabled -> PointerIcon.NotAllowed
                    !interactive -> PointerIcon.Default
                    else -> PointerIcon.Hand
                },
            )
            // 拖拽：按下时拾取控制点，随后跟随指针；x 夹 0..1、y 夹 yRange，Alt 吸附 / Shift 锁角 / Ctrl 微调
            .pointerInput(
                enabled && interactive,
                yRange.start,
                yRange.endInclusive,
                snapStep,
                snapWithAlt,
                lockAngleWithShift,
                slowMoveWithControl,
                slowMoveFactor,
                edgePaddingPx,
                hitRadiusPx,
            ) {
                if (!enabled || !interactive) return@pointerInput
                awaitEachGesture {
                    val space = plotSpace(size.width, size.height, yRange, edgePaddingPx)
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // 未命中控制点：不消费事件，让上层容器（滚动等）继续处理
                    val grabbed = space.pick(down.position, currentValue, hitRadiusPx) ?: return@awaitEachGesture
                    activeHandle = grabbed

                    // 保留按下时的抓取偏移：控制点跟随指针保持原有相对位置，不会在按下瞬间跳到指针正中
                    val center = space.handleCenter(grabbed, currentValue)
                    val grabOffsetX = center.x - down.position.x
                    val grabOffsetY = center.y - down.position.y

                    // Ctrl 微调：位移按比例缩放。基准取"上一次事件"，中途按下 / 松开 Ctrl 时
                    // 以当前位置重新起算，已拖出来的位置不会被缩回去
                    var basePointer = down.position
                    var baseEffective = down.position
                    var lastPointer = down.position
                    var lastEffective = down.position
                    var lastFactor = 1f

                    fun emitAt(position: Offset) {
                        val factor = if (slowMoveWithControl && currentControl) slowMoveFactor else 1f
                        if (factor != lastFactor) {
                            basePointer = lastPointer
                            baseEffective = lastEffective
                            lastFactor = factor
                        }
                        val effective = baseEffective + (position - basePointer) * factor
                        lastPointer = position
                        lastEffective = effective
                        val target = Offset(effective.x + grabOffsetX, effective.y + grabOffsetY)
                        val next = movedBezier(
                            space = space,
                            position = target,
                            handle = grabbed,
                            base = currentValue,
                            snap = snapWithAlt && currentAlt,
                            lockAngle = lockAngleWithShift && currentShift,
                            step = snapStep,
                        )
                        if (next != currentValue) currentOnValueChange(next)
                    }

                    try {
                        emitAt(down.position)
                        down.consume()
                        drag(down.id) { change ->
                            emitAt(change.position)
                            change.consume()
                        }
                    } finally {
                        activeHandle = null
                    }
                }
            }
            // 悬停：按每个指针事件的位置重算与两个控制点的命中关系，命中结果变化时才写状态
            .pointerInput(enabled && interactive, yRange.start, yRange.endInclusive, edgePaddingPx, hitRadiusPx) {
                if (!enabled || !interactive) {
                    hoveredHandle = null
                    return@pointerInput
                }
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Exit) {
                            if (hoveredHandle != null) hoveredHandle = null
                            continue
                        }
                        // 除 Exit 外都按事件里的指针位置重算命中：按住期间平台不派发 Exit
                        // （只有未按下的指针才算悬停），拖出画布后松手（Release）如果不重算，
                        // 高亮就会一直留在那个控制点上
                        val position = event.changes.lastOrNull()?.position ?: continue
                        val space = plotSpace(size.width, size.height, yRange, edgePaddingPx)
                        val hit = space.pick(position, currentValue, hitRadiusPx)
                        if (hit != hoveredHandle) hoveredHandle = hit
                    }
                }
            }
    ) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas

        val space = plotSpace(size.width.toInt(), size.height.toInt(), yRange, edgePaddingPx)
        val easing = value.toEasing()

        fun fade(color: Color): Color = color.copy(alpha = color.alpha * disabledOpacity)

        val gridLine = fade(resolvedGrid.copy(alpha = resolvedGrid.alpha * BezierCurvePlotDefaults.GridOpacity))
        val unitLeft = space.screenX(0f)
        val unitRight = space.screenX(1f)
        val unitTop = space.screenY(1f)
        val unitBottom = space.screenY(0f)
        val unitTopLeft = Offset(unitLeft, unitTop)
        val unitSize = Size(unitRight - unitLeft, unitBottom - unitTop)

        // 曲线与端点可能落在单位正方形之外（过冲），统一裁到画布内，避免画到相邻节点上
        clipRect(0f, 0f, size.width, size.height) {
            drawRect(fade(resolvedBackground))
            drawRect(color = fade(resolvedUnitArea), topLeft = unitTopLeft, size = unitSize)

            for (i in 1 until BezierCurvePlotDefaults.GridDivisions) {
                val fraction = i / BezierCurvePlotDefaults.GridDivisions.toFloat()
                val x = space.screenX(fraction)
                val y = space.screenY(fraction)
                drawLine(gridLine, Offset(x, unitTop), Offset(x, unitBottom), strokeWidth = lineThicknessPx)
                drawLine(gridLine, Offset(unitLeft, y), Offset(unitRight, y), strokeWidth = lineThicknessPx)
            }

            // 对角参考虚线 (0,0) → (1,1)：渲染后端不消费 Paint.pathEffect，虚线按线段手工切分
            drawDashedLine(
                color = gridLine,
                start = Offset(unitLeft, unitBottom),
                end = Offset(unitRight, unitTop),
                dashLength = dashLengthPx,
                gapLength = dashGapPx,
                strokeWidth = lineThicknessPx,
            )

            drawRect(
                color = fade(resolvedGrid),
                topLeft = unitTopLeft,
                size = unitSize,
                style = Stroke(width = lineThicknessPx),
            )

            // 曲线：按屏幕像素逐段采样 x，再由 x 求 y（x 单调，逐段直线即曲线本身）
            val steps = (unitRight - unitLeft).roundToInt().coerceAtLeast(2)
            var previous = curvePoint(space, easing, 0f)
            for (i in 1..steps) {
                val point = curvePoint(space, easing, i / steps.toFloat())
                drawLine(fade(resolvedCurve), previous, point, strokeWidth = curveThicknessPx)
                previous = point
            }

            // 引导线：起点 → P1、终点 → P2
            val lineStart0 = Offset(space.screenX(0f), space.screenY(0f))
            val lineEnd0 = space.handleCenter(Handle.P1, value)
            val lineStart1 = Offset(space.screenX(1f), space.screenY(1f))
            val lineEnd1 = space.handleCenter(Handle.P2, value)
            drawLine(gridLine, lineStart0, lineEnd0, strokeWidth = lineThicknessPx)
            drawLine(gridLine, lineStart1, lineEnd1, strokeWidth = lineThicknessPx)

            // 控制点方块：坐标取整到整像素，像素风下边缘才不糊
            val side = handleSizePx.roundToInt().toFloat().coerceAtLeast(1f)
            val rawCenters = arrayOf(
                space.handleCenter(Handle.P1, value),
                space.handleCenter(Handle.P2, value),
            )
            val tops = Array(2) { i ->
                Offset(
                    (rawCenters[i].x - side / 2f).roundToInt().toFloat(),
                    (rawCenters[i].y - side / 2f).roundToInt().toFloat(),
                )
            }
            for (i in 0..1) {
                val handle = if (i == 0) Handle.P1 else Handle.P2
                val fill = if (handle == activeHandle || handle == hoveredHandle) resolvedHandleActive else resolvedHandle
                val handleBox = Size(side, side)
                drawRect(color = fade(fill), topLeft = tops[i], size = handleBox)
                drawRect(
                    color = fade(resolvedHandleOutline),
                    topLeft = tops[i],
                    size = handleBox,
                    style = Stroke(width = lineThicknessPx),
                )
            }
        }
    }
}

/** 两个可拖拽的控制点。 */
private enum class Handle { P1, P2 }

/** 控制点对应的曲线锚点：P1 从起点 `(0, 0)` 出发，P2 从终点 `(1, 1)` 出发。 */
private fun Handle.anchor(): Offset = when (this) {
    Handle.P1 -> Offset(0f, 0f)
    Handle.P2 -> Offset(1f, 1f)
}

/** 控制点在曲线坐标下的当前位置。 */
private fun Handle.pointOf(value: CubicBezier): Offset = when (this) {
    Handle.P1 -> Offset(value.x1, value.y1)
    Handle.P2 -> Offset(value.x2, value.y2)
}

/** 把控制点移动到曲线坐标 `(x, y)`，其余字段取自 [base]。 */
private fun Handle.movedTo(x: Float, y: Float, base: CubicBezier): CubicBezier = when (this) {
    Handle.P1 -> base.copy(x1 = x, y1 = y)
    Handle.P2 -> base.copy(x2 = x, y2 = y)
}

/**
 * 画布坐标 ↔ 曲线坐标的换算。
 *
 * @param width 画布宽（像素）
 * @param height 画布高（像素）
 * @param yRange y 轴显示区间
 * @param edgePadding 画布四周留白（像素）
 */
private class PlotSpace(
    width: Float,
    height: Float,
    private val yRange: ClosedFloatingPointRange<Float>,
    edgePadding: Float,
) {

    private val ySpan = yRange.endInclusive - yRange.start
    private val left = edgePadding
    private val right = (width - edgePadding).coerceAtLeast(left)
    private val top = edgePadding
    private val bottom = (height - edgePadding).coerceAtLeast(top)
    private val innerWidth = (right - left).coerceAtLeast(1f)
    private val innerHeight = (bottom - top).coerceAtLeast(1f)

    /** y 轴显示区间的下界。 */
    val minY: Float get() = yRange.start

    /** y 轴显示区间的上界。 */
    val maxY: Float get() = yRange.endInclusive

    /** 曲线 x（`0f..1f`）→ 画布 x。 */
    fun screenX(x: Float): Float = left + x * innerWidth

    /** 曲线 y（[yRange] 内）→ 画布 y。 */
    fun screenY(y: Float): Float = top + (yRange.endInclusive - y) / ySpan * innerHeight

    /** 曲线坐标 → 画布坐标。 */
    fun screen(point: Offset): Offset = Offset(screenX(point.x), screenY(point.y))

    /** 画布 x → 曲线 x（夹在 `0f..1f`）。 */
    fun curveX(px: Float): Float = ((px - left) / innerWidth).coerceIn(0f, 1f)

    /** 画布 y → 曲线 y（夹在 [yRange] 内）。 */
    fun curveY(py: Float): Float =
        (yRange.endInclusive - (py - top) / innerHeight * ySpan).coerceIn(yRange.start, yRange.endInclusive)

    /** 控制点在画布上的中心位置。 */
    fun handleCenter(handle: Handle, value: CubicBezier): Offset = when (handle) {
        Handle.P1 -> Offset(screenX(value.x1), screenY(value.y1))
        Handle.P2 -> Offset(screenX(value.x2), screenY(value.y2))
    }

    /** 在 [radius] 内拾取控制点：两个都在范围内时取更近的那个。 */
    fun pick(position: Offset, value: CubicBezier, radius: Float): Handle? {
        val radiusSquared = radius * radius
        val p1 = distanceSquared(handleCenter(Handle.P1, value), position)
        val p2 = distanceSquared(handleCenter(Handle.P2, value), position)
        return when {
            p1 <= radiusSquared && p1 <= p2 -> Handle.P1
            p2 <= radiusSquared -> Handle.P2
            else -> null
        }
    }

    /**
     * 自 [anchor] 沿方向 `(directionX, directionY)` 出发、始终保持在本坐标系内的最大比例系数
     * （1 = 原长度；系数乘在方向向量上）。
     *
     * x 范围由曲线定义固定为 `0f..1f`，y 范围是本空间的 [yRange]。
     * 方向在该轴上分量为 0 时该轴不构成限制。
     */
    fun maxScale(anchor: Offset, directionX: Float, directionY: Float): Float {
        var max = Float.MAX_VALUE
        if (directionX > 1e-6f) max = minOf(max, (1f - anchor.x) / directionX)
        else if (directionX < -1e-6f) max = minOf(max, (0f - anchor.x) / directionX)
        if (directionY > 1e-6f) max = minOf(max, (yRange.endInclusive - anchor.y) / directionY)
        else if (directionY < -1e-6f) max = minOf(max, (yRange.start - anchor.y) / directionY)
        return max.coerceAtLeast(0f)
    }
}

private fun plotSpace(
    width: Int,
    height: Int,
    yRange: ClosedFloatingPointRange<Float>,
    edgePadding: Float,
): PlotSpace = PlotSpace(width.toFloat(), height.toFloat(), yRange, edgePadding)

/**
 * 把指针位置换算成"被拖拽控制点的新坐标"。
 *
 * [lockAngle] 开启时（Shift）：控制点只能沿"自身锚点 → 当前位置"这条直线移动，方向不变、只改长度：
 * - 指针先投影到该直线，投影在**画布坐标**下进行，保证拖动过程中点始终贴着指针；
 * - 长度以 `≥ 0` 夹取，不允许越过锚点反向；
 * - 长度再按 [PlotSpace.maxScale] 截断，于是在坐标边界处仍然保持角度（而不是先落到界外再逐轴夹回来）；
 * - 与 [snap] 同时开启时吸附的是**长度**（曲线坐标下的距离）而不是 x / y 分量。
 *
 * 锚点与当前点重合（方向未定义）时退回自由拖动。
 * [lockAngle] 关闭时按 [snap] 决定是否把 x / y 各自吸附到 [step] 网格，最后各轴夹进取值区间。
 *
 * @param space 当前画布的坐标换算
 * @param position 指针位置（画布坐标，已含按下时的抓取偏移）
 * @param handle 正在拖拽的控制点
 * @param base 拖拽开始时的曲线值
 * @param snap 是否吸附（Alt）
 * @param lockAngle 是否锁角（Shift）
 * @param step 吸附步长
 */
private fun movedBezier(
    space: PlotSpace,
    position: Offset,
    handle: Handle,
    base: CubicBezier,
    snap: Boolean,
    lockAngle: Boolean,
    step: Float,
): CubicBezier {
    if (lockAngle) {
        val anchor = handle.anchor()
        val current = handle.pointOf(base)
        val directionX = current.x - anchor.x
        val directionY = current.y - anchor.y
        val curveLength = sqrt(directionX * directionX + directionY * directionY)

        val screenAnchor = space.screen(anchor)
        val screenCurrent = space.screen(current)
        val screenDirectionX = screenCurrent.x - screenAnchor.x
        val screenDirectionY = screenCurrent.y - screenAnchor.y
        val screenLength = sqrt(screenDirectionX * screenDirectionX + screenDirectionY * screenDirectionY)

        if (curveLength > 1e-6f && screenLength > 1e-6f) {
            // 投影参数 s：1 = 保持当前长度；dot(v, d) / dot(d, d)
            val offsetX = position.x - screenAnchor.x
            val offsetY = position.y - screenAnchor.y
            var scale = ((offsetX * screenDirectionX + offsetY * screenDirectionY) / screenLength / screenLength)
                .coerceAtLeast(0f)
            if (snap) scale = snapTo(scale * curveLength, step) / curveLength
            scale = scale.coerceAtMost(space.maxScale(anchor, directionX, directionY))
            return handle.movedTo(anchor.x + directionX * scale, anchor.y + directionY * scale, base)
        }
    }

    val rawX = space.curveX(position.x)
    val rawY = space.curveY(position.y)
    val x = (if (snap) snapTo(rawX, step) else rawX).coerceIn(0f, 1f)
    val y = if (snap) snapTo(rawY, step) else rawY
    return handle.movedTo(x, y.coerceIn(space.minY, space.maxY), base)
}

/** 网格吸附：取最接近的 [step] 整数倍。 */
private fun snapTo(value: Float, step: Float): Float = (value / step).roundToInt() * step

/** 曲线上 `x = t` 处的画布坐标。坐标不取整：逐像素 x 采样下取整会把浅斜段烘成 1px 台阶（dy<0.5 连续归零再跳变），AA 无法挽回。 */
private fun curvePoint(space: PlotSpace, easing: Easing, t: Float): Offset = Offset(
    space.screenX(t),
    space.screenY(easing.easeIn(t)),
)

/** 手绘虚线：渲染后端不消费 `Paint.pathEffect`，这里按 `dashLength` / `gapLength` 交替切分。 */
private fun DrawScope.drawDashedLine(
    color: Color,
    start: Offset,
    end: Offset,
    dashLength: Float,
    gapLength: Float,
    strokeWidth: Float,
) {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val total = sqrt(dx * dx + dy * dy)
    if (total <= 0f || dashLength <= 0f) return

    val ux = dx / total
    val uy = dy / total
    var consumed = 0f
    while (consumed < total) {
        val segmentEnd = (consumed + dashLength).coerceAtMost(total)
        drawLine(
            color = color,
            start = Offset(start.x + ux * consumed, start.y + uy * consumed),
            end = Offset(start.x + ux * segmentEnd, start.y + uy * segmentEnd),
            strokeWidth = strokeWidth,
        )
        consumed = segmentEnd + gapLength
    }
}

private fun distanceSquared(a: Offset, b: Offset): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return dx * dx + dy * dy
}
