package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme.selectedOutlineColor
import moe.forpleuvoir.nebula.common.color.Color as NebulaColor

/**
 * 一个"色阶家族"：像素风控件本质是同色相的多个亮度档，从勾边到高光。
 * 档位顺序由暗到亮：[outline] → [shadow] → [dark] → [base] → [highlight]。
 *
 * - [outline]：勾边，同色相最深，不透明（负责图形边界）
 * - [shadow]：投影/凹影，允许半透明（画在对象外缘）
 */
@Stable
data class ColorTone(
    val outline: Color,
    val shadow: Color,
    val dark: Color,
    val base: Color,
    val highlight: Color
) {

    companion object {
        fun solid(color: Color) = ColorTone(color, color, color, color, color)

        /**
         * 从基准色自动生成完整的五色组。
         *
         * outline 是"该色板的描边/强调框"，**不必然是全组最暗的色**——常态内描边
         * 用 [dark]，而 [outline] 承担外层框/选中/焦点等需要可辨度的角色，
         * 因此可用 [outline] 显式覆盖（如提亮到醒目色）；不传则按 [outlineDarken] 推导为深色。
         *
         * @param base 基准色
         * @param outline 显式描边色（默认 null = 按 outlineDarken 推导）
         * @param outlineDarken 未显式传 outline 时，描边相对 base 的暗化比例（0~1，越大越暗），默认 0.75
         * @param shadowBrightnessOffset 阴影亮度偏移（负值变暗），默认 -0.7f
         * @param shadowAlpha 阴影透明度（0~1），默认 0.5f
         * @param darkOffset 暗部变暗比例，默认 -0.35f
         * @param highlightOffset 高光变亮比例，默认 0.35f
         */
        fun fromBase(
            base: Color,
            outline: Color? = null,
            outlineDarken: Float = 0.75f,
            shadowBrightnessOffset: Float = -0.7f,
            shadowAlpha: Float = 0.5f,
            darkOffset: Float = -0.35f,
            highlightOffset: Float = 0.35f
        ): ColorTone {
            return ColorTone(
                outline = outline ?: base.adjustBrightness(-outlineDarken),
                shadow = base.adjustBrightness(shadowBrightnessOffset).copy(alpha = shadowAlpha),
                dark = base.adjustBrightness(darkOffset),
                base = base,
                highlight = base.adjustBrightness(highlightOffset)
            )
        }

        /**
         * 调整明度但**保持色相与饱和度**。
         *
         * 若在 RGB 上对各通道乘同一系数再 clamp，一旦某通道接近 1（高亮提亮时几乎必然），
         * 该通道被截断而低值通道仍按全比例上升 → 通道比例破坏 → 色相漂移。
         * 因此桥接 nebula [NebulaColor.value]（内部纯自实现 HSV 转换，无 java.awt 依赖）
         * 只调整明度 V：H/S 原样保留，不存在 RGB clamp 导致的偏色。
         *
         * @param offset 明度偏移（-1..1）：正=提亮，负=压暗
         */
        private fun Color.adjustBrightness(offset: Float): Color {
            val nebula = NebulaColor.fromARGB(this.toArgb())
            return Color(nebula.value((nebula.value + offset).coerceIn(0f, 1f)).argb)
        }
    }

}

