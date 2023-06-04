package moe.forpleuvoir.ibukigourd.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

open class ModLogger(clazz: KClass<*>, modName: String) {

	constructor(clazz: Class<*>, modName: String) : this(clazz.kotlin, modName)

	private val log: Logger

	init {
		log = LoggerFactory.getLogger("${modName}[${clazz.simpleName ?: clazz.java.simpleName}]")
	}

	fun info(msg: String, vararg params: Any?) {
		log.info(msg, *params)
	}

	fun error(msg: String, vararg params: Any?) {
		log.error(msg, *params)
	}

	fun error(msg: String, throwable: Throwable) {
		log.error(msg, throwable)
	}

	fun error(e: Exception) {
		log.error(e.message, e)
	}

	fun warn(msg: String, vararg params: Any?) {
		log.warn(msg, *params)
	}
}