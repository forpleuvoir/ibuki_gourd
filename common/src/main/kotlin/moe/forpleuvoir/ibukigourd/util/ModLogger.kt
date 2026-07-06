package moe.forpleuvoir.ibukigourd.util

import moe.forpleuvoir.ibukigourd.platform.PLATFORM
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.slf4j.spi.LoggingEventBuilder
import kotlin.reflect.KClass


@JvmInline
@Suppress("NOTHING_TO_INLINE")
value class ModLogger internal constructor(
    private val log: Logger
) : Logger by log {

    constructor(clazz: Class<*>, modName: String) : this(clazz.kotlin, modName)

    constructor(clazz: KClass<*>, modName: String) : this(LoggerFactory.getLogger("${modName}/${clazz.simpleName ?: clazz.java.simpleName}"))

    constructor(logName: String, modName: String) : this(LoggerFactory.getLogger("${modName}/$logName"))

    fun devInfo(msg: String) {
        if (PLATFORM.isDevEnvironment()) log.info(msg)
    }

    inline fun warn(throwable: Throwable) {
        this.warn(throwable.message, throwable)
    }

    inline fun error(throwable: Throwable) {
        this.error(throwable.message, throwable)
    }

    override fun makeLoggingEventBuilder(level: Level): LoggingEventBuilder {
        return log.makeLoggingEventBuilder(level)
    }

    override fun atLevel(level: Level): LoggingEventBuilder {
        return log.atLevel(level)
    }

    override fun isEnabledForLevel(level: Level): Boolean {
        return log.isEnabledForLevel(level)
    }

    override fun atTrace(): LoggingEventBuilder {
        return log.atTrace()
    }

    override fun atDebug(): LoggingEventBuilder {
        return log.atDebug()
    }

    override fun atInfo(): LoggingEventBuilder {
        return log.atInfo()
    }

    override fun atWarn(): LoggingEventBuilder {
        return log.atWarn()
    }

    override fun atError(): LoggingEventBuilder {
        return log.atError()
    }

}