@Stable
class Colors(
    //基础色
    background: ColorTone,
    surface: ColorTone,
    primary: ColorTone,
    secondary: ColorTone,
    error: ColorTone,
    //内容色
    onBackground: ColorTone,
    onSurface: ColorTone,
    onPrimary: ColorTone,
    onSecondary: ColorTone,
    onError: ColorTone,
    //是否为浅色主题
    isLight: Boolean,
) {

    var background: ColorTone by mutableStateOf(background, structuralEqualityPolicy())
        internal set

    var surface: ColorTone by mutableStateOf(surface, structuralEqualityPolicy())
        internal set

    var primary: ColorTone by mutableStateOf(primary, structuralEqualityPolicy())
        internal set

    var secondary: ColorTone by mutableStateOf(secondary, structuralEqualityPolicy())
        internal set

    var error: ColorTone by mutableStateOf(error, structuralEqualityPolicy())
        internal set

    var onBackground: ColorTone by mutableStateOf(onBackground, structuralEqualityPolicy())
        internal set

    var onSurface: ColorTone by mutableStateOf(onSurface, structuralEqualityPolicy())
        internal set

    var onPrimary: ColorTone by mutableStateOf(onPrimary, structuralEqualityPolicy())
        internal set

    var onSecondary: ColorTone by mutableStateOf(onSecondary, structuralEqualityPolicy())
        internal set

    var onError: ColorTone by mutableStateOf(onError, structuralEqualityPolicy())
        internal set

    var isLight by mutableStateOf(isLight, structuralEqualityPolicy())
        internal set

    fun copy(
        background: ColorTone = this.background,
        surface: ColorTone = this.surface,
        primary: ColorTone = this.primary,
        secondary: ColorTone = this.secondary,
        error: ColorTone = this.error,
        onBackground: ColorTone = this.onBackground,
        onSurface: ColorTone = this.onSurface,
        onPrimary: ColorTone = this.onPrimary,
        onSecondary: ColorTone = this.onSecondary,
        onError: ColorTone = this.onError,
        isLight: Boolean = this.isLight,
    ) = Colors(
        background = background,
        surface = surface,
        primary = primary,
        secondary = secondary,
        error = error,
        onBackground = onBackground,
        onSurface = onSurface,
        onPrimary = onPrimary,
        onSecondary = onSecondary,
        onError = onError,
        isLight = isLight
    )

    fun updateColorsFrom(other: Colors) {
        background = other.background
        surface = other.surface
        primary = other.primary
        secondary = other.secondary
        error = other.error
        onSurface = other.onSurface
        onPrimary = other.onPrimary
        onSecondary = other.onSecondary
        onError = other.onError
        isLight = other.isLight
    }

    override fun toString(): String {
        return "Colors(background=$background, surface=$surface, primary=$primary, secondary=$secondary, error=$error, onBackground=$onBackground, onSurface=$onSurface, onPrimary=$onPrimary, onSecondary=$onSecondary, onError=$onError, isLight=$isLight)"
    }

}

enum class ColorLevel {
    Outline,
    Shadow,
    Dark,
    Base,
    Highlight
}

fun ColorTone.get(level: ColorLevel): Color {
    return when (level) {
        ColorLevel.Outline   -> outline
        ColorLevel.Shadow    -> shadow
        ColorLevel.Dark      -> dark
        ColorLevel.Base      -> base
        ColorLevel.Highlight -> highlight
    }
}

fun Colors.contentColorFor(backgroundColor: Color): Color {
    return when (backgroundColor) {
        primary.base    -> onPrimary.base
        secondary.base  -> onSecondary.base
        background.base -> onBackground.base
        surface.base    -> onSurface.base
        error.base      -> onError.base
        else            -> Color.Unspecified
    }
}

val LocalColors = staticCompositionLocalOf { lightColors() }

/**
 * 选中/焦点外框指示色（outline）。
 *
 * 默认 `null` ：为 null 时由 [selectedOutlineColor] 自动回退——取 [LocalColors] 当前辅助色板的高亮
 * （`secondary.highlight`）作为指示色，醒目且与主色区分，无需显式配置。
 * 仅当某子树需要不同的强调外框色时才 [androidx.compose.runtime.CompositionLocalProvider] 提供覆盖。
 */
val LocalSelectedOutlineColor: ProvidableCompositionLocal<Color?> = staticCompositionLocalOf { null }

