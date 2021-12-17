package forpleuvoir.ibuki_gourd.config.options.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigInt
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.client.gui.widget.SliderWidget
import net.minecraft.text.LiteralText

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options.gui

 * 文件名 WidgetSliderConfigInt

 * 创建时间 2021/12/17 16:58

 * @author forpleuvoir

 */
class WidgetSliderConfigInt(x: Int, y: Int, width: Int, height: Int, private val config: ConfigInt) :
	SliderWidget(x, y, width, height, LiteralText.EMPTY, config.getValue() / ((config.maxValue - config.minValue).toDouble())) {

	init {
		println("value:$value")
		updateMessage()
	}

	override fun updateMessage() {
		this.message = config.getValue().toString().text()
	}

	override fun applyValue() {
		this.config.setValue(((config.maxValue - config.minValue) * this.value).toInt())
	}
}