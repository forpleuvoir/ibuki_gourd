package moe.forpleuvoir.ibukigourd.test.sokitsu

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.curve.BezierCurveEditor
import moe.forpleuvoir.ibukigourd.ui.curve.BezierCurveEditorDefaults
import moe.forpleuvoir.ibukigourd.ui.curve.BezierCurvePlot
import moe.forpleuvoir.ibukigourd.ui.curve.BezierCurvePlotDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FloatField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Slider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Switch
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.ValueStep
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.util.math.easing.CubicBezier
import kotlin.math.floor

/**
 * 曲线编辑器测试屏。
 *
 * 验证点：
 * 1. 画布拖拽：x 夹在 `0f..1f`、y 夹在 y 轴区间内；命中式拾取（点空白不动）；
 *    按住 **Alt** 吸附 1/20，按住 **Shift** 锁角（控制点只沿自身锚点与当前位置的直线移动，不允许反向）；
 *    两者同时按住时锁角 + 长度吸附；悬停 / 拖拽中的控制点换次色；
 * 2. 数值框与画布共用同一份状态（双向同步），x 框限 `0f..1f`（曲线定义域，越界报错并收敛），
 *    y 框不限（可越过 y 轴区间，表达回弹 / 过冲）；
 * 3. 预设行点击后整条曲线被替换；
 * 4. 动画预览：滑块给的是**线性**时间 t，方块位置给的是 `ease(t)`，两者对照即可看出曲线手感；
 * 5. 裸画布：越界区间（`-0.4f..1.4f`）能画出过冲、严格模式（`0f..1f`）与禁用态的表现。
 */
