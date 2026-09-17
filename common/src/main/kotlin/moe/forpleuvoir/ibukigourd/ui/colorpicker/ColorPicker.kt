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
import moe.forpleuvoir.ibukigourd.ui.sokitsu.toast.ToastHandler
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
 * 页签切换用 [AnimatedContent] 做横向滑动 + 淡入淡出；**alpha 不参与该动画**——它是独立维度。
 *
 * **状态模型（关键，与上一版 M3 实现一致）**：
 * - 各通道的**显示值只在创建时从颜色取一次**（H / S / V 或 R / G / B，以及 alpha 各一份本地 state），
 *   之后**只由用户编辑改变**，绝不会因为颜色变化而反算回来；
 * - 值一律按**显示单位**保存（H 0..360、S / V 0..100、R / G / B / A 0..255），
 *   只在回调时换算成颜色分量 —— 若存成 0..1 再乘回去，会有 `26 → 26.000002` 的往返误差：
 *   数值框内部的"外部值同步"会因这个值不相等而重写文本并把光标甩到末尾，正在输入就被打断；
 * - 编辑时只把**当前颜色**的那一个通道换掉（[Color.withHue] / [Color.withSaturation] /
 *   [Color.withValue] / `color.copy(...)`），其余通道取自颜色本身 —— 各值互不干扰；
 * - 因此**外部改色不会同步到本组件的显示值**（与上一版一致）；只有"整色被替换"这类明确动作
 *   （目前是右键粘贴）才会通过 `valuesKey` 让各值重新取一次。
 *
 * 色值按钮：左键把当前色值文本写入剪贴板；**右键从剪贴板粘贴颜色**，接受
 * 十六进制（`#RGB` / `#RGBA` / `#RRGGBB` / `#AARRGGBB`，`#` 可省）或三 / 四个浮点数（`h, s, v[, a]`），
 * 解析失败则忽略。
 *
 * 已知待补：复制成功反馈（Toast 未落地）、hex 手工输入。
 *
 * @param color 外部当前颜色（各显示值的初始来源、也是"改一个通道"的作用对象）
 * @param onValueChange 颜色变化回调
 */
@Composable
fun ColorPicker(
    color: Color,
    onValueChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    var hsvTab by remember { mutableStateOf(true) }

    /**
     * 各通道值的"重取键"：默认 0，只有**整色被外部替换**（如右键粘贴）时才自增，
     * 让 H / S / V（或 R / G / B）与 alpha 重新从当前颜色取一次。
     * 普通的内部编辑**绝不自增** —— 否则正在输入的数字会被反解后的值顶掉。
     */
    var valuesKey by remember { mutableStateOf(0) }

    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    /**
     * 右键粘贴：整色替换，成功后自增 [valuesKey] 让各显示值重新取值。
     *
     * 成功与失败（剪贴板为空 / 文本不是可解析的颜色）都给提示——粘贴没有可见结果时，
     * 静默会让人分不清是"没读到剪贴板"还是"格式不支持"。
     */
    fun pasteFromClipboard() {
        scope.launch {
            val pasted = clipboard.getClipEntry()?.text?.let(::parseColorText)
            if (pasted == null) {
                ToastHandler.showContent { Text(IGLang.Color.pasteColorFailed) }
                return@launch
            }
            onValueChange(pasted)
            valuesKey++
            ToastHandler.showContent { Text(IGLang.Color.pasteColorSuccess(pasted.toNebulaColor())) }
        }
    }

    // alpha 是独立维度，单独持一份值（不参与页签动画，也不参与另两套通道的合成）
    var alpha by remember(valuesKey) { mutableStateOf((color.alpha * 255f).roundToInt()) }

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
                    onClick = {
                        scope.launch {
                            clipboard.setClipEntry(ClipEntry(valueText))
                            // 提示里回显的是实际复制的内容（HSV 页签是三元组文本，RGB 页签是 hex）
                            ToastHandler.showContent {
                                Text(IGLang.Color.copyTextSuccess(valueText))
                            }
                        }
                    },
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
                            HsvChannelColumn(color, valuesKey, onValueChange)
                        } else {
                            RgbChannelColumn(color, valuesKey, onValueChange)
                        }
                    }
                }

                Spacer(Modifier.height(ColorPickerDefaults.RowSpacing))
                AlphaChannelSlider(
                    color = color,
                    alpha = alpha,
                    onValueChange = { value ->
                        alpha = value.roundToInt()
                        onValueChange(color.copy(alpha = alpha / 255f))
                    },
                )
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
 * 独立的 alpha 通道条：不参与页签切换动画，也不参与另两套通道的合成。
 *
 * [alpha] 是本通道自己的值（0..255，只在"整色被替换"时重新取值），[onValueChange] 回传 0..255 的原始值，
 * 编辑时只把 [color] 的 alpha 换掉。
 */
