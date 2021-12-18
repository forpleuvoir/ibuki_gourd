package forpleuvoir.ibuki_gourd.config.options

import forpleuvoir.ibuki_gourd.keyboard.KeyBind


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigBooleanHotkey

 * 创建时间 2021/12/17 22:55

 * @author forpleuvoir

 */
class ConfigBooleanHotkey(defaultValue: KeyBind, val config: ConfigBoolean) :
	ConfigHotkey("${config.name}.hotkey", "${config.name}.remark", defaultValue) {
	init {
		this.setKeyCallback {
			this.config.toggle()
		}
	}

}