package forpleuvoir.ibuki_gourd.mod.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import forpleuvoir.ibuki_gourd.config.options.ConfigInt
import forpleuvoir.ibuki_gourd.config.gui.IConfigGroup
import forpleuvoir.ibuki_gourd.mod.config.IbukiGourdConfigs


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.config.gui

 * 文件名 IbukiGourdConfigGroup

 * 创建时间 2021/12/21 16:29

 * @author forpleuvoir

 */
enum class IbukiGourdConfigGroup(override val key: String, override val remark: String = "$key.remark", private val configs: List<ConfigBase>) :
	IConfigGroup {
	Toggles("ibuki_gourd.config.toggles", configs = IbukiGourdConfigs.Values.OPTION),
	Values("ibuki_gourd.config.values", configs = IbukiGourdConfigs.Values.OPTION)
	;

	override fun all(): List<IConfigGroup> {
		return values().toList()
	}

	override fun option(): List<ConfigBase> {
		val list = ArrayList(configs)
		for (i in 1..30) {
			list.add(ConfigInt("凑数的", "凑数的", 0, 0, 99999))
		}
		return list
	}

	override val current: IConfigGroup
		get() = currentConfigGroup

	override fun changeCurrent(configGroup: IConfigGroup) {
		currentConfigGroup = configGroup
	}

	companion object {
		var currentConfigGroup: IConfigGroup = Values

	}
}


