package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.Compress
import moe.forpleuvoir.ibukigourd.ui.icon.default.EditNote
import moe.forpleuvoir.ibukigourd.ui.icon.default.Expand
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IGCompositionLocalProvider
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.ibukigourd.ui.preset.state.rememberTextFieldState
import moe.forpleuvoir.ibukigourd.ui.util.Keyed
import moe.forpleuvoir.ibukigourd.ui.util.copyValue
import moe.forpleuvoir.ibukigourd.ui.util.rememberKeyedList
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.ConfigList
import moe.forpleuvoir.nebula.config.item.ConfigMap

private const val StringContentEditorAnimationDuration = 300

@Composable
private fun RowScope.ExpandableStringContentEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.weight(1f),
) {
    val state = rememberTextFieldState(value, onValueChange = onValueChange)
    var expanded by remember { mutableStateOf(false) }
    val height by animateDpAsState(
        targetValue = if (expanded) 180.dp else 46.dp,
        animationSpec = tween(durationMillis = StringContentEditorAnimationDuration),
        label = "stringContentEditorHeight"
    )
    OutlinedTextField(
        state = state,
        lineLimits = if (expanded) TextFieldLineLimits.Default else TextFieldLineLimits.SingleLine,
        contentPadding = if (expanded) OutlinedTextFieldDefaults.contentPadding() else PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        trailingIcon = {
            IconButton({ expanded = !expanded }) {
                Crossfade(
                    targetState = expanded,
                    animationSpec = tween(durationMillis = StringContentEditorAnimationDuration),
                    label = "expandIcon"
                ) {
                    Icon(if (it) Icons.Compress else Icons.Expand, null)
                }
            }
        },
        modifier = modifier.height(height),
    )
}

