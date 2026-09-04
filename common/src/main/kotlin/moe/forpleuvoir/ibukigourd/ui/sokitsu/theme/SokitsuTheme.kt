package moe.forpleuvoir.ibukigourd.ui.sokitsu.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import moe.forpleuvoir.ibukigourd.ui.sokitsu.ProvideTextStyle

object SokitsuTheme {

    val colors: Colors
        @Composable @ReadOnlyComposable get() = LocalColors.current

    /**
     * 当前生效的选中/焦点外框指示色：
     * 取 [LocalSelectedOutlineColor] 提供的值；未提供（null）时自动回退为 `secondary.highlight`。
     */
    val selectedOutlineColor: Color
        @Composable
        get() = LocalSelectedOutlineColor.current ?: colors.secondary.highlight

    val typography: Typography
        @Composable @ReadOnlyComposable get() = LocalTypography.current

}

@Composable
fun SokitsuTheme(
    colors: Colors = SokitsuTheme.colors,
    typography: Typography = SokitsuTheme.typography,
    content: @Composable () -> Unit,
) {
    val rememberedColors = remember { colors.copy() }.apply { updateColorsFrom(colors) }
    val selectionColors = rememberTextSelectionColors(rememberedColors)
    CompositionLocalProvider(
        LocalColors provides rememberedColors,
        LocalContentAlpha provides ContentAlpha.high,
        LocalIndication provides SokitsuIndicationNodeFactory,
        LocalTextSelectionColors provides selectionColors,
        LocalTypography provides typography,
    ) {
        ProvideTextStyle(value = typography.body, content = content)
    }
}