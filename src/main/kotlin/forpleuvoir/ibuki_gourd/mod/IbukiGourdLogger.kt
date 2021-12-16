package forpleuvoir.ibuki_gourd.mod

import forpleuvoir.ibuki_gourd.common.ModLogger


/**
 * 日志

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod

 * 文件名 IbukiGourdLogger

 * 创建时间 2021/12/9 14:59

 * @author forpleuvoir

 */
class IbukiGourdLogger(clazz: Class<*>) : ModLogger(clazz, IbukiGourdMod) {

	companion object {
		@JvmStatic
		fun getLogger(clazz: Class<*>): IbukiGourdLogger {
			return IbukiGourdLogger(clazz)
		}
	}

}