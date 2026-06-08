package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.window.DialogProperties
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.Palette
import moe.forpleuvoir.ibukigourd.ui.platformcontext.CompositionTextContextProvider
import moe.forpleuvoir.nebula.common.color.Color


@Composable
fun ColorSettingButton(
    value: Color,
    onValueChange: (Color) -> Unit,
    title: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = IconButtonDefaults.standardShape,
    content: @Composable () -> Unit = {
        Icon(Icons.Palette, null)
    }
) {
    var showDialog by remember { mutableStateOf(false) }
    IconButton(
        onClick = { showDialog = !showDialog },
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        shape = shape,
        content = content
    )

    if (showDialog) {
        var state by remember { mutableStateOf(value) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = title,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            text = {
                CompositionTextContextProvider {
                    ColorPicker(value, {
                        state = it
                    })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(state)
                    showDialog = false
                }) {
                    Text(IGLang.Misc.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(IGLang.Misc.cancel)
                }
            }
        )
    }
}