@Composable
fun StringConfigWrapper(
    config: Config<String>,
    editorDialogTitle: @Composable (() -> Unit)? = {
        Text(InlineStyleText(config.translateText.plainText))
    },
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    val textFieldState = rememberTextFieldState(config.getValue())

    ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment, onReset = {
        textFieldState.setTextAndPlaceCursorAtEnd(config.getValue())
    }) {
        var showDialog by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.size(ConfigRowWrapper.entrySize),
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            LaunchedEffect(textFieldState.text) {
                config.setValue(textFieldState.text.toString())
            }

            OutlinedTextField(
                state = textFieldState,
                labelPosition = TextFieldLabelPosition.Attached(true),
                label = { Text("String") },
                lineLimits = TextFieldLineLimits.SingleLine,
                modifier = Modifier.weight(1f),
            )

            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.EditNote, IGLang.Misc.edit.plainText)
            }
        }
        if (showDialog) {
            val state = rememberTextFieldState(config.getValue())
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = editorDialogTitle,
                text = {
                    IGCompositionLocalProvider {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        ) {
                            val scrollState = rememberScrollState()
                            OutlinedTextField(
                                state = state,
                                scrollState = scrollState,
                                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                            )
                            VerticalScrollbar(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                adapter = rememberScrollbarAdapter(scrollState)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        config.setValue(state.text.toString())
                        textFieldState.setTextAndPlaceCursorAtEnd(state.text.toString())
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
}

@Composable
fun StringListConfigWrapper(
    config: ConfigList<String>,
    editorDialogTitle: @Composable (() -> Unit)? = {
        Text(InlineStyleText(config.translateText.plainText))
    },
    contentHeader: @Composable BoxScope.() -> Unit = {
        Text(IGLang.Misc.content)
    },
    addContentLabel: @Composable TextFieldLabelScope.(newContent: String) -> Unit = {
        Text(IGLang.Misc.content)
    },
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier.padding(40.dp).size(1000.dp, 800.dp),
) = ListConfigWrapperDefaults.run {
    var showEditDialog by remember { mutableStateOf(false) }
    RowWrapper(
        config = config,
        modifier = modifier
    ) { showEditDialog = true }
    if (showEditDialog) {
        val editingValue = rememberKeyedList(config)
        var nextKey by remember { mutableLongStateOf(editingValue.size.toLong()) }
        EditDialog(
            config = config,
            editingValue = editingValue,
            modifier = dialogModifier,
            title = editorDialogTitle,
            onDismissRequest = { showEditDialog = false },
            onConfirmRequest = {
                config.clear()
                it.forEach { (_, value) -> config.add(value) }
                true
            }
        ) {
            EditDialogContent(
                modifier = Modifier.fillMaxSize(),
                header = {
                    EditDialogContentHeader(
                        contentHeader = contentHeader
                    )
                },
                addDialog = { onDismissRequest ->
                    val newValue = rememberTextFieldState("")
                    AlertDialog(
                        onDismissRequest = onDismissRequest,
                        title = { Text(IGLang.Misc.add) },
                        text = {
                            IGCompositionLocalProvider {
                                OutlinedTextField(
                                    newValue,
                                    modifier = Modifier.fillMaxWidth().height(300.dp),
                                    label = { addContentLabel(newValue.text.toString()) },
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    editingValue.add(Keyed(nextKey++, newValue.text.toString()))
                                    onDismissRequest()
                                }
                            ) {
                                Text(IGLang.Misc.confirm)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = onDismissRequest) {
                                Text(IGLang.Misc.cancel)
                            }
                        },
                    )
                }
            ) { lazyListState ->
                EditDialogContentList(
                    data = editingValue,
                    key = { it.key },
                    modifier = Modifier,
                    lazyListState = lazyListState
                ) { keyed, onValueChange ->
                    ExpandableStringContentEditor(keyed.value, { onValueChange(keyed.copyValue(it)) })
                }
            }
        }
    }
}

@Composable
fun StringMapConfigWrapper(
    config: ConfigMap<String>,
    editorDialogTitle: @Composable (() -> Unit)? = {
        Text(InlineStyleText(config.translateText.plainText))
    },
    keyHeader: @Composable BoxScope.() -> Unit = { Text(IGLang.ConfigWrapper.mapKey) },
    addKeyLabel: @Composable TextFieldLabelScope.(isDuplicate: Boolean, newKey: String) -> Unit = { isDuplicate, newKey ->
        if (isDuplicate) Text(IGLang.ConfigWrapper.keyExists(newKey))
        else Text(IGLang.ConfigWrapper.mapKey)
    },
    valueHeader: @Composable BoxScope.() -> Unit = { Text(IGLang.ConfigWrapper.mapValue) },
    addValueLabel: @Composable TextFieldLabelScope.(newValue: String) -> Unit = {
        Text(IGLang.ConfigWrapper.mapValue)
    },
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier.padding(40.dp).size(1000.dp, 800.dp),
) = MapConfigWrapperDefaults.run {
    var showEditDialog by remember { mutableStateOf(false) }
    RowWrapper(
        config = config,
        modifier = modifier,
    ) { showEditDialog = true }

    if (showEditDialog) {
        EditDialog(
            config = config,
            modifier = dialogModifier,
            title = editorDialogTitle,
            onDismissRequest = { showEditDialog = false },
        ) { data ->
            var nextKey by remember { mutableLongStateOf(data.size.toLong()) }
            EditDialogContent(
                modifier = Modifier.fillMaxSize(),
                header = {
                    EditDialogContentHeader(
                        keyHeader = keyHeader,
                        valueHeader = valueHeader
                    )
                },
                addDialog = { onDismissRequest ->
                    val newKey = rememberTextFieldState("")
                    val isDuplicate = remember(newKey.text.toString()) { data.any { it.value.key == newKey.text.toString() } }
                    val newValue = rememberTextFieldState("")
                    AlertDialog(
                        onDismissRequest = onDismissRequest,
                        title = { Text(IGLang.Misc.add) },
                        text = {
                            IGCompositionLocalProvider {
                                Column {
                                    OutlinedTextField(
                                        state = newKey,
                                        lineLimits = TextFieldLineLimits.SingleLine,
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = isDuplicate,
                                        label = {
                                            addKeyLabel(isDuplicate, newKey.text.toString())
                                        },
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        newValue,
                                        modifier = Modifier.fillMaxWidth().height(240.dp),
                                        label = { addValueLabel(newValue.text.toString()) },
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (!isDuplicate) {
                                        data.add(
                                            Keyed(
                                                nextKey++,
                                                MapEntry(newKey.text.toString(), newValue.text.toString())
                                            )
                                        )
                                        onDismissRequest()
                                    }
                                },
                                enabled = !isDuplicate
                            ) {
                                Text(IGLang.Misc.confirm)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = onDismissRequest) {
                                Text(IGLang.Misc.cancel)
                            }
                        },
                    )
                }
            ) { lazyListState ->
                EditDialogContentList(
                    data = data,
                    modifier = Modifier,
                    lazyListState = lazyListState,
                ) { value, onValueChange ->
                    Row(Modifier.weight(LocalValueColumnWeight.current)) {
                        ExpandableStringContentEditor(value, onValueChange)
                    }
                }
            }
        }
    }
}

@Composable
fun StringPairListConfigWrapper(
    config: ConfigList<Pair<String, String>>,
    editorDialogTitle: @Composable (() -> Unit)? = {
        Text(InlineStyleText(config.translateText.plainText))
    },
    firstHead: @Composable BoxScope.() -> Unit = { Text(IGLang.ConfigWrapper.pairFirst) },
    secondHead: @Composable BoxScope.() -> Unit = { Text(IGLang.ConfigWrapper.pairSecond) },
    addFirstLabel: @Composable TextFieldLabelScope.(newFirst: String) -> Unit = {
        Text(IGLang.ConfigWrapper.pairFirst)
    },
    addSecondLabel: @Composable TextFieldLabelScope.(newSecond: String) -> Unit = {
        Text(IGLang.ConfigWrapper.pairSecond)
    },
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier.padding(40.dp).size(1000.dp, 800.dp),
) = ListConfigWrapperDefaults.run {
    var showEditDialog by remember { mutableStateOf(false) }
    RowWrapper(
        config = config,
        modifier = modifier
    ) { showEditDialog = true }
    if (showEditDialog) {
        val editingValue = rememberKeyedList(config)
        var nextKey by remember { mutableLongStateOf(editingValue.size.toLong()) }
        EditDialog(
            config = config,
            editingValue = editingValue,
            modifier = dialogModifier,
            title = editorDialogTitle,
            onDismissRequest = { showEditDialog = false },
            onConfirmRequest = {
                config.clear()
                it.forEach { (_, value) -> config.add(value) }
                true
            }
        ) {
            EditDialogContent(
                modifier = Modifier.fillMaxSize(),
                header = {
                    EditDialogContentHeader(
                        contentHeader = {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(LocalColumnSpacing.current)) {
                                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    firstHead()
                                }
                                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    secondHead()
                                }
                            }
                        }
                    )
                },
                addDialog = { onDismissRequest ->
                    val newFirst = rememberTextFieldState("")
                    val newSecond = rememberTextFieldState("")
                    AlertDialog(
                        onDismissRequest = onDismissRequest,
                        title = { Text(IGLang.Misc.add) },
                        text = {
                            IGCompositionLocalProvider {
                                Column {
                                    OutlinedTextField(
                                        newFirst,
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { addFirstLabel(newFirst.text.toString()) },
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        newSecond,
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { addSecondLabel(newSecond.text.toString()) },
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    editingValue.add(Keyed(nextKey++, (newFirst.text.toString() to newSecond.text.toString())))
                                    onDismissRequest()
                                }
                            ) {
                                Text(IGLang.Misc.confirm)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = onDismissRequest) {
                                Text(IGLang.Misc.cancel)
                            }
                        },
                    )
                }
            ) { lazyListState ->
                EditDialogContentList(
                    data = editingValue,
                    key = { it.key },
                    modifier = Modifier,
                    lazyListState = lazyListState,
                ) { (key, value), onValueChange ->
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(LocalColumnSpacing.current)) {
                        ExpandableStringContentEditor(value.first, { onValueChange(Keyed(key, (it to value.second))) })
                        ExpandableStringContentEditor(value.second, { onValueChange(Keyed(key, (value.first to it))) })
                    }
                }
            }
        }
    }
}
