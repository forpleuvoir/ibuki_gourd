package moe.forpleuvoir.ibukigourd.ui.util

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import moe.forpleuvoir.nebula.common.color.Color
import androidx.compose.ui.graphics.Color as ComposeColor

inline val Color.toComposeColor: ComposeColor get() = ComposeColor(this.red, this.green, this.blue, this.alpha)

inline val ComposeColor.toNebulaColor: Color get() = Color.fromARGB(this.red, this.green, this.blue, this.alpha)

@Composable
fun ProvideContentColorTextStyle(
    contentColor: ComposeColor,
    textStyle: TextStyle,
    content: @Composable () -> Unit,
) {
    val mergedStyle = LocalTextStyle.current.merge(textStyle)
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalTextStyle provides mergedStyle,
        content = content,
    )
}