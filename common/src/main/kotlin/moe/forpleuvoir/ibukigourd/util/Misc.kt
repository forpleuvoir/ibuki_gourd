package moe.forpleuvoir.ibukigourd.util

import moe.forpleuvoir.ibukigourd.IbukiGourd
import net.minecraft.resources.Identifier
import java.util.*
import kotlin.reflect.KClass

fun Any.logger(modName: String): ModLogger {
    return ModLogger(this::class, modName)
}

internal fun Any.logger(): ModLogger {
    return ModLogger(this::class, IbukiGourd.MOD_NAME)
}

internal fun logger(kClass: KClass<*>): ModLogger {
    return ModLogger(kClass::class, IbukiGourd.MOD_NAME)
}

internal fun logger(name: String): ModLogger {
    return ModLogger(name, IbukiGourd.MOD_NAME)
}

fun identifier(namespace: String, path: String): Identifier = Identifier.fromNamespaceAndPath(namespace, path)

internal fun identifier(path: String) = identifier(IbukiGourd.MOD_ID, path)

fun <T> List<T>.exactMatch(list: List<T>, contrast: (T, T) -> Boolean = { a, b -> a == b }): Boolean {
    return if (this.size == list.size) {
        this.forEachIndexed { index, obj ->
            if (!contrast(list[index]!!, obj)) {
                return false
            }
        }
        true
    } else false
}

fun String.truncate(maxLength: Int, suffix: String = "..."): String {
    if (this.length <= maxLength) return this

    // 确保截断长度大于0，避免 suffix 比 maxLength 还长导致报错
    val truncateLength = maxLength - suffix.length
    return if (truncateLength > 0) {
        this.take(truncateLength) + suffix
    } else {
        this.take(maxLength)
    }
}

fun <T> List<T>.moveElement(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex == toIndex) return this
    val mutable = toMutableList()
    val element = mutable.removeAt(fromIndex)
    mutable.add(toIndex, element)
    return mutable
}

fun <T> MutableList<T>.moveElement(fromIndex: Int, toIndex: Int) {
    if (fromIndex == toIndex) return
    val movingElement = this.removeAt(fromIndex)
    this.add(toIndex, movingElement)
}

fun <T> Iterable<T>.forEachWithLimit(limit: Int, action: (T) -> Unit) {
    for ((count, element) in this.withIndex()) {
        if (count >= limit) break
        action(element)
    }
}

fun <T> Iterable<T>.forEachWithLimitIndexed(limit: Int, action: (Int, T) -> Unit) {
    var count = 0
    for ((index, element) in this.withIndex()) {
        if (count >= limit) break
        action(index, element)
        count++
    }
}

fun <K, V> Map<K, V>.forEachWithLimit(limit: Int, action: (K, V) -> Unit) {
    var count = 0
    for (element in this) {
        if (count >= limit) break
        action(element.key, element.value)
        count++
    }
}

fun <K, V> MutableMap<K, V>.renameKey(oldKey: K, newKey: K) {
    if (this is SequencedMap) {
        this.renameKey(oldKey, newKey)
    } else {
        this[oldKey]?.let { value ->
            this.remove(oldKey)
            this[newKey] = value
        }
    }
}

fun <K, V> SequencedMap<K, V>.renameKey(oldKey: K, newKey: K) {
    val iterator = this.entries.iterator()
    val newMap = LinkedHashMap<K, V>()

    while (iterator.hasNext()) {
        val entry = iterator.next()
        if (entry.key == oldKey) {
            newMap[newKey] = entry.value
        } else {
            newMap[entry.key] = entry.value
        }
    }

    this.clear()
    this.putAll(newMap)
}