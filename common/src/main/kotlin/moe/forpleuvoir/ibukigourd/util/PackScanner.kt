package moe.forpleuvoir.ibukigourd.util

import moe.forpleuvoir.ibukigourd.platform.PLATFORM
import kotlin.reflect.KClass

fun scanModPackage(predicate: (KClass<*>) -> Boolean = { true }): Map<String, Set<KClass<*>>> {
    return PLATFORM.getIGModClasses().mapValues { (_, value) ->
        value.filter(predicate).toSet()
    }
}