@Composable
private fun AlphaChannelSlider(
    color: Color,
    alpha: Int,
    onValueChange: (Float) -> Unit,
) {
    ColorChannelSlider(
        label = "A",
        value = alpha.toFloat(),
        valueRange = 0f..255f,
        gradientColors = listOf(color.copy(alpha = 0f), color.copy(alpha = 1f)),
        onValueChange = { onValueChange(it) },
        integral = true,
        showCheckerboard = true,
        enabled = LocalColorPickerEnableAlpha.current,
    )
}

/**
 * HSV 页的三个颜色通道：H / S / V **各持一份值**。
 *
 * 取值只在创建（或 [valuesKey] 变化）时从 [color] 拿一次，之后只由用户编辑改变，
 * **不会**因为颜色变化被反算 —— 否则输入框里正在敲的值会被精度差顶掉（如 26 → 25.9）。
 * 编辑时只把 [color] 的对应通道换掉（[Color.withHue] / [Color.withSaturation] / [Color.withValue]），
 * 其余通道取自颜色本身，互不干扰。
 */
@Composable
private fun HsvChannelColumn(
    color: Color,
    valuesKey: Int,
    onEmit: (Color) -> Unit,
) {
    // 各通道按**显示单位**存（H 0..360、S / V 0..100），只在回调时换算成分量 ——
    // 若存 0..1 再乘回去，会有 `26 / 360 * 360 = 26.000002` 的往返误差，
    // 数值框内部的"外部值同步"会因这个差值重写文本并把光标甩到末尾，正在输入就被打断。
    var hueDegrees by remember(valuesKey) { mutableStateOf(color.hsvChannels().first * 360f) }
    var saturation by remember(valuesKey) { mutableStateOf(color.hsvChannels().second * 100f) }
    var value by remember(valuesKey) { mutableStateOf(color.hsvChannels().third * 100f) }

    ColorChannelSlider(
        label = "H",
        value = hueDegrees,
        valueRange = 0f..360f,
        gradientColors = HueGradient,
        onValueChange = { hueDegrees = it; onEmit(hsvToColor(hueDegrees / 360f, saturation / 100f, value / 100f, color.alpha)) },
        suffix = "°",
        valueToText = { "%.1f".format(it) },
    )
    Spacer(Modifier.height(ColorPickerDefaults.RowSpacing))
    ColorChannelSlider(
        label = "S",
        value = saturation,
        valueRange = 0f..100f,
        gradientColors = listOf(
            Color.hsv(hueDegrees, 0f, value / 100f),
            Color.hsv(hueDegrees, 1f, value / 100f),
        ),
        onValueChange = { saturation = it; onEmit(hsvToColor(hueDegrees / 360f, saturation / 100f, value / 100f, color.alpha)) },
        suffix = "%",
        valueToText = { "%.1f".format(it) },
    )
    Spacer(Modifier.height(ColorPickerDefaults.RowSpacing))
    ColorChannelSlider(
        label = "V",
        value = value,
        valueRange = 0f..100f,
        gradientColors = listOf(
            Color.hsv(hueDegrees, saturation / 100f, 0f),
            Color.hsv(hueDegrees, saturation / 100f, 1f),
        ),
        onValueChange = { value = it; onEmit(hsvToColor(hueDegrees / 360f, saturation / 100f, value / 100f, color.alpha)) },
        suffix = "%",
        valueToText = { "%.1f".format(it) },
    )
}

