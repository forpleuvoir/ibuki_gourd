package moe.forpleuvoir.ibukigourd.ui.colorpicker

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.sokitsu.RadioButtonGroup
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TextButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip.tooltip
import moe.forpleuvoir.ibukigourd.util.contrasting
import moe.forpleuvoir.ibukigourd.util.toNebulaColor
import kotlin.math.roundToInt

/**
 * 是否允许调整 alpha：ColorScheme 一类不需要透明度的场景可在子树里关掉（只读展示）。
 */
val LocalColorPickerEnableAlpha = compositionLocalOf { true }

/**
 * 取色器：HSV / RGB 两套通道条 + alpha 条 + 预览与色值复制 / 粘贴。
 *
 * 布局分两层，各自是独立的 Row（不靠手调偏移对齐）：
 * 1. **顶部行**：页签（HSV / RGB）+ 弹性空隙 + 色值按钮（右对齐）；
 * 2. **内容行**：左栏通道条、右栏预览，两栏**垂直居中对齐**。
 * 两行宽度都按 [ColorPickerDefaults.TotalWidth] 约束（否则 `Row` 会被父布局拉满屏）。
 *
 * 页签切换用 [AnimatedContent] 做横向滑动 + 淡入淡出（HSV → RGB 时新内容自右侧进、旧内容向左出，切回反向）；
 * **alpha 条不参与这个动画**——它是独立维度，固定在三个颜色通道下方不动。
 *
 * **状态模型（关键）**：组件内部**每个通道各持一份独立的值**（见 [HsvChannelColumn] / [RgbChannelColumn]），
 * 拖动某个通道只改它自己、并用当前这一组值合成颜色回调；**不会拿回调后的颜色反算其它通道**。
 * 外部改色（含右键粘贴）用 `lastEmitted` 守卫回灌：只有"传进来的颜色 ≠ 组件刚发出去的那个"才整体重算。
 *
 * 色值按钮：左键把当前色值文本写入剪贴板；**右键从剪贴板粘贴颜色**，接受
 * 十六进制（`#RGB` / `#RGBA` / `#RRGGBB` / `#AARRGGBB`，`#` 可省）或三 / 四个浮点数（`h, s, v[, a]`），
 * 解析失败则忽略。
 *
 * 已知待补：复制成功反馈（Toast 未落地）、hex 手工输入。
 *
 * @param color 外部当前颜色（用于初始化、外部回灌与预览）
 * @param onValueChange 颜色变化回调
 */
@Composable
fun ColorPicker(
    color: Color,
    onValueChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    var hsvTab by remember { mutableStateOf(true) }

    // 组件刚发出去的颜色：用于区分"外部改色"与"自己编辑的回声"
    var lastEmitted by remember { mutableStateOf<Color?>(null) }

    fun emit(value: Color) {
        lastEmitted = value
        onValueChange(value)
    }

    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    /** 右键粘贴：读剪贴板 → 解析为颜色 → 交给外部（走 [lastEmitted] 守卫的整组回灌）；失败忽略。 */
    fun pasteFromClipboard() {
        scope.launch {
            val text = clipboard.getClipEntry()?.text ?: return@launch
            val pasted = parseColorText(text) ?: return@launch
            onValueChange(pasted)
        }
    }

    Column(modifier) {
        // ── 顶部行：页签 + 色值按钮（宽度按内容宽度约束，否则会被父布局拉满、按钮飞到屏幕最右）──
        Row(
            modifier = Modifier.width(ColorPickerDefaults.TotalWidth),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButtonGroup(selected = if (hsvTab) 0 else 1, onSelect = { hsvTab = it == 0 }) {
                item { Text("HSV") }
                item { Text("RGB") }
            }
            Spacer(Modifier.weight(1f))

            // 与上一版一致：HSV 页显示三元组、RGB 页显示 hex
            val valueText = if (hsvTab) {
                val (h, s, v) = color.hsvChannels()
                "(${"%.1f".format(h * 360f)}, ${"%.1f".format(s * 100f)}, ${"%.1f".format(v * 100f)})"
            } else {
                color.toHexString()
            }
            Box(
                modifier = Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            // 左键由 TextButton 自己处理；这里只接右键（secondary）
                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                pasteFromClipboard()
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
                }
            ) {
                TextButton(
                    onClick = { scope.launch { clipboard.setClipEntry(ClipEntry(valueText)) } },
                    text = valueText,
                    modifier = Modifier.tooltip {
                        Column {
                            Text(IGLang.Color.clickCopyColor(color.toNebulaColor()))
                            Text(IGLang.Color.rightClickPasteColor)
                        }
                    },
                )
            }
        }

        Spacer(Modifier.height(ColorPickerDefaults.TabGap))

        // ── 内容行：通道条 + 预览（两栏垂直居中对齐）──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(ColorPickerDefaults.ChannelWidth)) {
                // 只有三个颜色通道参与页签切换动画；alpha 是独立维度，固定在下方不动
                AnimatedContent(
                    targetState = hsvTab,
                    modifier = Modifier.width(ColorPickerDefaults.ChannelWidth),
                    transitionSpec = {
                        // 切到 RGB（targetState = false）时新内容自右侧进、旧内容向左出；切回 HSV 反向
                        val direction = if (targetState) -1 else 1
                        (slideInHorizontally { width -> direction * width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> -direction * width } + fadeOut())
                    },
                    label = "colorPickerTab",
                ) { tab ->
                    Column {
                        if (tab) {
                            HsvChannelColumn(color, lastEmitted, ::emit)
                        } else {
                            RgbChannelColumn(color, lastEmitted, ::emit)
                        }
                    }
                }

                Spacer(Modifier.height(ColorPickerDefaults.RowSpacing))
                AlphaChannelSlider(color, onValueChange = ::emit)
            }

            Spacer(Modifier.width(ColorPickerDefaults.ColumnGap))

            Column(
                modifier = Modifier.width(ColorPickerDefaults.PreviewSize),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .width(ColorPickerDefaults.PreviewSize)
                        .height(ColorPickerDefaults.PreviewSize)
                        .border(1.dp, color.contrasting()),
                ) {
                    Checkerboard(ColorPickerDefaults.PreviewTile, Modifier.matchParentSize())
                    Box(Modifier.matchParentSize().background(color))
                }
            }
        }
    }
}

