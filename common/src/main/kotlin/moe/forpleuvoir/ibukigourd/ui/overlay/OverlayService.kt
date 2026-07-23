package moe.forpleuvoir.ibukigourd.ui.overlay

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import java.util.concurrent.atomic.AtomicLong

fun interface OverlayHandle {
    fun unregister()
}

internal class OverlayEntry(
    val key: Any,
    val token: Any,
    val content: @Composable BoxScope.() -> Unit
)

object OverlayService {

    internal val activeEntries: List<OverlayEntry>
        field = mutableStateListOf()

    private val tokenCounter = AtomicLong()

    fun register(
        key: Any,
        content: @Composable BoxScope.() -> Unit
    ): OverlayHandle {
        val token = tokenCounter.incrementAndGet()
        val index = activeEntries.indexOfFirst { it.key == key }
        val entry = OverlayEntry(key, token, content)
        if (index >= 0) {
            activeEntries[index] = entry
        } else {
            activeEntries.add(entry)
        }
        return OverlayHandle { unregisterByToken(key, token) }
    }

    fun register(
        content: @Composable BoxScope.() -> Unit
    ): OverlayHandle {
        val token = tokenCounter.incrementAndGet()
        val identityKey = AutoKey(token)
        activeEntries.add(OverlayEntry(identityKey, token, content))
        return OverlayHandle { unregisterByToken(identityKey, token) }
    }

    fun unregister(key: Any): Boolean {
        return activeEntries.removeAll { it.key == key }
    }

    fun contains(key: Any): Boolean {
        return activeEntries.any { it.key == key }
    }

    fun clear() {
        activeEntries.clear()
    }

    private fun unregisterByToken(key: Any, token: Any) {
        activeEntries.removeAll { it.key == key && it.token == token }
    }

    private class AutoKey(val token: Any) {
        override fun equals(other: Any?): Boolean = other is AutoKey && token == other.token
        override fun hashCode(): Int = token.hashCode()
    }
}
