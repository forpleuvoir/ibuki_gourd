package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.text.TextStyle
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.DefaultTextStyle


val LocalTextStyle = compositionLocalOf(structuralEqualityPolicy()) { DefaultTextStyle }

@Composable
fun ProvideTextStyle(value: TextStyle, content: @Composable () -> Unit) {
    val mergedStyle = LocalTextStyle.current.merge(value)
    CompositionLocalProvider(LocalTextStyle provides mergedStyle, content = content)
}
