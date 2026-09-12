package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import moe.forpleuvoir.ibukigourd.util.toComposeColor
import moe.forpleuvoir.ibukigourd.util.toNebulaColor
import moe.forpleuvoir.ibukigourd.util.withHsv

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

    /**
     * 是否为**已解析**的色板。
     *
     * 只判 [base]：构造 [Unspecified] 时五个档位同为 [Color.Unspecified]，
     * 因此 base 已指定即可认为整个色板可用（不存在"部分档位未指定"的中间态）。
     */
    val isSpecified: Boolean get() = base.isSpecified

    companion object {

        /**
         * "未指定"占位色板：五个档位均为 [Color.Unspecified]。
         *
         * 用于组件 `Colors` 工厂的默认参数，语义是"调用方没意见，请按组件 token 映射表
         * 结合当前 [ColorScheme] 解析"。与 Compose 的 [Color.Unspecified] 同构，配合
         * [takeOrElse] 使用，目的是区分**"没传"**与**"传了一个具体值"**——后者无法用 null
         * 表达（本类字段非空，且 null 在 [LocalSokitsuTone] 等处已另有语义）。
         */
        val Unspecified: ColorTone = ColorTone(
            outline = Color.Unspecified,
            shadow = Color.Unspecified,
            dark = Color.Unspecified,
            base = Color.Unspecified,
            highlight = Color.Unspecified,
        )

        fun solid(color: Color) = ColorTone(color, color, color, color, color)

        /**
         * 从基准色自动生成完整的五色组。
         *
         * outline 是"该色板的描边/强调框"，**不必然是全组最暗的色**——常态内描边
         * 用 [dark]，而 [outline] 承担外层框/选中/焦点等需要可辨度的角色，
         * 因此可用 [outline] 显式覆盖（如提亮到醒目色）；不传则按 [outlineDarken] 推导为深色。
         *
         * [shadow] 恒为 **50% 透明度的纯黑**：投影与色板色相无关，中性黑在亮暗底色上都协调。
         *
         * @param base 基准色
         * @param outline 显式描边色（默认 null = 按 [outlineDarken] 推导为深色；
         *   **暗色主题必须传亮色**，深底上深描边不可见）
         * @param outlineDarken 未显式传 outline 时，描边相对 base 的暗化比例（0~1，越大越暗），默认 0.75
         * @param darkOffset 暗部变暗比例，默认 -0.35f
         * @param highlightOffset 高光变亮比例，默认 0.35f
         */
        fun fromBase(
            base: Color,
            outline: Color? = null,
            outlineDarken: Float = 0.75f,
            darkOffset: Float = -0.35f,
            highlightOffset: Float = 0.35f
        ): ColorTone {
            return ColorTone(
                outline = outline ?: base.adjustBrightness(-outlineDarken),
                shadow = Color(0f, 0f, 0f, 0.5f),
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
         * 因此桥接 nebula 的 value（内部纯自实现 HSV 转换，无 java.awt 依赖）
         * 只调整明度 V：H/S 原样保留，不存在 RGB clamp 导致的偏色。
         *
         * @param offset 明度偏移（-1..1）：正=提亮，负=压暗
         */
        private fun Color.adjustBrightness(offset: Float): Color =
            toNebulaColor().let { it.value((it.value + offset).coerceIn(0f, 1f)) }.toComposeColor()
    }

}

/**
 * 与 [Color.takeOrElse] 对称：本色板未指定（[ColorTone.isSpecified] == false）时取 [block] 的结果。
 *
 * 用于串起"调用点 > 作用域 > 组件 token > 主题槽位"的多级回退链：
 * ```kotlin
 * tone.takeOrElse { LocalSokitsuTone.current }
 *     .takeOrElse { scheme.fromToken(ButtonTokens.Container) }
 * ```
 */
inline fun ColorTone.takeOrElse(block: () -> ColorTone): ColorTone =
    if (isSpecified) this else block()

/**
 * 五档统一叠加 alpha（返回新实例，不修改原色板）。
 *
 * **不能只压 [base]**：主题染色按 [ColorLevel] 取档，精灵各图层分别用到
 * outline / dark / base / highlight；只改 base 会让禁用态的描边与高光仍是满不透明，
 * 视觉上"底色淡了但边框没淡"。
 *
 * 不透明度取值参考 Material3：`DisabledContainerOpacity = 0.12f`、`DisabledLabelTextOpacity = 0.38f`。
 */
fun ColorTone.withAlpha(alpha: Float): ColorTone = ColorTone(
    outline = outline.copy(alpha = alpha),
    shadow = shadow.copy(alpha = alpha),
    dark = dark.copy(alpha = alpha),
    base = base.copy(alpha = alpha),
    highlight = highlight.copy(alpha = alpha),
)

@Stable
class ColorScheme(
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
    //主色容器（M3 primaryContainer 语义）
    primaryContainer: ColorTone,
    onPrimaryContainer: ColorTone,
    onSecondary: ColorTone,
    onError: ColorTone,
    //表面变体（M3 surfaceVariant 语义）
    surfaceVariant: ColorTone,
    onSurfaceVariant: ColorTone,
    //是否为浅色主题
    isLight: Boolean,
) {

    var background: ColorTone by mutableStateOf(background, structuralEqualityPolicy())
        internal set

    /** 表面/容器底色：带主色色相倾向的低饱和中性色（卡片、开关轨道、菜单底）。 */
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

    /** 表面上的内容色（文字/图标）。 */
    var onSurface: ColorTone by mutableStateOf(onSurface, structuralEqualityPolicy())
        internal set

    var onPrimary: ColorTone by mutableStateOf(onPrimary, structuralEqualityPolicy())
        internal set

    /**
     * 主色容器色：主色的浅色调（M3 `primaryContainer`）。
     *
     * 用于需要与主色呼应、但又要作为背景衬托内容的容器——如开关开启态轨道、
     * 选中态卡片底。它比 [surfaceVariant] 更饱和、更靠近 [primary]，
     * 因此"主色家族"的辨识度更高，又不至于与 [primary] 本体撞色。
     */
    var primaryContainer: ColorTone by mutableStateOf(primaryContainer, structuralEqualityPolicy())
        internal set

    /** 主色容器上的内容色（文字/图标）。 */
    var onPrimaryContainer: ColorTone by mutableStateOf(onPrimaryContainer, structuralEqualityPolicy())
        internal set

    var onSecondary: ColorTone by mutableStateOf(onSecondary, structuralEqualityPolicy())
        internal set

    var onError: ColorTone by mutableStateOf(onError, structuralEqualityPolicy())
        internal set

    /**
     * 表面变体：与 [surface] 同色相、明度/饱和度略有差异的容器色（M3 `surfaceVariant`）。
     *
     * 用于需要与主表面拉开区分度、但又不该用主色的容器——如开关关闭态轨道、次要卡片底、
     * 输入框填充。这样"表面上的层级"由 tone 阶梯表达，而不是靠加描边硬分。
     */
    var surfaceVariant: ColorTone by mutableStateOf(surfaceVariant, structuralEqualityPolicy())
        internal set

    /** 表面变体之上的内容色（文字/图标）。 */
    var onSurfaceVariant: ColorTone by mutableStateOf(onSurfaceVariant, structuralEqualityPolicy())
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
        primaryContainer: ColorTone = this.primaryContainer,
        onPrimaryContainer: ColorTone = this.onPrimaryContainer,
        onSecondary: ColorTone = this.onSecondary,
        onError: ColorTone = this.onError,
        surfaceVariant: ColorTone = this.surfaceVariant,
        onSurfaceVariant: ColorTone = this.onSurfaceVariant,
        isLight: Boolean = this.isLight,
    ) = ColorScheme(
        background = background,
        surface = surface,
        primary = primary,
        secondary = secondary,
        error = error,
        onBackground = onBackground,
        onSurface = onSurface,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        onSecondary = onSecondary,
        onError = onError,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        isLight = isLight
    )

    fun updateColorsFrom(other: ColorScheme) {
        background = other.background
        surface = other.surface
        primary = other.primary
        secondary = other.secondary
        error = other.error
        onBackground = other.onBackground
        onSurface = other.onSurface
        onPrimary = other.onPrimary
        primaryContainer = other.primaryContainer
        onPrimaryContainer = other.onPrimaryContainer
        onSecondary = other.onSecondary
        onError = other.onError
        surfaceVariant = other.surfaceVariant
        onSurfaceVariant = other.onSurfaceVariant
        isLight = other.isLight
    }

    override fun toString(): String {
        return "ColorScheme(background=$background, surface=$surface, primary=$primary, secondary=$secondary, error=$error, onBackground=$onBackground, onSurface=$onSurface, onPrimary=$onPrimary, primaryContainer=$primaryContainer, onPrimaryContainer=$onPrimaryContainer, onSecondary=$onSecondary, onError=$onError, surfaceVariant=$surfaceVariant, onSurfaceVariant=$onSurfaceVariant, isLight=$isLight)"
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

fun ColorScheme.contentColorFor(backgroundColor: Color): Color {
    return when (backgroundColor) {
        primary.base    -> onPrimary.base
        primaryContainer.base -> onPrimaryContainer.base
        secondary.base  -> onSecondary.base
        background.base -> onBackground.base
        surface.base    -> onSurface.base
        surfaceVariant.base -> onSurfaceVariant.base
        error.base      -> onError.base
        else            -> Color.Unspecified
    }
}

/**
 * 计算该色板之上可读的内容色（文字/图标）：
 * 匹配主题已知色板（[base] 与 primary/secondary/error/background 的 base 一致
 * → 对应 onX 色板的 base）；未知色板回退为 [ColorScheme.onBackground.base]——
 * 内容色永远来自主题的 onXX 配色，不凭空发明黑/白。
 */
@Composable
fun ColorTone.contentColor(): Color {
    val scheme = LocalColorScheme.current
    return scheme.contentColorFor(base).takeOrElse { scheme.onBackground.base }
}

val LocalColorScheme = staticCompositionLocalOf { lightColorScheme() }

/**
 * 选中/焦点外框指示色（outline）。
 *
 * 默认 [Color.Unspecified]：未指定时由 [SokitsuTheme.selectedOutlineColor] 自动回退——基于当前主题
 * 主色 base 计算鲜艳互补色（见 [moe.forpleuvoir.ibukigourd.util.contrasting]），无需显式配置。
 * 仅当某子树需要不同的强调外框色时才 [androidx.compose.runtime.CompositionLocalProvider] 提供覆盖。
 */
val LocalSelectedOutlineColor: ProvidableCompositionLocal<Color> = staticCompositionLocalOf { Color.Unspecified }

/**
 * 亮色主题 —— 主色 #8647B3，暖白背景。
 *
 * 阴影统一为 50% 透明度纯黑（[ColorTone.fromBase] 固定行为），不随色板带色相。
 *
 * outline 语义说明：描边/强调**不单独占用一档强调色**。
 * - 常态结构描边、阴影：直接用各色板 [ColorTone.dark] / [ColorTone.highlight] 等档位
 * - 选中/焦点等"需要醒目"的场景：Sokitsu 由组件换色板/绑高亮档表达，不走 outline
 * - [ColorTone.outline] 由 [ColorTone.fromBase] 推导为同色相深色，作兜底描边；
 *   个别需要覆写时经 fromBase 的 outline 参数显式给出
 */
fun lightColorScheme(
    // 背景：暖白到浅灰
    background: ColorTone = ColorTone.fromBase(
        base = Color(0xFFF5F0EB),
        darkOffset = -0.1f,
        highlightOffset = 0.08f
    ),
    // 背景色上的内容：深灰（保证可读性）
    onBackground: ColorTone = ColorTone.fromBase(
        base = Color(0xFF2D2D2D),
        darkOffset = -0.2f,
        highlightOffset = 0.25f
    ),
    // 表面/容器：主色色相 + 低饱和 + 高明度（M3 surfaceVariant 思路，卡片/开关轨道底）
    surface: ColorTone = ColorTone.fromBase(
        base = Color(0xFF8647B3).withHsv(saturation = 0.12f, value = 0.93f),
        darkOffset = -0.06f,
        highlightOffset = 0.06f
    ),
    // 表面上的内容：深灰
    onSurface: ColorTone = ColorTone.fromBase(
        base = Color(0xFF2D2D2D),
        darkOffset = -0.15f
    ),
    // 主色：紫色 #8647B3
    primary: ColorTone = ColorTone.fromBase(
        base = Color(0xFF8647B3),
        darkOffset = -0.25f,
        highlightOffset = 0.3f
    ),
    // 主色上的内容：白（紫色背景上白色文字最清晰）
    onPrimary: ColorTone = ColorTone.fromBase(
        base = Color(0xFFFFFFFF),
        darkOffset = -0.15f
    ),
    // 主色容器：主色的浅色调（开关开启态轨道、选中卡片底，比 surfaceVariant 更饱和、更靠近 primary）
    primaryContainer: ColorTone = ColorTone.fromBase(
        base = Color(0xFF8647B3).withHsv(saturation = 0.40f, value = 0.82f),
        darkOffset = -0.08f,
        highlightOffset = 0.08f
    ),
    // 主色容器上的内容：深紫（浅紫底上深紫文字最清晰）
    onPrimaryContainer: ColorTone = ColorTone.fromBase(
        base = Color(0xFF3A2150),
        darkOffset = -0.15f,
        highlightOffset = 0.2f
    ),
    // 辅色：暖橙色（与紫色形成对比）
    secondary: ColorTone = ColorTone.fromBase(
        base = Color(0xFFE88A4A),
        darkOffset = -0.25f,
        highlightOffset = 0.3f
    ),
    // 辅色上的内容：深色
    onSecondary: ColorTone = ColorTone.fromBase(
        base = Color(0xFF2D2D2D),
        darkOffset = -0.15f,
        highlightOffset = 0.2f
    ),
    // 错误色：红
    error: ColorTone = ColorTone.fromBase(
        base = Color(0xFFD32F2F),
        darkOffset = -0.3f,
        highlightOffset = 0.3f
    ),
    // 错误色上的内容：白
    onError: ColorTone = ColorTone.fromBase(
        base = Color(0xFFFFFFFF),
        darkOffset = -0.15f
    ),
    // 表面变体：与 surface 同色相，饱和度略高、明度略低（开关关闭态轨道、次要卡片底、输入框填充）
    surfaceVariant: ColorTone = ColorTone.fromBase(
        base = Color(0xFF8647B3).withHsv(saturation = 0.18f, value = 0.86f),
        darkOffset = -0.08f,
        highlightOffset = 0.08f
    ),
    // 表面变体上的内容：比 onSurface 弱一档的次要内容色
    onSurfaceVariant: ColorTone = ColorTone.fromBase(
        base = Color(0xFF5A5A5A),
        darkOffset = -0.15f,
        highlightOffset = 0.2f
    )
) = ColorScheme(
    background = background,
    surface = surface,
    primary = primary,
    secondary = secondary,
    error = error,
    onBackground = onBackground,
    onSurface = onSurface,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        onSecondary = onSecondary,
        onError = onError,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        isLight = true
    )

/**
 * 暗色主题 —— 亮紫主色 #A970DC，深暖灰背景。
 *
 * 暗色主题的描边语义与亮色**相反**：深底上描边必须是**亮色**（[ColorTone.fromBase]
 * 默认推导为同色相深色，深底上不可见），因此各表面色板显式传入亮色 [ColorTone.outline]。
 * 阴影同样为 50% 透明度纯黑（深底上纯黑投影依然有效）。
 *
 * 用法：`SokitsuTheme(colorScheme = darkColorScheme())`
 */
fun darkColorScheme(
    // 背景：深暖灰
    background: ColorTone = ColorTone.fromBase(
        base = Color(0xFF1B1916),
        outline = Color(0xFFA69E93),
        darkOffset = -0.1f,
        highlightOffset = 0.35f
    ),
    // 背景色上的内容：暖白
    onBackground: ColorTone = ColorTone.fromBase(
        base = Color(0xFFEDE7DF),
        darkOffset = -0.25f,
        highlightOffset = 0.1f
    ),
    // 表面/容器：主色色相 + 低饱和暗色（比背景略亮一档，卡片/开关轨道底）
    surface: ColorTone = ColorTone.fromBase(
        base = Color(0xFFA970DC).withHsv(saturation = 0.15f, value = 0.22f),
        outline = Color(0xFFA69E93).withHsv(saturation = 0.15f, value = 0.6f),
        darkOffset = -0.08f,
        highlightOffset = 0.25f
    ),
    // 表面上的内容：暖白。显式中亮描边——该色板也被开关禁用态把手等"形状"复用，
    // 默认推导的深描边在深底上不可见
    onSurface: ColorTone = ColorTone.fromBase(
        base = Color(0xFFEDE7DF),
        outline = Color(0xFF938F89),
        darkOffset = -0.2f
    ),
    // 主色：亮紫 #A970DC（深底上更醒目）
    primary: ColorTone = ColorTone.fromBase(
        base = Color(0xFFA970DC),
        outline = Color(0xFFD5B8F0),
        darkOffset = -0.2f,
        highlightOffset = 0.25f
    ),
    // 主色上的内容：深紫黑（亮紫背景上深色文字最清晰）
    onPrimary: ColorTone = ColorTone.fromBase(
        base = Color(0xFF241033),
        darkOffset = -0.1f,
        highlightOffset = 0.3f
    ),
    // 主色容器：主色的浅色调（深底上取中亮紫，开关开启态轨道、选中卡片底）
    primaryContainer: ColorTone = ColorTone.fromBase(
        base = Color(0xFFA970DC).withHsv(saturation = 0.30f, value = 0.45f),
        outline = Color(0xFFD5B8F0),
        darkOffset = -0.08f,
        highlightOffset = 0.25f
    ),
    // 主色容器上的内容：暖白
    onPrimaryContainer: ColorTone = ColorTone.fromBase(
        base = Color(0xFFEDE7DF),
        darkOffset = -0.2f
    ),
    // 辅色：暖橙（亮化）
    secondary: ColorTone = ColorTone.fromBase(
        base = Color(0xFFF09A55),
        outline = Color(0xFFFFD9B8),
        darkOffset = -0.2f,
        highlightOffset = 0.25f
    ),
    // 辅色上的内容：深棕黑
    onSecondary: ColorTone = ColorTone.fromBase(
        base = Color(0xFF331B08),
        darkOffset = -0.1f,
        highlightOffset = 0.25f
    ),
    // 错误色：亮红
    error: ColorTone = ColorTone.fromBase(
        base = Color(0xFFEF5350),
        outline = Color(0xFFFFB4AB),
        darkOffset = -0.2f,
        highlightOffset = 0.25f
    ),
    // 错误色上的内容：深红黑
    onError: ColorTone = ColorTone.fromBase(
        base = Color(0xFF2B0B0A),
        darkOffset = -0.1f,
        highlightOffset = 0.2f
    ),
    // 表面变体：比 surface 亮一档（深底上的层级靠"更亮"表达）。
    // 注意：Tint 模式下精灵明度完全由纹理灰阶决定，本槽位的 V 不影响精灵渲染，
    // 只作为 contentColor 配对等场景的语义锚
    surfaceVariant: ColorTone = ColorTone.fromBase(
        base = Color(0xFFA970DC).withHsv(saturation = 0.15f, value = 0.30f),
        outline = Color(0xFFA69E93).withHsv(saturation = 0.15f, value = 0.7f),
        darkOffset = -0.08f,
        highlightOffset = 0.3f
    ),
    // 表面变体上的内容：比 onSurface 弱一档。显式中亮描边（开关关闭态把手的勾边层，
    // 深底上默认深描边不可见）。
    // **暗色主题下同样取深色**：surfaceVariant 容器（滑条轨道等精灵）的明度由纹理灰阶
    // 决定、两种主题下都偏亮，其上的内容色必须压深才可读——与 onSurface 跟随深底
    // 变亮的逻辑方向相反
    onSurfaceVariant: ColorTone = ColorTone.fromBase(
        base = Color(0xFF3F3A45),
        outline = Color(0xFF938F89),
        darkOffset = -0.2f,
        highlightOffset = 0.1f
    )
) = ColorScheme(
    background = background,
    surface = surface,
    primary = primary,
    secondary = secondary,
    error = error,
    onBackground = onBackground,
    onSurface = onSurface,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        onSecondary = onSecondary,
        onError = onError,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        isLight = false
    )