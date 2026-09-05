package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

@Immutable
class Typography(
    val title: TextStyle = DefaultTextStyle.copy(fontSize = DefaultTextStyle.fontSize * 2),
    val subtitle: TextStyle = DefaultTextStyle.copy(fontSize = DefaultTextStyle.fontSize * 1.5),
    val body: TextStyle = DefaultTextStyle,
    val button: TextStyle = DefaultTextStyle,
) {

    fun copy(
        title: TextStyle = this.title,
        subtitle: TextStyle = this.subtitle,
        body: TextStyle = this.body,
        button: TextStyle = this.button,
    ) = Typography(
        title,
        subtitle,
        body,
        button,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Typography

        if (title != other.title) return false
        if (subtitle != other.subtitle) return false
        if (body != other.body) return false
        if (button != other.button) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + subtitle.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + button.hashCode()
        return result
    }

    override fun toString(): String {
        return "Typography(title=$title, subtitle=$subtitle, body=$body, button=$button)"
    }

}


/**
 * 基准文字样式（24sp，项目默认字号 = Fusion Pixel 12px 设计网格的 2× 整数倍，
 * 字形与屏幕像素保持整数比锐利）。
 *
 * 字体不在 TextStyle 里指定：compose-minecraft 的文本渲染经 `LocalDefaultFont`
 * （FontDescription，默认 Fusion Pixel）解析，与 Compose FontFamily 体系无关；
 * 需要等宽等场景可用 `CompositionLocalProvider(LocalDefaultFont provides ...)` 子树覆盖。
 * 派生字号（title/subtitle）以本值为基准倍乘，建议保持 12 的整数倍以维持像素锐利。
 *
 * 注意：**不能直接用 [TextStyle.Default]** —— 它的 `fontSize` 是 `TextUnit.Unspecified`，
 * 任何算术（如上 [Typography] 推导 title/subtitle 时的 `fontSize * 2`）都会抛
 * `IllegalArgumentException: Cannot perform operation for Unspecified type`，
 * 且发生在 `LocalTypography` 默认工厂首次求值时（编译期不可见）。
 */
internal val DefaultTextStyle =
    TextStyle.Default.copy(fontSize = 24.sp)

val LocalTypography = staticCompositionLocalOf { Typography() }