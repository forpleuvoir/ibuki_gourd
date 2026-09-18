package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import moe.forpleuvoir.ibukigourd.util.withHsv

/**
 * Sokitsu 配色方案：每个槽位是一个**纯 [Color]**。
 *
 * 层次 / 立体感不再由色阶档位表达——精灵的明暗完全来自素材灰阶
 * （ Multiply 模式：主题槽位色 × 素材灰度），因此主题只需要给出
 * "每个语义槽位是什么颜色"。
 *
 * 槽位语义对齐 Material3：
 * - 容器/底色：[background] / [surface] / [surfaceVariant] / [primary] / [primaryContainer] /
 *   [secondary] / [error]
 * - 内容色：各 `on*` 槽位（[contentColorFor] 按容器槽位配对）
 * - [outline]：全局描边色
 */
@Stable
class ColorScheme(
    background: Color,
    surface: Color,
    surfaceVariant: Color,
    primary: Color,
    secondary: Color,
    error: Color,
    primaryContainer: Color,
    onBackground: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    onPrimary: Color,
    onPrimaryContainer: Color,
    onSecondary: Color,
    onError: Color,
    outline: Color,
    isLight: Boolean,
) {

    var background: Color by mutableStateOf(background, structuralEqualityPolicy())
        internal set

    /** 表面/容器底色：带主色色相倾向的低饱和中性色（卡片、开关轨道、菜单底）。 */
    var surface: Color by mutableStateOf(surface, structuralEqualityPolicy())
        internal set

    var primary: Color by mutableStateOf(primary, structuralEqualityPolicy())
        internal set

    var secondary: Color by mutableStateOf(secondary, structuralEqualityPolicy())
        internal set

    var error: Color by mutableStateOf(error, structuralEqualityPolicy())
        internal set

    var onBackground: Color by mutableStateOf(onBackground, structuralEqualityPolicy())
        internal set

    /** 表面上的内容色（文字/图标）。 */
    var onSurface: Color by mutableStateOf(onSurface, structuralEqualityPolicy())
        internal set

    var onPrimary: Color by mutableStateOf(onPrimary, structuralEqualityPolicy())
        internal set

    /**
     * 主色容器色：主色的浅色调（M3 `primaryContainer`）。
     *
     * 用于需要与主色呼应、但又要作为背景衬托内容的容器——如开关开启态轨道、
     * 选中态卡片底。它比 [surfaceVariant] 更饱和、更靠近 [primary]。
     */
    var primaryContainer: Color by mutableStateOf(primaryContainer, structuralEqualityPolicy())
        internal set

    /** 主色容器上的内容色（文字/图标）。 */
    var onPrimaryContainer: Color by mutableStateOf(onPrimaryContainer, structuralEqualityPolicy())
        internal set

    var onSecondary: Color by mutableStateOf(onSecondary, structuralEqualityPolicy())
        internal set

    var onError: Color by mutableStateOf(onError, structuralEqualityPolicy())
        internal set

    /**
     * 表面变体：与 [surface] 同色相、明度/饱和度略有差异的容器色（M3 `surfaceVariant`）。
     *
     * 用于需要与主表面拉开区分度、但又不该用主色的容器——如开关关闭态轨道、次要卡片底、
     * 输入框填充。
     */
    var surfaceVariant: Color by mutableStateOf(surfaceVariant, structuralEqualityPolicy())
        internal set

    /** 表面变体之上的内容色（文字/图标）。 */
    var onSurfaceVariant: Color by mutableStateOf(onSurfaceVariant, structuralEqualityPolicy())
        internal set

    /** 全局描边色。 */
    var outline: Color by mutableStateOf(outline, structuralEqualityPolicy())
        internal set

    var isLight by mutableStateOf(isLight, structuralEqualityPolicy())
        internal set

    fun copy(
        background: Color = this.background,
        surface: Color = this.surface,
        surfaceVariant: Color = this.surfaceVariant,
        primary: Color = this.primary,
        secondary: Color = this.secondary,
        error: Color = this.error,
        primaryContainer: Color = this.primaryContainer,
        onBackground: Color = this.onBackground,
        onSurface: Color = this.onSurface,
        onSurfaceVariant: Color = this.onSurfaceVariant,
        onPrimary: Color = this.onPrimary,
        onPrimaryContainer: Color = this.onPrimaryContainer,
        onSecondary: Color = this.onSecondary,
        onError: Color = this.onError,
        outline: Color = this.outline,
        isLight: Boolean = this.isLight,
    ) = ColorScheme(
        background = background,
        surface = surface,
        surfaceVariant = surfaceVariant,
        primary = primary,
        secondary = secondary,
        error = error,
        primaryContainer = primaryContainer,
        onBackground = onBackground,
        onSurface = onSurface,
        onSurfaceVariant = onSurfaceVariant,
        onPrimary = onPrimary,
        onPrimaryContainer = onPrimaryContainer,
        onSecondary = onSecondary,
        onError = onError,
        outline = outline,
        isLight = isLight,
    )

    fun updateColorsFrom(other: ColorScheme) {
        background = other.background
        surface = other.surface
        surfaceVariant = other.surfaceVariant
        primary = other.primary
        secondary = other.secondary
        error = other.error
        primaryContainer = other.primaryContainer
        onBackground = other.onBackground
        onSurface = other.onSurface
        onSurfaceVariant = other.onSurfaceVariant
        onPrimary = other.onPrimary
        onPrimaryContainer = other.onPrimaryContainer
        onSecondary = other.onSecondary
        onError = other.onError
        outline = other.outline
        isLight = other.isLight
    }

    override fun toString(): String {
        return "ColorScheme(background=$background, surface=$surface, surfaceVariant=$surfaceVariant, primary=$primary, secondary=$secondary, error=$error, primaryContainer=$primaryContainer, onBackground=$onBackground, onSurface=$onSurface, onSurfaceVariant=$onSurfaceVariant, onPrimary=$onPrimary, onPrimaryContainer=$onPrimaryContainer, onSecondary=$onSecondary, onError=$onError, outline=$outline, isLight=$isLight)"
    }

}

