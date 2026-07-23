package moe.forpleuvoir.ibukigourd.ui.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.ui.toast.ToastContainer

@Composable
internal fun OverlayContainer() {
    Box(Modifier.fillMaxSize()) {
        OverlayService.activeEntries.forEach { entry ->
            key(entry.key) {
                entry.content(this)
            }
        }
        ToastContainer()
    }
}
