package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.options.IConfigHotkey
import forpleuvoir.ibuki_gourd.gui.button.ButtonOption
import forpleuvoir.ibuki_gourd.gui.dialog.DialogBase
import forpleuvoir.ibuki_gourd.keyboard.KeyEnvironment
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import net.minecraft.client.gui.screen.Screen


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 DialogHotkeySetting

 * 创建时间 2022/1/3 18:22

 * @author forpleuvoir

 */
class DialogHotkeySetting(private val config: IConfigHotkey, dialogWidth: Int, dialogHeight: Int, parent: Screen?) :
	DialogBase<DialogHotkeySetting>(dialogWidth, dialogHeight, IbukiGourdLang.KeyEnvironment.tText(), parent) {

	private val values = ArrayList<String>().apply {
		KeyEnvironment.values().forEach {
			this.add(it.name)
		}
	}

	private lateinit var envButton: ButtonOption

	override fun init() {
		super.init()
		envButton = ButtonOption(
			values,
			config.getValue().keyEnvironment.name,
			x = this.x + this.paddingLeft + 5,
			y = this.paddingTop + this.y + this.contentHeight / 2 - 20 / 2,
			width = this.dialogWidth - (this.paddingLeft + this.paddingRight) - 10,
		) {
			config.setKeyEnvironment(KeyEnvironment.valueOf(it))
		}
		addDrawableChild(envButton)
	}

}