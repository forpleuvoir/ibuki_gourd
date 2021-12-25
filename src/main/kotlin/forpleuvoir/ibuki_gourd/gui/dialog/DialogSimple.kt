package forpleuvoir.ibuki_gourd.gui.dialog

import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.gui.dialog

 * 文件名 DialogSimple

 * 创建时间 2021/12/25 11:15

 * @author forpleuvoir

 */
abstract class DialogSimple(dialogWidth: Int, dialogHeight: Int, title: Text, parent: Screen?) :
	DialogBase<DialogSimple>(dialogWidth, dialogHeight, title, parent) {

	override fun init() {
		super.init()
		iniWidget()
	}

	abstract fun iniWidget()
}