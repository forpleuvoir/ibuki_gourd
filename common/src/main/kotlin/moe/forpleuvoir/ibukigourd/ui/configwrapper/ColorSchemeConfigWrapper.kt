package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.materialkolor.dynamicColorScheme
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.defaults.Add
import moe.forpleuvoir.ibukigourd.ui.icon.defaults.EditNote
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IGCompositionLocalProvider
import moe.forpleuvoir.ibukigourd.ui.preset.ColorPicker
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.ibukigourd.ui.util.toComposeColor
import moe.forpleuvoir.nebula.config.item.ConfigList
import moe.forpleuvoir.nebula.common.color.Color as NebulaColor

/**
 * 配色方案选择器：将 [ConfigList] 中的每个 [NebulaColor] 渲染为可点击的 4 色调色板预览，
 * 点击时触发 [onSelect] 回调。带编辑按钮，可打开 dialog 添加/删除颜色。
 *
 * @param config 预设 seed 颜色列表
 * @param selected 当前选中的 seed 色（用于高亮选中态）
 * @param isDark 当前是否为暗色模式，影响调色板生成
 * @param onSelect 选中某个颜色时的回调
 */
@Composable
fun ColorSchemeConfigWrapper(
    config: ConfigList<NebulaColor>,
    selected: NebulaColor,
    isDark: Boolean = false,
    onSelect: (NebulaColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors by config.asState()

    var showEditDialog by remember { mutableStateOf(false) }

    ConfigRowWrapper(
        config = config,
        modifier = modifier,
    ) {
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier.size(ConfigRowWrapper.entrySize),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier
                        .matchParentSize()
                        .horizontalScroll(scrollState)
                        .scrollable(
                            state = scrollState,
                            orientation = Orientation.Vertical,
                            reverseDirection = true,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    colors.forEach { color ->
                        PalettePreview(
                            seed = color,
                            isDark = isDark,
                            selected = color == selected,
                            onClick = { onSelect(color) },
                        )
                    }
                }
                // 滚动条
                if (scrollState.maxValue > 0) {
                    HorizontalScrollbar(
                        rememberScrollbarAdapter(scrollState),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .height(3.dp)
                            .padding(horizontal = 2.dp),
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { showEditDialog = true },
            ) {
                Icon(Icons.EditNote, IGLang.Misc.edit.plainText)
            }

        }
    }

    if (showEditDialog) {
        ColorSchemeEditDialog(
            config = config,
            isDark = isDark,
            selected = selected,
            onSelect = onSelect,
            onDismiss = { showEditDialog = false },
        )
    }
}

/**
 * 四色调色板圆形预览：整个预览是个大圆，内部 2×2 四色象限。
 */
@Composable
private fun PalettePreview(
    seed: NebulaColor,
    isDark: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scheme = remember(seed, isDark) {
        dynamicColorScheme(seedColor = seed.toComposeColor, isDark = isDark)
    }
    val colors = remember(scheme) {
        listOf(scheme.primary, scheme.secondary, scheme.tertiary, scheme.primaryContainer)
    }

    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (selected) 2.dp else 1.dp
    val size = 36.dp

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        // 四色象限：左上 primary，右上 secondary，左下 tertiary，右下 primaryContainer
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.weight(1f)) {
                Box(Modifier.weight(1f).fillMaxHeight().background(colors[0]))
                Box(Modifier.weight(1f).fillMaxHeight().background(colors[1]))
            }
            Row(Modifier.weight(1f)) {
                Box(Modifier.weight(1f).fillMaxHeight().background(colors[2]))
                Box(Modifier.weight(1f).fillMaxHeight().background(colors[3]))
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ColorSchemeEditDialog(
    config: ConfigList<NebulaColor>,
    isDark: Boolean,
    selected: NebulaColor,
    onSelect: (NebulaColor) -> Unit,
    onDismiss: () -> Unit,
) {
    val snapshot = remember { config.toList() }
    // 反应式订阅整个列表：增删改通过 observe 自动驱动，替代原轮询方式。
    val colors by config.asState()
    val itemCount = colors.size

    var showAddDialog by remember { mutableStateOf(false) }
    var editIndex by remember { mutableStateOf<Int?>(null) }
    var contextMenuIndex by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = {
            config.clear()
            snapshot.forEach { config.add(it) }
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(InlineStyleText(config.translateText.plainText)) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .fillMaxWidth()
                        .padding(bottom = 56.dp),
                ) {
                    if (itemCount == 0) {
                        Text(
                            "No colors",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            colors.forEachIndexed { index, color ->
                                Box {
                                    Box(
                                        modifier = Modifier
                                            .onPointerEvent(PointerEventType.Press) { event ->
                                                if (event.button == PointerButton.Secondary) {
                                                    contextMenuIndex = index
                                                }
                                            },
                                    ) {
                                        PalettePreview(
                                            seed = color,
                                            isDark = isDark,
                                            selected = color == selected,
                                            onClick = { onSelect(color) },
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = contextMenuIndex == index,
                                        onDismissRequest = { contextMenuIndex = null },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(IGLang.Misc.edit) },
                                            onClick = {
                                                contextMenuIndex = null
                                                editIndex = index
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text(IGLang.Misc.remove) },
                                            onClick = {
                                                config.removeAt(index)
                                                contextMenuIndex = null
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(40.dp),
                ) {
                    Icon(Icons.Add, IGLang.Misc.add.plainText)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(IGLang.Misc.confirm)
            }
        },
    )

    if (showAddDialog) {
        AddColorDialog(
            onConfirm = { color ->
                config.add(color)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }

    if (editIndex != null) {
        val idx = editIndex!!
        val currentColor = config.getOrNull(idx)
        if (currentColor != null) {
            AddColorDialog(
                initialColor = currentColor,
                title = IGLang.Misc.edit,
                onConfirm = { newColor ->
                    config[idx] = newColor
                    editIndex = null
                },
                onDismiss = { editIndex = null },
            )
        } else {
            editIndex = null
        }
    }
}

@Composable
private fun AddColorDialog(
    initialColor: NebulaColor = NebulaColor.fromRGB(0x5B7FFF),
    title: MutableText = IGLang.Misc.add,
    onConfirm: (NebulaColor) -> Unit,
    onDismiss: () -> Unit,
) {
    var pendingColor by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(title) },
        text = {
            IGCompositionLocalProvider {
                ColorPicker(
                    color = pendingColor,
                    onValueChange = { pendingColor = it },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pendingColor) }) {
                Text(IGLang.Misc.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(IGLang.Misc.cancel)
            }
        },
    )
}
