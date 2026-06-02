package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.window.DialogProperties
import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftClipboard
import moe.forpleuvoir.nebula.common.color.Color


@Composable
fun ColorSettingButton(
    value: Color,
    onValueChange: (Color) -> Unit,
    title: @Composable (() -> Unit)? = null,
    label: @Composable ((Color) -> Unit) = {
        Text(value.hexStr, style = MaterialTheme.typography.labelSmall)
    },
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = AssistChipDefaults.shape,
    colors: ChipColors = AssistChipDefaults.assistChipColors(),
    elevation: ChipElevation? = AssistChipDefaults.assistChipElevation(),
    border: BorderStroke? = AssistChipDefaults.assistChipBorder(enabled),
    horizontalArrangement: Arrangement.Horizontal = AssistChipDefaults.horizontalArrangement(),
    contentPadding: PaddingValues = AssistChipDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    AssistChip(
        onClick = {
            showDialog = true
        },
        { label(value) },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        horizontalArrangement = horizontalArrangement,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    )
    if (showDialog) {
        var state by remember { mutableStateOf(value) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = title,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            text = {
                CompositionLocalProvider(LocalClipboard provides MinecraftClipboard) {
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
                    Text(IGLang.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(IGLang.cancel)
                }
            }
        )
    }
}