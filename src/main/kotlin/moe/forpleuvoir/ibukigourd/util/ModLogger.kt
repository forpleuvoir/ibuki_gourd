package moe.forpleuvoir.ibukigourd.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

@Suppress("NOTHING_TO_INLINE")
@JvmInline
value class ModLogger internal constructor(
    private val log: Logger
) : Logger by log {

    constructor(clazz: Class<*>, modName: String) : this(clazz.kotlin, modName)

    constructor(clazz: KClass<*>, modName: String) : this(LoggerFactory.getLogger("${modName}[${clazz.simpleName ?: clazz.java.simpleName}]"))

    inline fun warn(throwable: Throwable) {
        this.warn(throwable.message, throwable)
    }

    inline fun error(throwable: Throwable) {
        this.error(throwable.message, throwable)
    }

}