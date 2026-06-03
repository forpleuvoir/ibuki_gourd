package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftClipboard
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.nebula.config.item.ConfigList
import moe.forpleuvoir.nebula.config.item.ConfigMap

@Composable
fun StringListConfigWrapper(
    config: ConfigList<String>,
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier,
) {
    ListConfigWrapper(
        config = config,
        modifier = modifier,
        dialogModifier = dialogModifier,
        header = {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(IGLang.content)
            }
        },
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
                title = { Text(IGLang.add) },
                text = {
                    CompositionLocalProvider(LocalClipboard provides MinecraftClipboard) {
                        OutlinedTextField(
                            value = newValue,
                            onValueChange = { newValue = it },
                            singleLine = true,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onConfirm(newValue) }) {
                        Text(IGLang.confirm)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(IGLang.cancel)
                    }
                },
            )
        },
    )
}

@Composable
fun StringMapConfigWrapper(
    config: ConfigMap<String>,
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier,
) {
    MapConfigWrapper(
        config = config,
        modifier = modifier,
        dialogModifier = dialogModifier,
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
                title = { Text(IGLang.add) },
                text = {
                    CompositionLocalProvider(LocalClipboard provides MinecraftClipboard) {
                    Column {
                        OutlinedTextField(
                            value = newKey,
                            onValueChange = { newKey = it },
                            singleLine = true,
                            isError = isDuplicate,
                            label = {
                                if (isDuplicate) Text(IGLang.keyExists(newKey))
                                else Text(IGLang.mapKey)
                            },
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newValue,
                            onValueChange = { newValue = it },
                            singleLine = true,
                            label = { Text(IGLang.mapValue) },
                        )
                    }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { onConfirm(newKey, newValue) },
                        enabled = newKey.isNotBlank() && !config.containsKey(newKey),
                    ) {
                        Text(IGLang.confirm)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(IGLang.cancel)
                    }
                },
            )
        },
    )
}

@Composable
fun StringPairListConfigWrapper(
    config: ConfigList<Pair<String, String>>,
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier,
) {
    ListConfigWrapper(
        config = config,
        modifier = modifier,
        dialogModifier = dialogModifier,
        header = {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(IGLang.pairFirst)
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(IGLang.pairSecond)
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
                title = { Text(IGLang.add) },
                text = {
                    CompositionLocalProvider(LocalClipboard provides MinecraftClipboard) {
                        Column {
                            OutlinedTextField(
                                value = newFirst,
                                onValueChange = { newFirst = it },
                                singleLine = true,
                                label = { Text(IGLang.pairFirst) },
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newSecond,
                                onValueChange = { newSecond = it },
                                singleLine = true,
                                label = { Text(IGLang.pairSecond) },
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onConfirm(newFirst to newSecond) }) {
                        Text(IGLang.confirm)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(IGLang.cancel)
                    }
                },
            )
        },
    )
}