/**
 * 亮色主题 —— 主色 #8647B3
 *
 * outline 语义说明：描边/强调**不单独占用一档强调色**。
 * - 常态结构描边、阴影：直接用各色板 [ColorTone.dark] / [ColorTone.highlight] 等档位
 * - 选中/焦点等"需要醒目"的场景：Sokitsu 由组件换色板/绑高亮档表达，不走 outline
 * - [ColorTone.outline] 由 [ColorTone.fromBase] 推导为同色相深色，作兜底描边；
 *   个别需要覆写时经 fromBase 的 outline 参数显式给出
 */
fun lightColors(
    // 背景：暖白到浅灰
    background: ColorTone = ColorTone.fromBase(
        base = Color(0xFFF5F0EB),
        shadowBrightnessOffset = -0.15f,
        shadowAlpha = 0.15f,
        darkOffset = -0.1f,
        highlightOffset = 0.08f
    ),
    // 背景色上的内容：深灰（保证可读性）
    onBackground: ColorTone = ColorTone.fromBase(
        base = Color(0xFF2D2D2D),
        shadowBrightnessOffset = -0.3f,
        shadowAlpha = 0.15f,
        darkOffset = -0.2f,
        highlightOffset = 0.25f
    ),
    // 表面：纯白
    surface: ColorTone = ColorTone.fromBase(
        base = Color(0xFFFFFFFF),
        shadowBrightnessOffset = -0.2f,
        shadowAlpha = 0.2f,
        darkOffset = -0.1f,
        highlightOffset = 0.0f
    ),
    // 内容色：深灰（保证可读性）
    onSurface: ColorTone = ColorTone.fromBase(
        base = Color(0xFF2D2D2D),
        shadowBrightnessOffset = -0.3f,
        shadowAlpha = 0.15f,
        darkOffset = -0.2f,
        highlightOffset = 0.25f
    ),
    // 主色：紫色 #8647B3
    primary: ColorTone = ColorTone.fromBase(
        base = Color(0xFF8647B3),
        shadowBrightnessOffset = -0.45f,
        shadowAlpha = 0.4f,
        darkOffset = -0.25f,
        highlightOffset = 0.3f
    ),
    // 主色上的内容：白（紫色背景上白色文字最清晰）
    onPrimary: ColorTone = ColorTone.fromBase(
        base = Color(0xFFFFFFFF),
        shadowBrightnessOffset = -0.3f,
        shadowAlpha = 0.15f,
        darkOffset = -0.15f,
        highlightOffset = 0.0f
    ),
    // 辅色：暖橙色（与紫色形成对比）
    secondary: ColorTone = ColorTone.fromBase(
        base = Color(0xFFE88A4A),
        shadowBrightnessOffset = -0.45f,
        shadowAlpha = 0.35f,
        darkOffset = -0.25f,
        highlightOffset = 0.3f
    ),
    // 辅色上的内容：深色
    onSecondary: ColorTone = ColorTone.fromBase(
        base = Color(0xFF2D2D2D),
        shadowBrightnessOffset = -0.3f,
        shadowAlpha = 0.15f,
        darkOffset = -0.15f,
        highlightOffset = 0.2f
    ),
    // 错误色：红
    error: ColorTone = ColorTone.fromBase(
        base = Color(0xFFD32F2F),
        shadowBrightnessOffset = -0.5f,
        shadowAlpha = 0.4f,
        darkOffset = -0.3f,
        highlightOffset = 0.3f
    ),
    // 错误色上的内容：白
    onError: ColorTone = ColorTone.fromBase(
        base = Color(0xFFFFFFFF),
        shadowBrightnessOffset = -0.3f,
        shadowAlpha = 0.15f,
        darkOffset = -0.15f,
        highlightOffset = 0.0f
    )
) = Colors(
    background = background,
    surface = surface,
    primary = primary,
    secondary = secondary,
    error = error,
    onBackground = onBackground,
    onSurface = onSurface,
    onPrimary = onPrimary,
    onSecondary = onSecondary,
    onError = onError,
    isLight = true
)