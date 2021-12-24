package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.common.tText
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod
import net.minecraft.client.gui.widget.TextFieldWidget
import java.util.function.Consumer


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WidgetTextFieldDouble

 * 创建时间 2021/12/24 13:26

 * @author forpleuvoir

 */
class WidgetTextFieldDouble(x: Int, y: Int, width: Int, height: Int, value: Double) :
	TextFieldWidget(IbukiGourdMod.mc.textRenderer, x, y, width, height, value.toString().tText()) {
	init {
		setTextPredicate {
			val regex = Regex("^-?([1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|0?\\.0+|0)\$")
			regex.containsMatchIn(it)
		}
	}

	fun setConsumer(consumer: Consumer<Double?>) {
		setChangedListener {
			consumer.accept(it.toDoubleOrNull())
		}
	}

}