package moe.forpleuvoir.ibukigourd.util

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.reflect.ClassPath
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.platform.PLATFORM
import kotlin.reflect.KClass


fun scanModPackage(predicate: (KClass<*>) -> Boolean = { true }): Map<String, Set<KClass<*>>> {
    val builder = ImmutableMap.builder<String, Set<KClass<*>>>()
    PLATFORM.getIGModPackage().forEach { (metadata, packs) ->
        val set = ImmutableSet.builder<KClass<*>>()
        packs.forEach {
            set.addAll(scanPackage(it, predicate))
        }
        builder.put(metadata, set.build())
    }
    return builder.build()
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