/**
 * 独立的 alpha 通道条：不参与页签切换动画，也不受 HSV / RGB 两套通道的本地状态影响，
 * 直接以 [color] 的当前值编辑（alpha 是独立维度，没有 HSV 那种"某通道无意义"的问题，
 * 所以不需要本地 state 兜底）。
 */
@Composable
private fun AlphaChannelSlider(
    color: Color,
    onValueChange: (Color) -> Unit,
) {
    ColorChannelSlider(
        label = "A",
        value = color.alpha * 255f,
        valueRange = 0f..255f,
        gradientColors = listOf(color.copy(alpha = 0f), color.copy(alpha = 1f)),
        onValueChange = { onValueChange(color.copy(alpha = it / 255f)) },
        integral = true,
        showCheckerboard = true,
        enabled = LocalColorPickerEnableAlpha.current,
    )
}

/**
 * HSV 页的三个颜色通道：H / S / V 各持一份独立本地 state。
 *
 * [lastEmitted] 是组件刚发出去的颜色 —— 只有"传进来的 [color] ≠ 它"时才整体回灌各通道，
 * 这样外部改色能同步进来，而自己的编辑（回声）不会把别的通道重置。
 * alpha 不在本栏内（见 [AlphaChannelSlider]），合成时取 [color] 当前的 alpha。
 */
@Composable
private fun HsvChannelColumn(
    color: Color,
    lastEmitted: Color?,
    onEmit: (Color) -> Unit,
) {
    var hue by remember { mutableStateOf(color.hsvChannels().first) }
    var saturation by remember { mutableStateOf(color.hsvChannels().second) }
    var value by remember { mutableStateOf(color.hsvChannels().third) }
    LaunchedEffect(color) {
        if (color != lastEmitted) {
            val (h, s, v) = color.hsvChannels()
            hue = h
            saturation = s
            value = v
        }
    }

    val hueDegrees = hue * 360f
    val alpha = color.alpha

    ColorChannelSlider(
        label = "H",
        value = hueDegrees,
        valueRange = 0f..360f,
        gradientColors = HueGradient,
        onValueChange = { hue = it / 360f; onEmit(hsvToColor(hue, saturation, value, alpha)) },
        valueToText = { "%.1f".format(it) },
    )
    Spacer(Modifier.height(ColorPickerDefaults.RowSpacing))
    ColorChannelSlider(
        label = "S",
        value = saturation * 100f,
        valueRange = 0f..100f,
        gradientColors = listOf(
            Color.hsv(hueDegrees, 0f, value),
            Color.hsv(hueDegrees, 1f, value),
        ),
        onValueChange = { saturation = it / 100f; onEmit(hsvToColor(hue, saturation, value, alpha)) },
        suffix = "%",
        valueToText = { "%.1f".format(it) },
    )
    Spacer(Modifier.height(ColorPickerDefaults.RowSpacing))
    ColorChannelSlider(
        label = "V",
        value = value * 100f,
        valueRange = 0f..100f,
        gradientColors = listOf(
            Color.hsv(hueDegrees, saturation, 0f),
            Color.hsv(hueDegrees, saturation, 1f),
        ),
        onValueChange = { value = it / 100f; onEmit(hsvToColor(hue, saturation, value, alpha)) },
        suffix = "%",
        valueToText = { "%.1f".format(it) },
    )
}

