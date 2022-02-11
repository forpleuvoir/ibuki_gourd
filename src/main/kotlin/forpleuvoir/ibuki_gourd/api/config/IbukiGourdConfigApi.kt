package forpleuvoir.ibuki_gourd.api.config

import forpleuvoir.ibuki_gourd.config.IConfigHandler

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.api.config

 * 文件名 IbukiGourdConfigApi

 * 创建时间 2022/2/11 22:09

 * @author forpleuvoir

 */
interface IbukiGourdConfigApi {

	fun getConfigHandlerFactory(): () -> IConfigHandler

}