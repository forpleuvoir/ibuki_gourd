package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IGCompositionLocalProvider
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.nebula.config.item.ConfigList
import moe.forpleuvoir.nebula.config.item.ConfigMap

@Composable
fun StringListConfigWrapper(
    config: ConfigList<String>,
    header: @Composable RowScope.() -> Unit = {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(IGLang.Misc.content)
        }
    },
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier,
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
    dialogModifier: Modifier = Modifier,
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
    secondHead: @Composable BoxScope.() -> Unit = { Text(IGLang.ConfigWrapper.pairFirst) },
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier,
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
