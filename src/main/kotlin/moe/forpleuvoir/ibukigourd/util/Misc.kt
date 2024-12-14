@file:Suppress("unused")
@file:OptIn(ExperimentalContracts::class)

package moe.forpleuvoir.ibukigourd.util

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.reflect.ClassPath
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.util.Identifier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass

typealias Tick = Long

val ARGBColor?.isNull: Boolean
    get() = this == null || this.alpha == 0

val ARGBColor?.get: ARGBColor
    get() = this ?: Colors.BLACK.alpha(0)

fun Any.logger(modName: String): ModLogger {
    return ModLogger(this::class, modName)
}

internal fun Any.logger(): ModLogger {
    return ModLogger(this::class, IbukiGourd.MOD_NAME)
}

val loader: FabricLoader by lazy { FabricLoader.getInstance() }

val isDevEnv: Boolean by lazy { loader.isDevelopmentEnvironment }
fun identifier(nameSpace: String, path: String): Identifier = Identifier.of(nameSpace, path)
internal fun identifier(path: String): Identifier = identifier(IbukiGourd.MOD_ID, path)

inline fun isDevEnv(block: () -> Unit) {
    contract {
        callsInPlace(block, kotlin.contracts.InvocationKind.AT_MOST_ONCE)
    }
    if (isDevEnv) block()
}

/**
 * Returns the number of decimal places in a Float value.
 *
 * @return The number of decimal places.
 */
val Float.decimalPlaces: Int
    get() {
        val decimalString = this.toString()
        val dotIndex = decimalString.indexOf('.')

        return if (dotIndex == -1) {
            0 // 没有小数点，返回0
        } else {
            decimalString.length - dotIndex - 1
        }
    }

/**
 * Calculates the number of decimal places in a Double.
 *
 * @return The number of decimal places in the Double.
 * If the Double has no decimal part, returns 0.
 *
 */
val Double.decimalPlaces: Int
    get() {
        val decimalString = this.toString()
        val dotIndex = decimalString.indexOf('.')

        return if (dotIndex == -1) {
            0 // 没有小数点，返回0
        } else {
            decimalString.length - dotIndex - 1
        }
    }

@JvmName("measureTime")
fun javaMeasureTime(block: Runnable) =
    kotlin.time.measureTime(block::run).let { it.toString() to it.inWholeNanoseconds }


fun scanModPackage(predicate: (KClass<*>) -> Boolean = { true }): Map<ModMetadata, Set<KClass<*>>> {
    val builder = ImmutableMap.builder<ModMetadata, Set<KClass<*>>>()
    modPacks.forEach { (metadata, packs) ->
        val set = ImmutableSet.builder<KClass<*>>()
        packs.forEach {
            set.addAll(scanPackage(it, predicate))
        }
        builder.put(metadata, set.build())
    }
    return builder.build()
}

val modPacks: Map<ModMetadata, Set<String>> by lazy {
    ImmutableMap.builder<ModMetadata, Set<String>>().also { packs ->
        loader.allMods.forEach {
            val set = ImmutableSet.builder<String>()
            it.metadata.customValues[IbukiGourd.MOD_ID]?.apply {
                asObject.get("package")?.asArray?.onEach { value ->
                    set.add(value.asString)
                }
                packs.put(it.metadata, set.build())
            }
        }
    }.build()
}

fun scanPackage(pack: String, predicate: (KClass<*>) -> Boolean = { true }): List<KClass<*>> {
    val list = ArrayList<KClass<*>>()
    ClassPath.from(IbukiGourd::class.java.classLoader).getTopLevelClassesRecursive(pack).forEach {
        runCatching {
            val clazz = Class.forName(it.name).kotlin
            clazz.java.declaredClasses.forEach { innerClass ->
                if (predicate(innerClass.kotlin)) list.add(innerClass.kotlin)
            }
            if (predicate(clazz)) list.add(clazz)
        }
    }
    return list
}

/**
 * 判断两个list是否完全匹配
 *
 * list.size == list2.size && list[0] == list2[0]
 *
 * @receiver List<*>
 * @param list List<*>
 * @return Boolean
 */
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

fun <T> MutableList<T>.moveElement(fromIndex: Int, toIndex: Int) {
    if (fromIndex == toIndex) return
    val movingElement = this.removeAt(fromIndex)
    this.add(toIndex, movingElement)
}

fun <T> Iterable<T>.forEachWithLimit(limit: Int, action: (T) -> Unit) {
    var count = 0
    for (element in this) {
        if (count >= limit) break
        action(element)
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
    this[oldKey]?.let { value ->
        this.remove(oldKey)
        this[newKey] = value
    }
}

fun <K, V> LinkedHashMap<K, V>.renameKey(oldKey: K, newKey: K) {
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