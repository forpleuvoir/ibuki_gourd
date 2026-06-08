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
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.icon.Add
import moe.forpleuvoir.ibukigourd.ui.icon.Delete
import moe.forpleuvoir.ibukigourd.ui.icon.DragIndicator
import moe.forpleuvoir.ibukigourd.ui.icon.EditNote
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.platformcontext.CompositionTextContextProvider
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftClipboard
import moe.forpleuvoir.ibukigourd.ui.preset.Text
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

    var displaySize by remember { mutableStateOf(config.size) }
    val pollInterval = ConfigRowWrapper.valuePollInterval
    LaunchedEffect(config) {
        while (isActive) {
            delay(pollInterval)
            displaySize = config.size
        }
    }

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
    var itemCount by remember { mutableStateOf(config.size) }
    var configVersion by remember { mutableStateOf(0) }

    val pollInterval = ConfigRowWrapper.valuePollInterval
    LaunchedEffect(config) {
        while (isActive) {
            delay(pollInterval)
            if (itemCount != config.size) {
                itemCount = config.size
                configVersion++
            }
        }
    }

    AlertDialog(
        onDismissRequest = onCancel,
        modifier = dialogModifier,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(config.translateText) },
        text = {
            CompositionTextContextProvider {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp),
                ) {
                    val scrollState = rememberScrollState()

                    Column(modifier = Modifier.fillMaxSize()) {
                        if (itemCount > 0) {
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
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .verticalScroll(scrollState)
                                    .padding(top = 4.dp, bottom = 56.dp),
                            ) {
                                ReorderableItemList(
                                    itemCount = itemCount,
                                    version = configVersion,
                                    onMove = { from, to ->
                                        val item = config.removeAt(from)
                                        config.add(to, item)
                                        configVersion++
                                    },
                                ) { index, dragModifier ->
                                    val handleInteraction = remember { MutableInteractionSource() }
                                    val handleHovered by handleInteraction.collectIsHoveredAsState()
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            Icons.DragIndicator,
                                            contentDescription = null,
                                            modifier = dragModifier
                                                .hoverable(handleInteraction)
                                                .background(
                                                    if (handleHovered) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                                                    CircleShape,
                                                )
                                                .size(24.dp),
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
                                        ) {
                                            element(index)
                                        }
                                        IconButton(onClick = {
                                            config.removeAt(index)
                                            configVersion++
                                        }) {
                                            Icon(Icons.Delete, IGLang.Misc.remove.plainText, Modifier.size(24.dp))
                                        }
                                    }
                                }
                            }

                            VerticalScrollbar(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight(),
                                adapter = rememberScrollbarAdapter(scrollState),
                                style = defaultScrollbarStyle().copy(
                                    hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                ),
                            )
                        }
                    }

                    FloatingActionButton(
                        onClick = onAddClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(40.dp),
                    ) {
                        Icon(Icons.Add, IGLang.Misc.add.plainText)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(IGLang.Misc.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(IGLang.Misc.cancel)
            }
        },
    )
}
