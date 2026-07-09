package moe.forpleuvoir.ibukigourd.ui.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Stable
class PopupHostState {

    private val _entries = mutableStateMapOf<Any, PopupEntry>()

    val entries: Map<Any, PopupEntry> get() = _entries

    fun show(entry: PopupEntry) {
        _entries[entry.key] = entry
    }

    fun hide(key: Any) {
        _entries.remove(key)
    }
}

data class PopupEntry(
    val key: Any,
    val positionProvider: PopupPositionProvider,
    val onDismissRequest: (() -> Unit)?,
    val properties: PopupProperties,
    val content: @Composable () -> Unit,
)

val LocalPopupHost = staticCompositionLocalOf<PopupHostState?> { null }

@Composable
fun PopupHostOverlay() {
    val state = LocalPopupHost.current ?: return
    for (entry in state.entries.values) {
        key(entry.key) {
            Popup(
                popupPositionProvider = entry.positionProvider,
                onDismissRequest = entry.onDismissRequest,
                properties = entry.properties,
            ) {
                entry.content()
            }
        }
    }
}
