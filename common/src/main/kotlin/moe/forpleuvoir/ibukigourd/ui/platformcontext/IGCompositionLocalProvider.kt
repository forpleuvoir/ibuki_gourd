package moe.forpleuvoir.ibukigourd.ui.platformcontext

import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalLocalization
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.ui.toast.ToastHandler

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun IGCompositionLocalProvider(
    vararg values: ProvidedValue<*>,
    noinline content: @Composable () -> Unit
) = CompositionLocalProvider(
    *values,
    LocalClipboard provides MinecraftClipboard,
    LocalLocalization provides MinecraftPlatformLocalization,
    LocalContextMenuRepresentation provides Material3ContextMenuRepresentation,
    content = content
)

@Composable
fun IbukiGourdTheme(
    colorScheme: ColorScheme = IGConfig.Gui.Theme.colorScheme,
    shapes: Shapes = MaterialTheme.shapes,
    typography: Typography = MaterialTheme.typography,
    content: @Composable () -> Unit,
) {
    DisposableEffect(colorScheme) {
        ToastHandler.enter(colorScheme)
        onDispose { ToastHandler.leave(colorScheme) }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        typography = typography
    ) {
        CompositionLocalProvider(
            LocalScrollbarStyle provides defaultScrollbarStyle().copy(
                hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
            ),
            content = content
        )
    }
}