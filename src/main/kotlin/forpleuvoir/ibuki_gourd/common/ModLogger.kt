package forpleuvoir.ibuki_gourd.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.common

 * 文件名 ModLogger

 * 创建时间 2021/12/9 14:56

 * @author forpleuvoir

 */
open class ModLogger(clazz: Class<*>, mod: ModInfo) {

	private val log: Logger

	init {
		log = LoggerFactory.getLogger("${mod.modName.replace(" ", "")}[${clazz.simpleName}]")
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