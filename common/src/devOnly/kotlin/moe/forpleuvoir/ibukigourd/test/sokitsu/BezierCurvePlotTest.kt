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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.curve.BezierCurveEditor
import moe.forpleuvoir.ibukigourd.ui.curve.BezierCurvePlot
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Slider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.util.math.easing.CubicBezier

/**
 * 曲线编辑器测试屏。
 *
 * 验证点：
 * 1. 画布拖拽：x 夹在 `0f..1f`、y 夹在 y 轴区间内；命中式拾取（点空白不动）；
 *    按住 **Alt** 吸附 1/20，按住 **Shift** 锁角（控制点只沿自身锚点与当前位置的直线移动，不允许反向）；
 *    两者同时按住时锁角 + 长度吸附；悬停 / 拖拽中的控制点换次色；
 * 2. 数值框与画布共用同一份状态（双向同步），x 框限 `0f..1f`、y 框限 y 轴区间；
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
            var plot by remember { mutableStateOf(CubicBezier.Standard) }

            Text("拖动控制点 · 数值框 · 预设（拖拽时 Alt 吸附 1/20 · Shift 锁定角度）")
            BezierCurveEditor(
                value = plot,
                onValueChange = { plot = it },
            )

            BezierAnimationPreview(bezier = plot)

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    var strict by remember { mutableStateOf(CubicBezier.EaseOut) }
                    Text("严格 0..1")
                    BezierCurvePlot(
                        value = strict,
                        onValueChange = { strict = it },
                        yRange = 0f..1f,
                        modifier = Modifier.size(160.dp),
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    var wide by remember { mutableStateOf(CubicBezier(0.68f, -0.24f, 0.32f, 1.24f)) }
                    Text("过冲 -0.4..1.4")
                    BezierCurvePlot(
                        value = wide,
                        onValueChange = { wide = it },
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
        }
    }
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
