package forpleuvoir.ibuki_gourd.mod.compat.ibuki_gourd

import forpleuvoir.ibuki_gourd.api.config.IbukiGourdConfigApi
import forpleuvoir.ibuki_gourd.config.IConfigHandler
import forpleuvoir.ibuki_gourd.mod.config.IbukiGourdConfigs

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.compat.ibuki_gourd

 * 文件名 ConfigImpl

 * 创建时间 2022/2/11 22:18

 * @author forpleuvoir

 */
class ConfigImpl : IbukiGourdConfigApi {

	override fun getConfigHandlerFactory(): () -> IConfigHandler {
		return { IbukiGourdConfigs }
	}

}