package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.gui.dialog.DialogBase
import forpleuvoir.ibuki_gourd.gui.widget.LabelText
import forpleuvoir.ibuki_gourd.gui.widget.WidgetSliderNumber
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 DialogBase

 * 创建时间 2021/12/23 16:57

 * @author forpleuvoir

 */
class DialogColorEditor(dialogWidth: Int, dialogHeight: Int, title: Text, parent: Screen) :
	DialogBase<DialogColorEditor>(dialogWidth, dialogHeight, title, parent) {

	private lateinit var red: WidgetSliderNumber
	private lateinit var green: WidgetSliderNumber
	private lateinit var blue: WidgetSliderNumber

	private lateinit var redText:LabelText
	private lateinit var greenText:LabelText
	private lateinit var blueText:LabelText

	override fun init() {
		super.init()
	}


}