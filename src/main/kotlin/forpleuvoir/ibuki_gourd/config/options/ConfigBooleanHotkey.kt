package forpleuvoir.ibuki_gourd.config.options

import forpleuvoir.ibuki_gourd.keyboard.KeyBind
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import forpleuvoir.ibuki_gourd.utils.Message


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigBooleanHotkey

 * 创建时间 2021/12/17 22:55

 * @author forpleuvoir

 */
class ConfigBooleanHotkey(defaultValue: KeyBind, val config: ConfigBoolean) :
	ConfigHotkey("${config.name}.hotkey", "${config.name}.remark", KeyBind().apply {
		copyOf(defaultValue)
		this.callback = {
			config.toggle()
			val status = if (config.getValue()) IbukiGourdLang.On.tText() else IbukiGourdLang.Off.tText()
			Message.showInfo(config.displayName.append(": ").append(status))
		}
	}) {

}