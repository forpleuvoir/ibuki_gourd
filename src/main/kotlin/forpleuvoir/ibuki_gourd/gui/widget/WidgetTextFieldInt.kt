package forpleuvoir.ibuki_gourd.gui.widget

import forpleuvoir.ibuki_gourd.common.tText
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod.mc
import net.minecraft.client.gui.widget.TextFieldWidget
import java.util.function.Consumer

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WidgetTextFieldInt

 * 创建时间 2021/12/24 13:36

 * @author forpleuvoir

 */
class WidgetTextFieldInt(x: Int, y: Int, width: Int, height: Int, value: Int) :
	TextFieldWidget(mc.textRenderer, x, y, width, height, value.toString().tText()) {
	init {
		setTextPredicate {
			val regex = Regex("^-?[1-9]\\d*\$")
			regex.containsMatchIn(it)
		}
	}

	fun setConsumer(consumer: Consumer<Int?>){
		setChangedListener {
			consumer.accept(it.toIntOrNull())
		}
	}

}