/**
 * RGB 页的三个颜色通道：R / G / B **各持一份值**（0..255），规则同 [HsvChannelColumn] ——
 * 取值只拿一次、编辑时只把 [color] 的对应分量换掉。
 */
@Composable
private fun RgbChannelColumn(
    color: Color,
    valuesKey: Int,
    onEmit: (Color) -> Unit,
) {
    var red by remember(valuesKey) { mutableStateOf((color.red * 255f).roundToInt()) }
    var green by remember(valuesKey) { mutableStateOf((color.green * 255f).roundToInt()) }
    var blue by remember(valuesKey) { mutableStateOf((color.blue * 255f).roundToInt()) }

    ColorChannelSlider(
        label = "R",
        value = red.toFloat(),
        valueRange = 0f..255f,
        gradientColors = listOf(Color(0, green, blue, 255), Color(255, green, blue, 255)),
        onValueChange = { red = it.roundToInt(); onEmit(color.copy(red = red / 255f)) },
        integral = true,
    )
    Spacer(Modifier.height(ColorPickerDefaults.RowSpacing))
    ColorChannelSlider(
        label = "G",
        value = green.toFloat(),
        valueRange = 0f..255f,
        gradientColors = listOf(Color(red, 0, blue, 255), Color(red, 255, blue, 255)),
        onValueChange = { green = it.roundToInt(); onEmit(color.copy(green = green / 255f)) },
        integral = true,
    )
    Spacer(Modifier.height(ColorPickerDefaults.RowSpacing))
    ColorChannelSlider(
        label = "B",
        value = blue.toFloat(),
        valueRange = 0f..255f,
        gradientColors = listOf(Color(red, green, 0, 255), Color(red, green, 255, 255)),
        onValueChange = { blue = it.roundToInt(); onEmit(color.copy(blue = blue / 255f)) },
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
 * 与 `moe.forpleuvoir.ibukigourd.util.ColorContrast.kt` 里的同名实现同源，但那份是 private
 * 且 `hsvToColor` 会丢 alpha；取色器需要保留 alpha，故在此自带一份。
 * 后续若别处也要用，建议把 util 的那份提公开并加 alpha 参数，两边合并。
 */
private fun Color.hsvChannels(): Triple<Float, Float, Float> {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val d = max - min
    val h = if (d == 0f) 0f
    else when (max) {
        red   -> ((green - blue) / d).mod(6f)
        green -> ((blue - red) / d + 2f).mod(6f)
        else  -> ((red - green) / d + 4f).mod(6f)
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
        0    -> Triple(v, t, p)
        1    -> Triple(q, v, p)
        2    -> Triple(p, v, t)
        3    -> Triple(p, q, v)
        4    -> Triple(t, p, v)
        else -> Triple(v, p, q)
    }
    return Color(r, g, b, alpha.coerceIn(0f, 1f))
}

/** 只换色相：饱和度 / 明度 / alpha 取自接收者本身（对应上一版的 `Color.hue(x)`）。 */
private fun Color.withHue(hue: Float): Color {
    val (_, s, v) = hsvChannels()
    return hsvToColor(hue, s, v, alpha)
}

/** 只换饱和度：色相 / 明度 / alpha 取自接收者本身。 */
private fun Color.withSaturation(saturation: Float): Color {
    val (h, _, v) = hsvChannels()
    return hsvToColor(h, saturation, v, alpha)
}

/** 只换明度：色相 / 饱和度 / alpha 取自接收者本身。 */
private fun Color.withValue(value: Float): Color {
    val (h, s, _) = hsvChannels()
    return hsvToColor(h, s, value, alpha)
}
