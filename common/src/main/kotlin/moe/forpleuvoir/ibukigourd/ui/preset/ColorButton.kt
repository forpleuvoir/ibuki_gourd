package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.Palette
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IGCompositionLocalProvider
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftClipboard
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.background
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.plainTooltip
import moe.forpleuvoir.ibukigourd.ui.toast.ToastContent
import moe.forpleuvoir.ibukigourd.ui.toast.ToastHandler
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import moe.forpleuvoir.nebula.common.color.Color


@Composable
fun ColorAssistChipOuterSetting(
    value: Color,
    onValueChange: (Color) -> Unit,
    editorTitle: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    chipModifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp, Alignment.End),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    Row(
        modifier,
        horizontalArrangement,
        verticalAlignment
    ) {
        AssistChip(
            modifier = chipModifier.plainTooltip {
                Text(IGLang.Color.clickCopyColor(value))
            },
            onClick = {
                MinecraftClipboard.setClipboardText(value.hexStr)
                ToastHandler.show {
                    ToastContent { Text(IGLang.Color.copyColorSuccess(value)) }
                }
            },
            leadingIcon = {
                ColorDisplayer(value)
            },
            label = {
                Text("#%08X".format(value.argb), fontFamily = FontFamily.Monospace)
            }
        )
        ColorSettingButton(value, onValueChange, title = editorTitle)
    }
}

@Composable
fun ColorAssistChip(
    value: Color,
    onValueChange: (Color) -> Unit,
    editorTitle: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AssistChip(
        modifier = modifier.plainTooltip {
            Text(IGLang.Color.clickCopyColor(value))
        },
        onClick = {
            MinecraftClipboard.setClipboardText(value.hexStr)
            ToastHandler.show {
                ToastContent { Text(IGLang.Color.copyColorSuccess(value)) }
            }
        },
        leadingIcon = {
            ColorDisplayer(value)
        },
        trailingIcon = {
            ColorSettingButton(value, onValueChange, title = editorTitle)
        },
        label = {
            Text("#%08X".format(value.argb), fontFamily = FontFamily.Monospace)
        }
    )
}

@Composable
private fun ColorDisplayer(color: Color) {
    val shape = MaterialTheme.shapes.extraSmall
    var titleSize by remember { mutableStateOf(5.dp) }
    val density = LocalDensity.current
    Box(
        modifier = Modifier.padding(vertical = 8.dp).size(28.dp)
            .onSizeChanged { size ->
                titleSize = with(density) { (size.height / 3).toDp() }
            }
            .border(0.5.dp, color.reverse(false).toComposeColor, shape)
    ) {
        Checkerboard(titleSize, modifier = Modifier.fillMaxSize().clip(shape))
        Box(Modifier.fillMaxSize().background(color, shape))
    }
}

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
        var editingColor by remember { mutableStateOf(value) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = title,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            text = {
                IGCompositionLocalProvider {
                    ColorPicker(editingColor, {
                        editingColor = it
                    })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(editingColor)
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