package forpleuvoir.ibuki_gourd.config

import forpleuvoir.ibuki_gourd.config.options.ConfigHotkey
import forpleuvoir.ibuki_gourd.mod.config.IbukiGourdConfigs


/**
 * 配置管理器

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 ConfigManager

 * 创建时间 2021/12/25 18:10

 * @author forpleuvoir

 */
object ConfigManager {
	private val configHandler: HashMap<String, IConfigHandler> = HashMap()

	fun register(modId: String, handler: IConfigHandler) {
		handler.allConfig().forEach {
			it.setOnValueChanged { handler.onConfigChange() }
		}
		handler.allConfig().forEach {
			if (it is ConfigHotkey) {
				it.initKeyBind()
			}
		}
		configHandler[modId] = handler
	}

	fun onChanged(modId: String) {
		configHandler[modId]?.onConfigChange()
	}

	fun loadAll() {
		configHandler.forEach {
			it.value.load()
		}
	}

	fun saveAll() {
		configHandler.forEach {
			it.value.save()
		}
	}

}