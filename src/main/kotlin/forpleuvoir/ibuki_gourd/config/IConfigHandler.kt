package forpleuvoir.ibuki_gourd.config



/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 IConfigHandler

 * 创建时间 2021/12/12 19:03

 * @author forpleuvoir

 */
interface IConfigHandler {

	fun onConfigChange() {
		save()
		load()
	}

	fun save()

	fun load()

	fun allConfig(): List<IConfigBase>
}