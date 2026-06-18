package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.icon.*
import moe.forpleuvoir.ibukigourd.ui.icon.default.Add
import moe.forpleuvoir.ibukigourd.ui.icon.default.Delete
import moe.forpleuvoir.ibukigourd.ui.icon.default.DragIndicator
import moe.forpleuvoir.ibukigourd.ui.icon.default.EditNote
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IGCompositionLocalProvider
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.nebula.config.item.ConfigMap

@Composable
fun <V : Any> MapConfigWrapper(
    config: ConfigMap<V>,
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    keyHeader: @Composable () -> Unit = { Text(IGLang.ConfigWrapper.mapKey) },
    valueHeader: @Composable () -> Unit = { Text(IGLang.ConfigWrapper.mapValue) },
    valueEditor: @Composable (key: String) -> Unit,
    addDialog: @Composable (onConfirm: (key: String, value: V) -> Unit, onDismiss: () -> Unit) -> Unit,
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
                        Text(IGLang.ConfigWrapper.mapConfigWrapperText(displaySize), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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
            { key, value -> config[key] = value; showAddDialog = false },
            { showAddDialog = false },
        )
    }

    if (showEditDialog) {
        val snapshot = remember { config.entries.associate { it.key to it.value } }

        MapEditDialog(
            config = config,
            keyHeader = keyHeader,
            valueHeader = valueHeader,
            valueEditor = valueEditor,
            dialogModifier = dialogModifier,
            properties = properties,
            onAddClick = { showAddDialog = true },
            onConfirm = { showEditDialog = false },
            onCancel = {
                config.clear()
                snapshot.forEach { (k, v) -> config[k] = v }
                showEditDialog = false
            },
        )
    }
}

@Composable
private fun <V : Any> MapEditDialog(
    config: ConfigMap<V>,
    keyHeader: @Composable () -> Unit,
    valueHeader: @Composable () -> Unit,
    valueEditor: @Composable (key: String) -> Unit,
    dialogModifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
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
        properties = properties,
        title = { Text(InlineStyleText(config.translateText.plainText)) },
        text = {
            IGCompositionLocalProvider {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(min = 640.dp)
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
                                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { keyHeader() }
                                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { valueHeader() }
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
                                        val entries = config.entries.toMutableList()
                                        val moved = entries.removeAt(from)
                                        entries.add(to, moved)
                                        config.clear()
                                        entries.forEach { (k, v) -> config[k] = v }
                                        configVersion++
                                    },
                                ) { index, dragModifier ->
                                    val handleInteraction = remember { MutableInteractionSource() }
                                    val handleHovered by handleInteraction.collectIsHoveredAsState()
                                    val entries = config.entries.toList()
                                    val entry = entries.getOrNull(index) ?: return@ReorderableItemList
                                    val currentKey = entry.key
                                    var showKeyEditDialog by remember { mutableStateOf(false) }

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
                                        OutlinedButton(
                                            onClick = { showKeyEditDialog = true },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(4.dp),
                                        ) {
                                            Text(Literal(currentKey), maxLines = 1)
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Box(modifier = Modifier.weight(1f)) {
                                            valueEditor(currentKey)
                                        }
                                        IconButton(onClick = {
                                            config.remove(currentKey)
                                            configVersion++
                                        }) {
                                            Icon(Icons.Delete, IGLang.Misc.remove.plainText, Modifier.size(24.dp))
                                        }

                                        if (showKeyEditDialog) {
                                            var newKey by remember { mutableStateOf(currentKey) }
                                            val isDuplicate = newKey.isNotBlank() && newKey != currentKey && config.containsKey(newKey)
                                            AlertDialog(
                                                onDismissRequest = { showKeyEditDialog = false },
                                                properties = DialogProperties(usePlatformDefaultWidth = false),
                                                title = { Text(IGLang.Misc.edit) },
                                                text = {
                                                    IGCompositionLocalProvider {
                                                        OutlinedTextField(
                                                            value = newKey,
                                                            onValueChange = { newKey = it },
                                                            singleLine = true,
                                                            isError = isDuplicate,
                                                            label = {
                                                                if (isDuplicate) Text(IGLang.ConfigWrapper.keyExists(newKey))
                                                                else Text(IGLang.ConfigWrapper.mapKey)
                                                            },
                                                        )
                                                    }
                                                },
                                                confirmButton = {
                                                    TextButton(
                                                        onClick = {
                                                            if (newKey.isNotBlank() && newKey != currentKey && !config.containsKey(newKey)) {
                                                                val v = config.remove(currentKey)
                                                                if (v != null) {
                                                                    config[newKey] = v
                                                                    configVersion++
                                                                }
                                                            }
                                                            showKeyEditDialog = false
                                                        },
                                                        enabled = newKey.isNotBlank() && (newKey == currentKey || !config.containsKey(newKey)),
                                                    ) {
                                                        Text(IGLang.Misc.confirm)
                                                    }
                                                },
                                                dismissButton = {
                                                    TextButton(onClick = { showKeyEditDialog = false }) {
                                                        Text(IGLang.Misc.cancel)
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            }

                            VerticalScrollbar(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight(),
                                adapter = rememberScrollbarAdapter(scrollState),
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