fun BezierCurvePlotTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 复现"引导线被画歪"的那组坐标（原样取自触发帧的读数，全精度）
            val reproValue = CubicBezier(0.6129485f, 0.35067517f, 0.2f, 1.0f)
            var plot by remember { mutableStateOf(reproValue) }

            // ── 诊断：控制点在设定的 x / y 矩形内**逐行扫描** + 全精度读数（配录屏回放定位渲染错位）──
            var autoDrift by remember { mutableStateOf(false) }
            var driftP1 by remember { mutableStateOf(true) }
            var driftP2 by remember { mutableStateOf(false) }
            var scanAlongY by remember { mutableStateOf(false) }
            var xFrom by remember { mutableStateOf(0f) }
            var xTo by remember { mutableStateOf(1f) }
            var yFrom by remember { mutableStateOf(0f) }
            var yTo by remember { mutableStateOf(1f) }
            var rowStep by remember { mutableStateOf(0.02f) }
            var rowSeconds by remember { mutableStateOf(10f) }
            var scanRow by remember { mutableStateOf(0) }
            // 进给轴 = 每扫完一行抬一格的轴（X 扫描时是 y，Y 扫描时是 x），行数按它算（含两端）
            val feedFrom = if (scanAlongY) xFrom else yFrom
            val feedTo = if (scanAlongY) xTo else yTo
            val scanRows = (((feedTo - feedFrom) / rowStep.coerceAtLeast(1e-4f)).toInt().coerceAtLeast(0) + 1)
                .coerceAtMost(2000)
            if (autoDrift) {
                // 逐行扫描（CRT 式）：行内沿扫描轴从一端扫到另一端，下一行反向；行末进给轴抬一个 rowStep。
                // 每行耗时固定 rowSeconds 秒 —— 范围收得越窄，同一段扫得越慢；配合录屏可逐帧定位。
                LaunchedEffect(
                    driftP1, driftP2, scanAlongY, xFrom, xTo, yFrom, yTo, rowStep, rowSeconds, scanRows,
                ) {
                    val start = withFrameNanos { it }
                    while (true) {
                        val progress = (withFrameNanos { it } - start) / 1_000_000_000f /
                                rowSeconds.coerceAtLeast(0.1f)
                        val row = progress.toInt() % scanRows
                        val frac = progress - floor(progress)
                        val sweep = if (row % 2 == 0) frac else 1f - frac
                        val swept = if (scanAlongY) yFrom + (yTo - yFrom) * sweep
                        else xFrom + (xTo - xFrom) * sweep
                        val fed = if (scanRows <= 1) feedFrom
                        else feedFrom + (feedTo - feedFrom) * (row.toFloat() / (scanRows - 1))
                        val x = if (scanAlongY) fed else swept
                        val y = if (scanAlongY) swept else fed
                        scanRow = row
                        plot = plot.copy(
                            x1 = if (driftP1) x else plot.x1,
                            y1 = if (driftP1) y else plot.y1,
                            x2 = if (driftP2) x else plot.x2,
                            y2 = if (driftP2) y else plot.y2,
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("诊断：逐行扫描")
                Switch(checked = autoDrift, onCheckedChange = { autoDrift = it })
                Text("扫 P1")
                Switch(checked = driftP1, onCheckedChange = { driftP1 = it })
                Text("扫 P2")
                Switch(checked = driftP2, onCheckedChange = { driftP2 = it })
                Text("沿 Y 扫描（否则沿 X）")
                Switch(checked = scanAlongY, onCheckedChange = { scanAlongY = it })
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("x")
                FloatField(
                    value = xFrom, onValueChange = { xFrom = it },
                    valueRange = 0f..1f, valueStep = ValueStep(0.01f),
                    modifier = Modifier.width(110.dp),
                )
                Text("→")
                FloatField(
                    value = xTo, onValueChange = { xTo = it },
                    valueRange = 0f..1f, valueStep = ValueStep(0.01f),
                    modifier = Modifier.width(110.dp),
                )
                Text("y")
                FloatField(
                    value = yFrom, onValueChange = { yFrom = it },
                    valueStep = ValueStep(0.01f),
                    modifier = Modifier.width(110.dp),
                )
                Text("→")
                FloatField(
                    value = yTo, onValueChange = { yTo = it },
                    valueStep = ValueStep(0.01f),
                    modifier = Modifier.width(110.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("每行抬（进给轴）")
                FloatField(
                    value = rowStep, onValueChange = { rowStep = it },
                    valueRange = 1e-4f..1f, valueStep = ValueStep(0.001f),
                    modifier = Modifier.width(110.dp),
                )
                Text("行耗时(s)")
                FloatField(
                    value = rowSeconds, onValueChange = { rowSeconds = it },
                    valueRange = 0.1f..120f, valueStep = ValueStep(0.5f),
                    modifier = Modifier.width(110.dp),
                )
            }
            Text(driftReadout(plot, scanRow, scanRows, scanAlongY))

            Text("拖动控制点 · 数值框 · 预设（Alt 吸附 1/20 · Shift 锁定角度 · Ctrl 微调 1/10）")
            // 画布取默认尺寸的两倍：控制点更好抓，测吸附 / 锁角时手感更接近真实曲线
            val editorSize = BezierCurveEditorDefaults.PlotSize * 2
            BezierCurveEditor(
                value = plot,
                onValueChange = { plot = it },
                plotSize = editorSize,
            )

            BezierAnimationPreview(bezier = plot)

            // 三个小画布的状态提到外层：下面要给每个画布都挂读数
            var strict by remember { mutableStateOf(CubicBezier.EaseOut) }
            var wide by remember { mutableStateOf(reproValue) }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("严格 0..1")
                    BezierCurvePlot(
                        value = strict,
                        onValueChange = { strict = it },
                        xRange = 0f..1f,
                        yRange = 0f..1f,
                        modifier = Modifier.size(160.dp),
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("过冲 -0.4..1.4")
                    BezierCurvePlot(
                        // 漂移/扫描开着时跟着上面那份值走：同一组坐标在两种 y 轴比例下同时呈现，覆盖更多像素斜率
                        value = if (autoDrift) plot else wide,
                        onValueChange = { wide = it },
                        xRange = -0.4f..1.4f,
                        yRange = -0.4f..1.4f,
                        modifier = Modifier.size(160.dp),
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("禁用态")
                    BezierCurvePlot(
                        value = CubicBezier.EaseInOut,
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier.size(160.dp),
                    )
                }
            }

            // 每块画布的读数（单行、不换行，录屏里一帧就能读到全部坐标与对应像素位置）
            Text(
                plotReadout(
                    "编辑器 $editorSize",
                    plot,
                    editorSize,
                    BezierCurvePlotDefaults.XRange,
                    BezierCurvePlotDefaults.YRange,
                )
            )
            Text(plotReadout("严格 0..1 160dp", strict, 160.dp, 0f..1f, 0f..1f))
            Text(plotReadout("过冲 -0.4..1.4 160dp", if (autoDrift) plot else wide, 160.dp, -0.4f..1.4f, -0.4f..1.4f))
            Text(
                plotReadout(
                    "禁用态 160dp",
                    CubicBezier.EaseInOut,
                    160.dp,
                    BezierCurvePlotDefaults.XRange,
                    BezierCurvePlotDefaults.YRange,
                )
            )
        }
    }
}

/**
 * 单行读数：画布标识 + 控制点**全精度**值（`Float.toString()`，不截断）+ 该画布内的像素坐标。
 *
 * 像素换算与 `BezierCurvePlot` 的 `PlotSpace` 一致（边距 [BezierCurvePlotDefaults.EdgePadding]、
 * 画布尺寸 = [plotSize] 经密度换算、x / y 轴区间 = [xRange] / [yRange]），所以录屏里"画面位置 vs 这行 px"
 * 对不上就说明是渲染侧，对得上就说明传进去的值本身如此。
 */
@Composable
private fun plotReadout(
    label: String,
    value: CubicBezier,
    plotSize: Dp,
    xRange: ClosedFloatingPointRange<Float>,
    yRange: ClosedFloatingPointRange<Float>,
): String {
    val density = LocalDensity.current
    val canvas = with(density) { plotSize.toPx() }
    val edge = with(density) { BezierCurvePlotDefaults.EdgePadding.toPx() }
    val inner = (canvas - 2f * edge).coerceAtLeast(1f)
    fun sx(x: Float): Float = edge + (x - xRange.start) / (xRange.endInclusive - xRange.start) * inner
    fun sy(y: Float): Float = edge + (yRange.endInclusive - y) / (yRange.endInclusive - yRange.start) * inner
    return "$label  x1=${value.x1} y1=${value.y1} x2=${value.x2} y2=${value.y2}  " +
            "px P1=(${sx(value.x1)}, ${sy(value.y1)}) P2=(${sx(value.x2)}, ${sy(value.y2)})"
}

/**
 * 诊断读数：**当前扫描行** + 控制点**全精度**值（`Float.toString()`，不做格式化截断）+ 画布内像素坐标。
 *
 * 像素换算与 `BezierCurvePlot` 的 `PlotSpace` 一致（边距取 [BezierCurvePlotDefaults.EdgePadding]、
 * x / y 轴区间取编辑器默认的 [BezierCurvePlotDefaults.XRange] / [BezierCurvePlotDefaults.YRange]、
 * 画布尺寸取编辑器的 `plotSize` ×2），
 * 这样录屏回放里能直接读出"传进去的值"和"应该画在哪"，不必再去对日志时间戳。
 */
@Composable
private fun driftReadout(value: CubicBezier, row: Int, rows: Int, alongY: Boolean): String {
    val density = LocalDensity.current
    val canvas = with(density) { (BezierCurveEditorDefaults.PlotSize * 2).toPx() }
    val edge = with(density) { BezierCurvePlotDefaults.EdgePadding.toPx() }
    val xRange = BezierCurvePlotDefaults.XRange
    val range = BezierCurvePlotDefaults.YRange
    val inner = (canvas - 2f * edge).coerceAtLeast(1f)
    fun sx(x: Float): Float = edge + (x - xRange.start) / (xRange.endInclusive - xRange.start) * inner
    fun sy(y: Float): Float = edge + (range.endInclusive - y) / (range.endInclusive - range.start) * inner
    return "扫描 轴=${if (alongY) "Y" else "X"}  第 ${row + 1}/$rows 行\n" +
            "值 x1=${value.x1}  y1=${value.y1}  x2=${value.x2}  y2=${value.y2}\n" +
            "画布内 px（canvas=${canvas}）  P1=(${sx(value.x1)}, ${sy(value.y1)})  P2=(${sx(value.x2)}, ${sy(value.y2)})"
}

/**
 * 动画预览：滑块 + 轨道上的运动方块。
 *
 * - 滑块给的是**线性时间** `t`（0 → 1）；拖动滑块会暂停播放；
 * - 方块位置给的是 `ease(t)`，运动方块与滑块进度条的差值就是该曲线的"快慢分布"；
 * - 「播放」用 1.2s 一轮的线性时间驱动同一个 `t`，用来判断真实节奏。
 *
 * @param bezier 当前曲线；控制点一变，预览与滑块立即跟着变
 */
@Composable
private fun BezierAnimationPreview(bezier: CubicBezier, modifier: Modifier = Modifier) {
    val easing = remember(bezier) { bezier.toEasing() }

    var manualProgress by remember { mutableStateOf(0.35f) }
    var playing by remember { mutableStateOf(false) }

    val transition = rememberInfiniteTransition()
    val loopProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val progress = if (playing) loopProgress else manualProgress
    val eased = easing.easeIn(progress)
    val scheme = LocalColorScheme.current

    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("滑块 = 线性时间 t · 方块 = ease(t)")
        Slider(
            value = progress,
            onValueChange = {
                manualProgress = it
                playing = false
            },
            modifier = Modifier.width(PreviewTrackWidth),
            label = { Text("t %.2f → %.2f".format(progress, eased)) },
        )
        // 轨道宽度留出方块自身宽度，方块贴到两端时不会溢出轨道
        Box(
            Modifier
                .width(PreviewTrackWidth)
                .height(PreviewMarkerSize)
                .background(scheme.surfaceVariant),
        ) {
            Box(
                Modifier
                    .offset(x = (PreviewTrackWidth - PreviewMarkerSize) * eased)
                    .size(PreviewMarkerSize)
                    .background(scheme.primary),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("缓动值 %.3f".format(eased))
            Button({
                playing = !playing
            }) {
                Text(if (playing) "暂停" else "播放")
            }
        }
    }
}

private val PreviewTrackWidth = 280.dp

private val PreviewMarkerSize = 20.dp
