package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.Delete
import moe.forpleuvoir.ibukigourd.ui.preset.state.isQuickAction

@Composable
fun RemoveButton(
    modifier: Modifier = Modifier,
    action: () -> Unit,
) {
    IconButton(onClick = action, modifier = modifier) {
        Icon(Icons.Delete, IGLang.Misc.remove.plainText, Modifier.size(24.dp))
    }
}

@Composable
fun RemoveConfirmButton(
    message: String,
    action: () -> Unit,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null,
) {
    var showDialog by remember { mutableStateOf(false) }
    IconButton(onClick = {
        if (isQuickAction) {
            action()
        } else showDialog = true
    }, modifier = modifier) {
        Icon(Icons.Delete, IGLang.Misc.remove.plainText, Modifier.size(24.dp))
    }

    if (showDialog) {
        SimpleAlertDialog(
            onDismissRequest = { showDialog = false },
            onConfirmRequest = { true },
            title = {
                Text(IGLang.Misc.removeConfirm(message))
            },
            content = content,
            confirmButton = {
                TextButton(
                    onClick = {
                        action()
                        showDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(IGLang.Misc.confirm)
                }
            }
        )
    }
}