package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.Add
import moe.forpleuvoir.ibukigourd.ui.icon.default.Delete
import moe.forpleuvoir.ibukigourd.ui.icon.default.DragIndicator
import moe.forpleuvoir.ibukigourd.ui.icon.default.EditNote
import moe.forpleuvoir.ibukigourd.ui.preset.FlexibleDialog
import moe.forpleuvoir.ibukigourd.ui.preset.RemoveConfirmButton
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.fabVisibilityAnimation
import moe.forpleuvoir.ibukigourd.ui.preset.state.rememberScrollFabVisibilityProgress
import moe.forpleuvoir.nebula.config.item.ConfigList

@Composable
fun <E : Any> ListConfigWrapper(
    config: ConfigList<E>,
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier,
    header: @Composable RowScope.() -> Unit = {},
    element: @Composable RowScope.(index: Int) -> Unit,
    addDialog: @Composable (onConfirm: (E) -> Unit, onDismiss: () -> Unit) -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val displaySize by config.asDerivedState { it.size }

    ConfigRowWrapper(config = config, modifier = modifier) {
        Row(
            modifier = Modifier.size(ConfigRowWrapper.entrySize),
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AssistChip(
                {},
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(IGLang.ConfigWrapper.listConfigWrapperText(displaySize), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                },
                modifier = Modifier.weight(1f).height(40.dp)
            )
            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.EditNote, IGLang.Misc.edit.plainText)
            }
        }
    }

    if (showAddDialog) {
        addDialog(
            { value -> config.add(value); showAddDialog = false },
            { showAddDialog = false },
        )
    }

    if (showEditDialog) {
        val snapshot = remember { config.toList() }
        EditDialog(
            config = config,
            header = header,
            element = element,
            dialogModifier = dialogModifier,
            onAddClick = { showAddDialog = true },
            onConfirm = { showEditDialog = false },
            onCancel = {
                config.clear()
                snapshot.forEach { config.add(it) }
                showEditDialog = false
            },
        )
    }
}

@Composable
private fun <E : Any> EditDialog(
    config: ConfigList<E>,
    header: @Composable RowScope.() -> Unit,
    element: @Composable RowScope.(index: Int) -> Unit,
    dialogModifier: Modifier = Modifier,
    onAddClick: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val itemCount by config.asDerivedState { it.size }
    FlexibleDialog(
        onDismissRequest = onCancel,
        onConfirmRequest = { onConfirm(); false },
        modifier = dialogModifier,
        title = { Text(InlineStyleText(config.translateText.plainText)) },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val scrollState = rememberScrollState()

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.width(32.dp), contentAlignment = Alignment.Center) {
                            Text(IGLang.ConfigWrapper.move, style = MaterialTheme.typography.labelSmall)
                        }
                        header()
                        Box(Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                            Text(IGLang.Misc.remove, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    HorizontalDivider()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(scrollState)
                                .fillMaxHeight()
                                .padding(top = 4.dp, bottom = 56.dp),
                        ) {
                            ReorderableItemList(
                                itemCount = itemCount,
                                onMove = { from, to ->
                                    val item = config.removeAt(from)
                                    config.add(to, item)
                                },
                            ) { index, isDragging, dragModifier ->
                                val handleInteraction = remember { MutableInteractionSource() }
                                val handleHovered by handleInteraction.collectIsHoveredAsState()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        dragModifier
                                            .hoverable(handleInteraction)
                                            .pointerHoverIcon(PointerIcon.Hand)
                                            .background(
                                                if (handleHovered || isDragging) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                                                CircleShape,
                                            ).padding(4.dp)
                                    ) {
                                        Icon(Icons.DragIndicator, contentDescription = null)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
                                    ) {
                                        element(index)
                                    }

                                    RemoveConfirmButton("${config.translateText.plainText}[$index]", { config.removeAt(index) })
                                }
                            }
                        }

                        VerticalScrollbar(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(scrollState)
                        )
                    }
                }

                FloatingActionButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(40.dp)
                        .fabVisibilityAnimation(rememberScrollFabVisibilityProgress(scrollState))
                ) {
                    Icon(Icons.Add, IGLang.Misc.add.plainText)
                }
            }
        }
    )
}
