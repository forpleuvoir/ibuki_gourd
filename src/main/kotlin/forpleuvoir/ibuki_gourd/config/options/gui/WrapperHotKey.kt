package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.config.gui.DialogHotkeySetting
import forpleuvoir.ibuki_gourd.config.options.ConfigHotkey
import forpleuvoir.ibuki_gourd.gui.button.ButtonIcon
import forpleuvoir.ibuki_gourd.gui.icon.Icon
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WrapperHotKey

 * 创建时间 2022/1/3 17:12

 * @author forpleuvoir

 */
class WrapperHotKey(config: ConfigHotkey, x: Int, y: Int, width: Int, height: Int) : ConfigWrapper<ConfigHotkey>(config, x, y, width, height) {

	private val setting: ButtonIcon =
		ButtonIcon(this.x, this.y, Icon.SETTING, this.height, padding = 4, renderBord = false, renderBg = true) {
			ScreenBase.openScreen(DialogHotkeySetting(this.config, 140, 60, parent))
		}

	private val button: ButtonConfigHotkey =
		ButtonConfigHotkey(this.setting.x + this.setting.width + 2, this.y, this.width - this.setting.width - 2, this.height, config)

	override fun initWidget() {
		addDrawableChild(setting)
		addDrawableChild(button)
	}
}