/**
 * 按容器槽位配对内容色：传入的容器色与哪个槽位一致，就返回该槽位的 on 色；
 * 未匹配返回 [Color.Unspecified]（调用方自行兜底）。
 */
fun ColorScheme.contentColorFor(backgroundColor: Color): Color = when (backgroundColor) {
    primary            -> onPrimary
    primaryContainer   -> onPrimaryContainer
    secondary          -> onSecondary
    background         -> onBackground
    surface            -> onSurface
    surfaceVariant     -> onSurfaceVariant
    error              -> onError
    else               -> Color.Unspecified
}

val LocalColorScheme = staticCompositionLocalOf { lightColorScheme() }

/**
 * 选中/焦点外框指示色。
 *
 * 默认 [Color.Unspecified]：未指定时由 Sokitsu 主题自动回退——基于当前主题主色计算
 * 鲜艳互补色（见 [moe.forpleuvoir.ibukigourd.util.contrasting]）。
 */
val LocalSelectedOutlineColor: ProvidableCompositionLocal<Color> = staticCompositionLocalOf { Color.Unspecified }

/**
 * 亮色主题 —— 主色 #A356D9，暖白背景。
 */
fun lightColorScheme(
    background: Color = Color(0xFFF5F0EB),
    onBackground: Color = Color(0xFF2D2D2D),
    // 表面/容器：浅紫白（比背景亮一档）
    surface: Color = Color(0xFFE1D1ED),
    onSurface: Color = Color(0xFF2D2D2D),
    // 表面变体：比 surface 更亮更淡的浅紫
    surfaceVariant: Color = Color(0xFFEED9FF),
    onSurfaceVariant: Color = Color(0xFF3F3A45),
    primary: Color = Color(0xFFA356D9),
    onPrimary: Color = Color(0xFFFFFFFF),
    // 主色容器：主色的浅色调（开关开启态轨道、选中卡片底）
    primaryContainer: Color = Color(0xFFAE7DD1),
    onPrimaryContainer: Color = Color(0xFF3A2150),
    // 辅色：暖橙色（与紫色形成对比）
    secondary: Color = Color(0xFFE88A4A),
    onSecondary: Color = Color(0xFF2D2D2D),
    error: Color = Color(0xFFD32F2F),
    onError: Color = Color(0xFFFFFFFF),
    outline: Color = Color(0xFF5F3280),
    isLight: Boolean = true,
) = ColorScheme(
    background = background,
    surface = surface,
    surfaceVariant = surfaceVariant,
    primary = primary,
    secondary = secondary,
    error = error,
    primaryContainer = primaryContainer,
    onBackground = onBackground,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    onPrimary = onPrimary,
    onPrimaryContainer = onPrimaryContainer,
    onSecondary = onSecondary,
    onError = onError,
    outline = outline,
    isLight = isLight,
)

/**
 * 暗色主题 —— 亮紫主色 #A970DC，深暖灰背景。
 *
 * 深底上的描边/层级靠"更亮"表达，因此 outline 与各槽位取中亮色。
 */
fun darkColorScheme(
    background: Color = Color(0xFF1B1916),
    onBackground: Color = Color(0xFFEDE7DF),
    // 表面/容器：深灰紫（比背景略亮一档，深底上的层级靠"更亮"表达）
    surface: Color = Color(0xFF343038),
    onSurface: Color = Color(0xFFEDE7DF),
    // 表面变体：比 surface 亮一档
    surfaceVariant: Color = Color(0xFF47414D),
    // 表面变体上的内容：亮紫灰（深底上的次要内容色必须够亮才可读）
    onSurfaceVariant: Color = Color(0xFFC9BFD2),
    primary: Color = Color(0xFFA970DC),
    onPrimary: Color = Color(0xFF241033),
    // 主色容器：深底上取中亮紫
    primaryContainer: Color = Color(0xFF625073),
    onPrimaryContainer: Color = Color(0xFFEDE7DF),
    secondary: Color = Color(0xFFF09A55),
    onSecondary: Color = Color(0xFF331B08),
    error: Color = Color(0xFFEF5350),
    onError: Color = Color(0xFF2B0B0A),
    outline: Color = Color(0xFFB9A3CC),
    isLight: Boolean = false,
) = ColorScheme(
    background = background,
    surface = surface,
    surfaceVariant = surfaceVariant,
    primary = primary,
    secondary = secondary,
    error = error,
    primaryContainer = primaryContainer,
    onBackground = onBackground,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    onPrimary = onPrimary,
    onPrimaryContainer = onPrimaryContainer,
    onSecondary = onSecondary,
    onError = onError,
    outline = outline,
    isLight = isLight,
)
