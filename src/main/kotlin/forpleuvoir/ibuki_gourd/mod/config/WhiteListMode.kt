package forpleuvoir.ibuki_gourd.mod.config

import forpleuvoir.ibuki_gourd.config.IConfigOptionItem


/**
 * 白名单模式

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.config

 * 文件名 WhiteListMode

 * 创建时间 2021/12/18 20:59

 * @author forpleuvoir

 */
enum class WhiteListMode(override val key: String, override val remark: String = "$key.remark") : IConfigOptionItem {
	WhiteList("ibuki_gourd.white_list.white_list"),
	BlackList("ibuki_gourd.white_list.black_list"),
	None("ibuki_gourd.white_list.none")
	;

	override fun cycle(): IConfigOptionItem {
		val size = values().size
		val index = values().indexOf(this)
		return if (index < size - 1) {
			values()[index + 1]
		} else {
			values()[0]
		}
	}

	override fun fromKey(key: String): IConfigOptionItem {
		getAllItem().forEach {
			if (it.key == key) return it
		}
		return None
	}

	override fun getAllItem(): List<IConfigOptionItem> {
		return values().toList()
	}
}