/**
 * RGB 页的三个颜色通道：R / G / B 各持一份独立本地 state，规则同 [HsvChannelColumn]。
 */
@Composable
private fun RgbChannelColumn(
    color: Color,
    lastEmitted: Color?,
    onEmit: (Color) -> Unit,
) {
    var red by remember { mutableStateOf((color.red * 255f).roundToInt()) }
    var green by remember { mutableStateOf((color.green * 255f).roundToInt()) }
    var blue by remember { mutableStateOf((color.blue * 255f).roundToInt()) }
    LaunchedEffect(color) {
        if (color != lastEmitted) {
            red = (color.red * 255f).roundToInt()
            green = (color.green * 255f).roundToInt()
            blue = (color.blue * 255f).roundToInt()
        }
    }

    val alpha = color.alpha

    ColorChannelSlider(
        label = "R",
        value = red.toFloat(),
        valueRange = 0f..255f,
        gradientColors = listOf(Color(0, green, blue, 255), Color(255, green, blue, 255)),
        onValueChange = { red = it.roundToInt(); onEmit(Color(red / 255f, green / 255f, blue / 255f, alpha)) },
        integral = true,
    )
    Spacer(Modifier.height(ColorPickerDefaults.RowSpacing))
    ColorChannelSlider(
        label = "G",
        value = green.toFloat(),
        valueRange = 0f..255f,
        gradientColors = listOf(Color(red, 0, blue, 255), Color(red, 255, blue, 255)),
        onValueChange = { green = it.roundToInt(); onEmit(Color(red / 255f, green / 255f, blue / 255f, alpha)) },
        integral = true,
    )
    Spacer(Modifier.height(ColorPickerDefaults.RowSpacing))
    ColorChannelSlider(
        label = "B",
        value = blue.toFloat(),
        valueRange = 0f..255f,
        gradientColors = listOf(Color(red, green, 0, 255), Color(red, green, 255, 255)),
        onValueChange = { blue = it.roundToInt(); onEmit(Color(red / 255f, green / 255f, blue / 255f, alpha)) },
        integral = true,
    )
}

/**
 * 取色器的尺寸与外观常量。
 *
 * 目前是写死的常量（组件还在打磨阶段）；稳定后再按 sokitsu 组件的做法抽成
 * `XxxMeta` + `ColorPickerTheme.kt` 走主题 meta，让资源包可覆盖。
 */
object ColorPickerDefaults {

    /** 通道栏宽度（内容行左栏）。 */
    val ChannelWidth = 400.dp

    /** 左右栏间距。 */
    val ColumnGap = 40.dp

    /** 顶部行与内容行的间距。 */
    val TabGap = 20.dp

    /** 通道行首标签宽度。 */
    val LabelWidth = 32.dp

    /** 通道条高度：与右侧数值框**无关**（行高由数值框决定，条在其中居中）。 */
    val BarHeight = 28.dp

    /** 数值框宽度（高度不指定，用主题 textField 的默认最小高度）。 */
    val FieldWidth = 104.dp

    /** 通道条与数值框的间距。 */
    val FieldGap = 12.dp

    /** 通道行间距。 */
    val RowSpacing = 12.dp

    /** 指示条宽度。 */
    val ThumbWidth = 4.dp

    /** 指示条高度：**比条本身高**，上下各冒出一截，读起来才是"把手"（绘制节点不裁剪，可直接画到条外）。 */
    val ThumbHeight = 36.dp

    /** 指示条填充色。 */
    val ThumbColor = Color.White.copy(alpha = 0.9f)

    /** 指示条描边色。 */
    val ThumbOutline = Color(0x90000000)

    /** 通道条描边色。 */
    val BarOutline = Color(0xA0000000)

    /** alpha 条棋盘格边长：取条高一半，**高度上刚好两格**（列方向不整除时由绘制端截断末格）。 */
    val BarTile = BarHeight / 2

    /** 预览方块棋盘格边长。 */
    val PreviewTile = 16.dp

    /** 预览方块边长（也是右栏宽度）。 */
    val PreviewSize = 224.dp

    /** 整块内容宽度 = 左栏 + 栏间距 + 右栏（两行都按它约束，避免 `Row` 被父布局拉满）。 */
    val TotalWidth = ChannelWidth + ColumnGap + PreviewSize

    /**
     * 把取色器放进 `AlertDialog` 时所需的面板最小宽度：
     * 内容宽度 + `alert_dialog` 默认内容内边距（24dp × 2）。
     */
    val DialogWidth = TotalWidth + 48.dp
}

