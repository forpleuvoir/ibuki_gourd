package moe.forpleuvoir.ibukigourd.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

data class Keyed<T>(
    val key: Long,
    val value: T
)

fun <T> List<Keyed<T>>.values(): List<T> =
    map { it.value }

fun <T> Keyed<T>.copyValue(
    value: T,
): Keyed<T> = copy(value = value)

@Composable
fun <T> rememberKeyedList(
    list: List<T>,
    key: Any? = list,
): SnapshotStateList<Keyed<T>> = remember(key) {
    mutableStateListOf<Keyed<T>>().apply {
        var nextKey = 0L
        list.forEach { item ->
            add(Keyed(nextKey++, item))
        }
    }
}
