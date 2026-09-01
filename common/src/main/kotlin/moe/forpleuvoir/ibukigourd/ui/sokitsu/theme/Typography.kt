package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

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


internal val DefaultTextStyle =
    TextStyle.Default

val LocalTypography = staticCompositionLocalOf { Typography() }