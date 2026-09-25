package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import moe.forpleuvoir.ibukigourd.util.toComposeColor
import moe.forpleuvoir.ibukigourd.util.toNebulaColor
import moe.forpleuvoir.ibukigourd.util.withHsv
import moe.forpleuvoir.nebula.common.util.expectedType
import moe.forpleuvoir.nebula.common.util.requireKey
import moe.forpleuvoir.nebula.common.util.requireTypeOrNull
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.base.builder.build
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.color
import moe.forpleuvoir.nebula.serialization.extensions.requireBoolean

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

    /**
     * 序列化：槽位名（snake_case）→ 颜色，外加 `is_light` 标记。
     *
     * 与主题 meta 的配色段同构（颜色都是 `#AARRGGBB`），因此一份配色既可以是资源包 meta 里的段，
     * 也可以是配置里的一项。
     */
    companion object : Codec<ColorScheme> {

        override fun serialization(target: ColorScheme): SerializeElement = SerializeObject.build {
            ColorSchemeSlots.flatMap { (slot, on) -> listOf(slot) + listOfNotNull(on) }.forEach { name ->
                name.toSnakeCase() to Codec.color.serialization(target.slot(name).toNebulaColor())
            }
            "is_light" to target.isLight
        }

        override fun deserialization(data: SerializeElement): Result<ColorScheme> =
            DeserializationException.runCatching {
                val obj = data.requireTypeOrNull<SerializeObject>()
                    ?: throw expectedType(data::class, SerializeObject::class)
                val isLight = obj.requireBoolean("is_light")
                // 缺槽位回落该亮暗档的内置默认，因此手写的配色段可以只列出想改的槽位
                var scheme = if (isLight) lightColorScheme() else darkColorScheme()
                ColorSchemeSlots.flatMap { (slot, on) -> listOf(slot) + listOfNotNull(on) }.forEach { name ->
                    if (obj.containsKey(name.toSnakeCase())) {
                        val color = Codec.color.deserialization(obj.requireKey(name.toSnakeCase())).getOrThrow()
                        scheme = scheme.withSlot(name, color.toComposeColor())
                    }
                }
                scheme.copy(isLight = isLight)
            }

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

/**
 * 配色方案的可编辑槽位清单：`xx` 与 `onXx` 成对（没有配对 `on` 的槽位写成 `null`）。
 *
 * 与 [ColorScheme] 的构造参数、主题 meta 的配色段、[ColorScheme] 的序列化一一对应；
 * 新增槽位时同步本表与 [withSlot]。
 */
val ColorSchemeSlots: List<Pair<String, String?>> = listOf(
    "background" to "onBackground",
    "surface" to "onSurface",
    "surfaceVariant" to "onSurfaceVariant",
    "primary" to "onPrimary",
    "primaryContainer" to "onPrimaryContainer",
    "secondary" to "onSecondary",
    "error" to "onError",
    "outline" to null,
)

/** 全部槽位名（`xx` 与 `onXx` 都算一个），顺序同 [ColorSchemeSlots]。 */
val ColorSchemeSlotNames: List<String>
    get() = ColorSchemeSlots.flatMap { (slot, on) -> listOf(slot) + listOfNotNull(on) }

/** 按槽位名取色（未知槽位给 [Color.Unspecified]）。 */
fun ColorScheme.slot(name: String): Color = when (name) {
    "background"         -> background
    "surface"            -> surface
    "surfaceVariant"     -> surfaceVariant
    "primary"            -> primary
    "primaryContainer"   -> primaryContainer
    "secondary"          -> secondary
    "error"              -> error
    "onBackground"       -> onBackground
    "onSurface"          -> onSurface
    "onSurfaceVariant"   -> onSurfaceVariant
    "onPrimary"          -> onPrimary
    "onPrimaryContainer" -> onPrimaryContainer
    "onSecondary"        -> onSecondary
    "onError"            -> onError
    "outline"            -> outline
    else                 -> Color.Unspecified
}

/**
 * 按槽位名重设一个槽位色（未知槽位原样返回）。
 *
 * [ColorScheme] 的字段 setter 是 internal（只给主题内部用），所以外部改槽位走公开的
 * [ColorScheme.copy] 重建实例。
 */
fun ColorScheme.withSlot(name: String, color: Color): ColorScheme = when (name) {
    "background"         -> copy(background = color)
    "surface"            -> copy(surface = color)
    "surfaceVariant"     -> copy(surfaceVariant = color)
    "primary"            -> copy(primary = color)
    "primaryContainer"   -> copy(primaryContainer = color)
    "secondary"          -> copy(secondary = color)
    "error"              -> copy(error = color)
    "onBackground"       -> copy(onBackground = color)
    "onSurface"          -> copy(onSurface = color)
    "onSurfaceVariant"   -> copy(onSurfaceVariant = color)
    "onPrimary"          -> copy(onPrimary = color)
    "onPrimaryContainer" -> copy(onPrimaryContainer = color)
    "onSecondary"        -> copy(onSecondary = color)
    "onError"            -> copy(onError = color)
    "outline"            -> copy(outline = color)
    else                 -> this
}

/** 驼峰槽位名 → 序列化 / meta 里的 snake_case 键。 */
internal fun String.toSnakeCase(): String = buildString {
    this@toSnakeCase.forEach { char ->
        if (char.isUpperCase()) {
            append('_')
            append(char.lowercaseChar())
        } else {
            append(char)
        }
    }
}

/**
 * 全部槽位序列化为**主题 meta 的配色段** JSON（键 snake_case，值 `#AARRGGBB`）。
 *
 * 结果可直接粘进 `sokitsu_meta.json` 的 `light` / `dark` 段。
 */
fun ColorScheme.toMetaJson(): String = ColorSchemeSlotNames.joinToString(
    separator = ",\n    ",
    prefix = "{\n    ",
    postfix = "\n}",
) { name ->
    "\"${name.toSnakeCase()}\": \"#%08X\"".format(slot(name).toArgb())
}
