package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.EditNote
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IGCompositionLocalProvider
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.ConfigList
import moe.forpleuvoir.nebula.config.item.ConfigMap

@Composable
fun StringConfigWrapper(
    config: Config<String>,
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
                label = { androidx.compose.material3.Text("String") },
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
                title = { Text(InlineStyleText(config.translateText.plainText)) },
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
    header: @Composable RowScope.() -> Unit = {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(IGLang.Misc.content)
        }
    },
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier.padding(40.dp).size(1000.dp, 800.dp),
) {
    ListConfigWrapper(
        config = config,
        modifier = modifier,
        dialogModifier = dialogModifier,
        header = header,
        element = { index ->
            val state = rememberTextFieldState(config.getOrNull(index) ?: "")

            LaunchedEffect(config.getOrNull(index)) {
                val current = config.getOrNull(index) ?: ""
                if (state.text.toString() != current) {
                    state.setTextAndPlaceCursorAtEnd(current)
                }
            }

            LaunchedEffect(state.text) {
                val text = state.text.toString()
                if (index < config.size && config[index] != text) {
                    config[index] = text
                }
            }

            OutlinedTextField(
                state = state,
                lineLimits = TextFieldLineLimits.SingleLine,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp),
            )
        },
        addDialog = { onConfirm, onDismiss ->
            var newValue by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(IGLang.Misc.add) },
                text = {
                    IGCompositionLocalProvider {
                        OutlinedTextField(
                            value = newValue,
                            onValueChange = { newValue = it },
                            singleLine = true,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onConfirm(newValue) }) {
                        Text(IGLang.Misc.confirm)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(IGLang.Misc.cancel)
                    }
                },
            )
        },
    )
}

@Composable
fun StringMapConfigWrapper(
    config: ConfigMap<String>,
    keyHeader: @Composable () -> Unit = { Text(IGLang.ConfigWrapper.mapKey) },
    valueHeader: @Composable () -> Unit = { Text(IGLang.ConfigWrapper.mapValue) },
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier.padding(40.dp).size(1000.dp, 800.dp),
) {
    MapConfigWrapper(
        config = config,
        modifier = modifier,
        dialogModifier = dialogModifier,
        keyHeader = keyHeader,
        valueHeader = valueHeader,
        valueEditor = { key ->
            val state = rememberTextFieldState(config[key] ?: "")

            LaunchedEffect(key) {
                val current = config[key] ?: ""
                if (state.text.toString() != current) {
                    state.setTextAndPlaceCursorAtEnd(current)
                }
            }

            LaunchedEffect(state.text) {
                val text = state.text.toString()
                config[key] = text
            }

            OutlinedTextField(
                state = state,
                lineLimits = TextFieldLineLimits.SingleLine,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp),
            )
        },
        addDialog = { onConfirm, onDismiss ->
            var newKey by remember { mutableStateOf("") }
            var newValue by remember { mutableStateOf("") }
            val isDuplicate = newKey.isNotBlank() && config.containsKey(newKey)

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(IGLang.Misc.add) },
                text = {
                    IGCompositionLocalProvider {
                        Column {
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
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newValue,
                                onValueChange = { newValue = it },
                                singleLine = true,
                                label = { Text(IGLang.ConfigWrapper.mapValue) },
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { onConfirm(newKey, newValue) },
                        enabled = newKey.isNotBlank() && !config.containsKey(newKey),
                    ) {
                        Text(IGLang.Misc.confirm)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(IGLang.Misc.cancel)
                    }
                },
            )
        },
    )
}

@Composable
fun StringPairListConfigWrapper(
    config: ConfigList<Pair<String, String>>,
    firstHead: @Composable BoxScope.() -> Unit = { Text(IGLang.ConfigWrapper.pairFirst) },
    secondHead: @Composable BoxScope.() -> Unit = { Text(IGLang.ConfigWrapper.pairSecond) },
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier.padding(40.dp).size(1000.dp, 800.dp),
) {
    ListConfigWrapper(
        config = config,
        modifier = modifier,
        dialogModifier = dialogModifier,
        header = {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                firstHead()
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                secondHead()
            }
        },
        element = { index ->
            val (first, second) = config.getOrNull(index) ?: ("" to "")

            val firstState = rememberTextFieldState(first)
            val secondState = rememberTextFieldState(second)

            LaunchedEffect(first) {
                if (firstState.text.toString() != first) firstState.setTextAndPlaceCursorAtEnd(first)
            }
            LaunchedEffect(second) {
                if (secondState.text.toString() != second) secondState.setTextAndPlaceCursorAtEnd(second)
            }

            LaunchedEffect(firstState.text) {
                val text = firstState.text.toString()
                if (index < config.size && config[index].first != text) {
                    config[index] = text to config[index].second
                }
            }
            LaunchedEffect(secondState.text) {
                val text = secondState.text.toString()
                if (index < config.size && config[index].second != text) {
                    config[index] = config[index].first to text
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    state = firstState,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.weight(1f).height(44.dp),
                )
                OutlinedTextField(
                    state = secondState,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.weight(1f).height(44.dp),
                )
            }
        },
        addDialog = { onConfirm, onDismiss ->
            var newFirst by remember { mutableStateOf("") }
            var newSecond by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(IGLang.Misc.add) },
                text = {
                    IGCompositionLocalProvider {
                        Column {
                            OutlinedTextField(
                                value = newFirst,
                                onValueChange = { newFirst = it },
                                singleLine = true,
                                label = { Text(IGLang.ConfigWrapper.pairFirst) },
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newSecond,
                                onValueChange = { newSecond = it },
                                singleLine = true,
                                label = { Text(IGLang.ConfigWrapper.pairSecond) },
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onConfirm(newFirst to newSecond) }) {
                        Text(IGLang.Misc.confirm)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(IGLang.Misc.cancel)
                    }
                },
            )
        },
    )
}
