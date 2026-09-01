package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorRole.*
import moe.forpleuvoir.nebula.common.util.expectedType
import moe.forpleuvoir.nebula.common.util.requireType
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.enum

@Stable
data class ColorTone(
    val shadow: Color,
    val dark: Color,
    val base: Color,
    val highlight: Color
) {

    companion object {
        fun solid(color: Color) = ColorTone(color, color, color, color)

        /**
         * 从基准色自动生成完整的四色组
         *
         * @param base 基准色
         * @param shadowBrightnessOffset 阴影亮度偏移（负值变暗），默认 -0.7f
         * @param shadowAlpha 阴影透明度（0~1），默认 0.5f
         * @param darkOffset 暗部变暗比例，默认 -0.35f
         * @param highlightOffset 高光变亮比例，默认 0.35f
         */
        fun fromBase(
            base: Color,
            shadowBrightnessOffset: Float = -0.7f,
            shadowAlpha: Float = 0.5f,
            darkOffset: Float = -0.35f,
            highlightOffset: Float = 0.35f
        ): ColorTone {
            return ColorTone(
                shadow = base.adjustBrightness(shadowBrightnessOffset).copy(alpha = shadowAlpha),
                dark = base.adjustBrightness(darkOffset),
                base = base,
                highlight = base.adjustBrightness(highlightOffset)
            )
        }

        private fun Color.adjustBrightness(offset: Float): Color {
            val r = (red * (1f + offset)).coerceIn(0f, 1f)
            val g = (green * (1f + offset)).coerceIn(0f, 1f)
            val b = (blue * (1f + offset)).coerceIn(0f, 1f)
            return Color(r, g, b, alpha)
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
    //描边
    outline: Color,
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

    var outline: Color by mutableStateOf(outline, structuralEqualityPolicy())
        internal set

    var isLight by mutableStateOf(isLight, structuralEqualityPolicy())
        internal set


    operator fun get(ref: ColorRef): Color {
        return when (ref.role) {
            Background   -> background.get(ref.level)
            Surface      -> surface.get(ref.level)
            Primary      -> primary.get(ref.level)
            Secondary    -> secondary.get(ref.level)
            OnBackground -> onBackground.get(ref.level)
            OnSurface    -> onSurface.get(ref.level)
            OnPrimary    -> onPrimary.get(ref.level)
            OnSecondary  -> onSecondary.get(ref.level)
            Error        -> error.get(ref.level)
            OnError      -> onError.get(ref.level)
            Outline      -> outline
        }
    }

    fun tone(role: ColorRole): ColorTone {
        return when (role) {
            Background   -> background
            Surface      -> surface
            Primary      -> primary
            Secondary    -> secondary
            OnBackground -> onBackground
            OnSurface    -> onSurface
            OnPrimary    -> onPrimary
            OnSecondary  -> onSecondary
            Error        -> error
            OnError      -> onError
            Outline      -> ColorTone.solid(outline)
        }
    }


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
        outline: Color = this.outline,
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
        outline = outline,
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
        outline = other.outline
        isLight = other.isLight
    }

    override fun toString(): String {
        return "Colors(background=$background, surface=$surface, primary=$primary, secondary=$secondary, error=$error, onBackground=$onBackground, onSurface=$onSurface, onPrimary=$onPrimary, onSecondary=$onSecondary, onError=$onError, outline=$outline, isLight=$isLight)"
    }


}

enum class ColorRole {
    Background,
    Surface,
    Primary,
    Secondary,
    Error,
    OnBackground,
    OnSurface,
    OnPrimary,
    OnSecondary,
    OnError,
    Outline,
}

enum class ColorLevel {
    Shadow,
    Dark,
    Base,
    Highlight
}

@Stable
data class ColorRef(
    val role: ColorRole,
    val level: ColorLevel = ColorLevel.Base
) {
    companion object : Codec<ColorRef> {

        override fun serialization(target: ColorRef): SerializeElement = SerializePrimitive(
            if (target.level == ColorLevel.Base) target.role.name else "${target.role.name}:${target.level.name}"
        )

        override fun deserialization(data: SerializeElement): Result<ColorRef> = DeserializationException.runCatching {
            val str = data.requireType<SerializePrimitive>().let {
                it.asString ?: throw expectedType(it.valueType, String::class)
            }
            val (role, level) = str.split(':', limit = 2).let {
                it[0] to (it.getOrNull(1) ?: ColorLevel.Base.name)
            }
            ColorRef(
                role = Codec.enum<ColorRole>().deserialization(SerializePrimitive(role)).getOrThrow(),
                level = Codec.enum<ColorLevel>().deserialization(SerializePrimitive(level)).getOrThrow()
            )
        }

        fun shadow(role: ColorRole) = ColorRef(role, ColorLevel.Shadow)
        fun dark(role: ColorRole) = ColorRef(role, ColorLevel.Dark)
        fun base(role: ColorRole) = ColorRef(role, ColorLevel.Base)
        fun highlight(role: ColorRole) = ColorRef(role, ColorLevel.Highlight)

        // 预定义常用引用
        val Background = base(ColorRole.Background)
        val Surface = base(ColorRole.Surface)
        val Primary = base(ColorRole.Primary)
        val Secondary = base(ColorRole.Secondary)
        val OnBackground = base(ColorRole.OnBackground)
        val OnSurface = base(ColorRole.OnSurface)
        val OnPrimary = base(ColorRole.OnPrimary)
        val OnSecondary = base(ColorRole.OnSecondary)
        val Error = base(ColorRole.Error)
        val OnError = base(ColorRole.OnError)
        val Outline = base(ColorRole.Outline)
    }
}

fun ColorTone.get(level: ColorLevel): Color {
    return when (level) {
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
 * 亮色主题 —— 主色 #8647B3
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
    // 背景色上的内容：深灰色（保证可读性）
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
    // 内容色：深灰色（保证可读性）
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
    // 主色上的内容：白色（紫色背景上白色文字最清晰）
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
    // 错误色：红色
    error: ColorTone = ColorTone.fromBase(
        base = Color(0xFFD32F2F),
        shadowBrightnessOffset = -0.5f,
        shadowAlpha = 0.4f,
        darkOffset = -0.3f,
        highlightOffset = 0.3f
    ),
    // 错误色上的内容：白色
    onError: ColorTone = ColorTone.fromBase(
        base = Color(0xFFFFFFFF),
        shadowBrightnessOffset = -0.3f,
        shadowAlpha = 0.15f,
        darkOffset = -0.15f,
        highlightOffset = 0.0f
    ),
    // 描边：深色（亮色主题用深灰描边）
    outline: Color = Color(0xFF3D3D3D)
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
    outline = outline,
    isLight = true
)