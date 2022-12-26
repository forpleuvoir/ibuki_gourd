package com.forpleuvoir.ibukigourd.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

open class ModLogger(clazz: KClass<*>, modName: String) {

	private val log: Logger

	init {
		log = LoggerFactory.getLogger("${modName}[${clazz.simpleName ?: clazz.java.simpleName}]")
	}

	fun info(str: String, vararg params: Any?) {
		log.info(str, *params)
	}

	fun error(str: String, vararg params: Any?) {
		log.error(str, *params)
	}

	fun error(e: Exception) {
		log.error(e.message, e)
	}

	fun warn(str: String, vararg params: Any?) {
		log.warn(str, *params)
	}
}