package moe.forpleuvoir.ibukigourd.ui.toast

import androidx.compose.runtime.Composable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Toast(
    val content: @Composable () -> Unit,
    val duration: Duration = 2.seconds,
    val tag: String? = null,
    val animation: ToastAnimation? = null
)