/** 色相条色标：0/60/…/360，两端同为红色（闭合）；色相渐变靠多色标表达（其余通道两段即可）。 */
private val HueGradient: List<Color> =
    listOf(0f, 60f, 120f, 180f, 240f, 300f, 360f).map { Color.hsv(it, 1f, 1f) }

/** `#RRGGBB`；alpha 不满时给 `#AARRGGBB`。 */
internal fun Color.toHexString(): String {
    val a = (alpha * 255f).roundToInt().coerceIn(0, 255)
    val r = (red * 255f).roundToInt().coerceIn(0, 255)
    val g = (green * 255f).roundToInt().coerceIn(0, 255)
    val b = (blue * 255f).roundToInt().coerceIn(0, 255)
    return if (a == 255) {
        "#%02X%02X%02X".format(r, g, b)
    } else {
        "#%02X%02X%02X%02X".format(a, r, g, b)
    }
}

/**
 * 解析剪贴板文本为颜色，失败返回 null。
 *
 * 接受两种写法：
 * - **十六进制**：`#RGB` / `#RGBA` / `#RRGGBB` / `#AARRGGBB`（`#` 可省，大小写不限）；
 * - **三 / 四个浮点数**（逗号或空白分隔，外层括号可省）：`h, s, v[, a]`，
 *   与色值按钮复制出去的 `(h, s, v)` 格式一致 —— hue 0..360、s / v 按百分比 0..100；
 *   alpha 兼容 0..1（小数）、0..100（百分比）、0..255（字节）三种写法。
 */
private fun parseColorText(raw: String): Color? {
    val text = raw.trim()
    if (text.isEmpty()) return null

    val parts = text.removeSurrounding("(", ")")
        .split(',', ' ', '\t', '\r', '\n')
        .filter { it.isNotBlank() }
    val floats = parts.mapNotNull { it.toFloatOrNull() }
    if (parts.size == floats.size && (floats.size == 3 || floats.size == 4)) {
        val hue = (floats[0] / 360f).mod(1f)
        val saturation = (floats[1] / 100f).coerceIn(0f, 1f)
        val value = (floats[2] / 100f).coerceIn(0f, 1f)
        val alpha = if (floats.size == 4) {
            val a = floats[3]
            when {
                a <= 1f   -> a
                a <= 100f -> a / 100f
                else      -> a / 255f
            }.coerceIn(0f, 1f)
        } else 1f
        return hsvToColor(hue, saturation, value, alpha)
    }

    val hex = text.removePrefix("#").trim()
    if (hex.length != 3 && hex.length != 4 && hex.length != 6 && hex.length != 8) return null
    if (hex.any { it.digitToIntOrNull(16) == null }) return null
    fun nibble(index: Int) = hex[index].digitToInt(16)
    fun byte(index: Int) = nibble(index) * 16 + nibble(index + 1)
    return when (hex.length) {
        3    -> Color(nibble(0) * 17, nibble(1) * 17, nibble(2) * 17, 255)
        4    -> Color(nibble(1) * 17, nibble(2) * 17, nibble(3) * 17, nibble(0) * 17)
        6    -> Color(byte(0), byte(2), byte(4), 255)
        else -> Color(byte(2), byte(4), byte(6), byte(0))
    }
}

/**
 * HSV 反解，hue / saturation / value 均为 0..1。
 *
 * 与 [moe.forpleuvoir.ibukigourd.util.ColorContrast] 里的同名实现同源，但那份是 private
 * 且 `hsvToColor` 会丢 alpha；取色器需要保留 alpha，故在此自带一份。
 * 后续若别处也要用，建议把 util 的那份提公开并加 alpha 参数，两边合并。
 */
private fun Color.hsvChannels(): Triple<Float, Float, Float> {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val d = max - min
    val h = if (d == 0f) 0f
    else when (max) {
        red -> ((green - blue) / d).mod(6f)
        green -> ((blue - red) / d + 2f).mod(6f)
        else -> ((red - green) / d + 4f).mod(6f)
    } / 6f
    val s = if (max == 0f) 0f else d / max
    return Triple(h, s, max)
}

/** HSV 合成（含 alpha）；hue 0..1、saturation / value / alpha 0..1。 */
private fun hsvToColor(h: Float, s: Float, v: Float, alpha: Float): Color {
    val hh = h.mod(1f) * 6f
    val i = hh.toInt() % 6
    val f = hh - hh.toInt()
    val p = v * (1f - s)
    val q = v * (1f - f * s)
    val t = v * (1f - (1f - f) * s)
    val (r, g, b) = when (i) {
        0 -> Triple(v, t, p)
        1 -> Triple(q, v, p)
        2 -> Triple(p, v, t)
        3 -> Triple(p, q, v)
        4 -> Triple(t, p, v)
        else -> Triple(v, p, q)
    }
    return Color(r, g, b, alpha.coerceIn(0f, 1f))
}
